/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.walkaround.slob.server;

import com.goodow.wind.server.model.ObjectId;
import com.goodow.wind.server.servlet.PostCommitTaskHandler;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.walkaround.slob.shared.SlobModel.ReadableSlob;
import com.google.walkaround.util.server.RetryHelper;
import com.google.walkaround.util.server.RetryHelper.PermanentFailure;
import com.google.walkaround.util.server.RetryHelper.RetryableFailure;
import com.google.walkaround.util.server.appengine.CheckedDatastore;
import com.google.walkaround.util.server.appengine.CheckedDatastore.CheckedTransaction;
import com.google.walkaround.util.server.appengine.MemcacheTable;

import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Schedules task queue tasks that invoke {@link PostCommitAction} in a throttled manner.
 */
public class PostCommitActionScheduler {
  // Protocol (all of the following is for a given slob; different slobs are
  // independent (except that they compete for CPU and other resources)):
  //
  // We keep an "action pending" marker in memcache. The presence of this
  // marker means that a task queue task is scheduled that will execute some
  // time in the future and run the post-commit actions.
  //
  // On every slob update in the datastore:
  //
  // - begin transaction
  // - slob update (some gets and puts on the entity group)
  // - if marker not present, schedule task at time T
  // - commit
  // - if task scheduled, put marker into memcache, expiring at time T-buffer
  // (buffer to compensate for clock skew)
  //
  // Task queue task:
  //
  // - do a datastore put on the entity group. This ensures that this task runs
  // outside of any "if marker not present, schedule task, commit" block -- we
  // either want that block to re-run, or this task to run after that block
  // - run post-commit actions
  //
  // This protocol ensures that a task queue task will run after every update,
  // since:
  //
  // - Start of task and update transaction never happen concurrently; may
  // attempt in parallel but one will retry if so.
  //
  // - Tasks don't interfere with one another, and neither do updates (memcache
  // entry may be overwritten with an earlier expiry but that's still
  // correct).
  //
  // - Presence of the marker in memcache always implies that a task will run in
  // the future, since we only put after successfully scheduling a task, and
  // the expiry is before the ETA.
  //
  // - A task cannot begin during an update; so the update will either schedule
  // a task, or a marker was present while the update was running. In both
  // cases, a task will run after the update, which is the property we're
  // after.
  //
  // (Still not convinced; need to use formal techniques.)

  private static final Logger log = Logger.getLogger(PostCommitActionScheduler.class.getName());
  private static final String MEMCACHE_TAG_PREFIX = "PostCommitActionPending";
  // Used in the key of sync entities.
  private static final String SYNC_ENTITY_NAME = "Sync";

  private final Set<PostCommitAction> actions;
  private final int postCommitActionIntervalMillis;
  private final MemcacheTable<ObjectId, Boolean> postCommitActionPending;
  private final String taskUrl;
  private final Random random;
  // We treat the cache-clearing InternalPostCommitAction specially since we
  // want it to always run first; otherwise, it could be preempted by
  // user-defined PostCommitActions that always crash.
  private final SlobStoreImpl.InternalPostCommitAction internalPostCommit;
  private final CheckedDatastore datastore;

  @Inject
  public PostCommitActionScheduler(Set<PostCommitAction> actions,
      @PostCommitActionIntervalMillis int postCommitActionIntervalMillis,
      MemcacheTable.Factory memcacheFactory, @PostCommitTaskUrl String taskUrl, Random random,
      SlobStoreImpl.InternalPostCommitAction internalPostCommit, CheckedDatastore datastore) {
    this.actions = actions;
    this.postCommitActionIntervalMillis = postCommitActionIntervalMillis;
    this.postCommitActionPending = memcacheFactory.create(MEMCACHE_TAG_PREFIX);
    this.taskUrl = taskUrl;
    this.random = random;
    this.internalPostCommit = internalPostCommit;
    this.datastore = datastore;
  }

  public void prepareCommit(CheckedTransaction tx, final ObjectId slobId,
      final long resultingVersion, final ReadableSlob resultingState) throws PermanentFailure,
      RetryableFailure {
    // Can't short-circuit if actions.isEmpty() because we always have internalPostCommit.
    @Nullable
    Long cacheEntryExpirationMillis;
    if (postCommitActionPending.get(slobId) == Boolean.TRUE) {
      log.info("Post-commit actions pending on " + slobId + " , not scheduling task");
      cacheEntryExpirationMillis = null;
    } else {
      long delayMillis =
          postCommitActionIntervalMillis == 0 ? 0 : random.nextInt(postCommitActionIntervalMillis)
              + (postCommitActionIntervalMillis / 2L);
      long timeNowMillis = System.currentTimeMillis();
      cacheEntryExpirationMillis = timeNowMillis + delayMillis;
      long taskEtaMillis = cacheEntryExpirationMillis
      // We need to be sure that the cache entry expires before the task queue
      // task runs, so we add some safety buffer in case the machine clocks
      // are out of sync.
          + 2000;
      log.info("Scheduling post-commit actions on " + slobId + " ; time now=" + timeNowMillis
          + ", cache entry expiration=" + cacheEntryExpirationMillis + ", task eta="
          + taskEtaMillis);
      scheduleTask(tx, slobId, taskEtaMillis);
    }
    @Nullable
    final Long cacheEntryExpirationMillisFinal = cacheEntryExpirationMillis;
    tx.runAfterCommit(new Runnable() {
      @Override
      public void run() {
        if (cacheEntryExpirationMillisFinal != null) {
          postCommitActionPending.put(slobId, true, Expiration.onDate(new Date(
              cacheEntryExpirationMillisFinal)));
        }
        for (PostCommitAction action : getActions()) {
          log.info("Running immediate post-commit action " + action + " on " + slobId);
          action.unreliableImmediatePostCommit(slobId, resultingVersion, resultingState);
        }
      }
    });
  }

  public void taskInvoked(final ObjectId slobId) {
    try {
      new RetryHelper().run(new RetryHelper.VoidBody() {
        @Override
        public void run() throws RetryableFailure, PermanentFailure {
          CheckedTransaction tx = datastore.beginTransaction();
          tx.put(new Entity(KeyFactory.createKey(MutationLog.makeRootEntityKey(slobId), slobId
              .getKind()
              + SYNC_ENTITY_NAME, SYNC_ENTITY_NAME)));
          tx.commit();
        }
      });
    } catch (PermanentFailure e) {
      throw new RuntimeException("Failed to touch sync entity, trying again later", e);
    }
    for (PostCommitAction action : getActions()) {
      log.info("Running reliable post-commit action " + action + " on " + slobId);
      // TODO: Should this be wrapped in a try...catch and just log
      // any exceptions in order to truly reliably run all the post-commit actions?
      action.reliableDelayedPostCommit(slobId);
    }
  }

  private Iterable<PostCommitAction> getActions() {
    return Iterables.concat(ImmutableList.of(internalPostCommit), actions);
  }

  private void scheduleTask(CheckedTransaction tx, final ObjectId slobId, long taskEtaMillis)
      throws PermanentFailure, RetryableFailure {
    Queue postCommitActionQueue = QueueFactory.getQueue("post-commit");
    tx.enqueueTask(postCommitActionQueue, TaskOptions.Builder.withUrl(taskUrl).param(
        PostCommitTaskHandler.SLOB_ID_PARAM, slobId.toString()).etaMillis(taskEtaMillis));
  }
}