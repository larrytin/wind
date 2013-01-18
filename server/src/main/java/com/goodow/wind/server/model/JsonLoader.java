/*
 * Copyright 2012 Goodow.com
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
package com.goodow.wind.server.model;

import com.goodow.wind.model.id.RandomBase64Generator;
import com.goodow.wind.model.json.JsonOp;
import com.goodow.wind.model.json.Path;
import com.goodow.wind.model.op.basic.ReplaceOp;
import com.goodow.wind.model.op.basic.ReplaceTarget;
import com.goodow.wind.model.util.Pair;
import com.goodow.wind.model.util.Serializer;

import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.walkaround.slob.server.AccessDeniedException;
import com.google.walkaround.slob.server.SlobAlreadyExistsException;
import com.google.walkaround.slob.server.SlobFacilities;
import com.google.walkaround.slob.server.SlobNotFoundException;
import com.google.walkaround.slob.server.SlobStore;
import com.google.walkaround.slob.server.SlobStore.ConnectResult;
import com.google.walkaround.util.server.RetryHelper;
import com.google.walkaround.util.server.RetryHelper.PermanentFailure;
import com.google.walkaround.util.server.RetryHelper.RetryableFailure;
import com.google.walkaround.util.server.appengine.CheckedDatastore;
import com.google.walkaround.util.server.appengine.CheckedDatastore.CheckedTransaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import elemental.json.Json;
import elemental.json.JsonType;
import elemental.json.JsonValue;

public class JsonLoader {

  private static final Logger log = Logger.getLogger(JsonLoader.class.getName());

  private final RandomBase64Generator random64;
  private final CheckedDatastore datastore;
  private final SlobStore store;

  private final Provider<SlobFacilities> facilities;

  @Inject
  JsonLoader(SlobStore store, Random random, CheckedDatastore datastore,
      Provider<SlobFacilities> facilities) {
    this.store = store;
    this.facilities = facilities;
    this.random64 = new RandomBase64Generator(random);
    this.datastore = datastore;
  }

  public void create(final ObjectId id, final SessionId sessionId, final JsonType jsonType)
      throws IOException {
    try {
      new RetryHelper().run(new RetryHelper.Body<Void>() {
        @Override
        public Void run() throws RetryableFailure, PermanentFailure {
          CheckedTransaction tx = datastore.beginTransaction();
          try {
            store.newObject(tx, id, "", createInitOp(sessionId, jsonType), false);
            tx.commit();
          } catch (SlobAlreadyExistsException e) {
            throw new RetryableFailure("Object id collision, retrying: " + id, e);
          } catch (AccessDeniedException e) {
            throw new RuntimeException("Unexpected AccessDeniedException creating object " + id, e);
          } finally {
            tx.close();
          }
          return null;
        }
      });
    } catch (PermanentFailure e) {
      throw new IOException("PermanentFailure creating object", e);
    }
  }

  public Pair<ConnectResult, String> load(ObjectId id, SessionId sessionId) throws IOException,
      AccessDeniedException, SlobNotFoundException {
    Preconditions.checkNotNull(id, "Null object key");
    Preconditions.checkNotNull(sessionId, "Null sessionId");

    Pair<ConnectResult, String> pair = store.connect(id, sessionId);
    log.info("load(" + id + "): " + pair.getFirst());
    return pair;
  }

  public Pair<ConnectResult, String> loadOrCreate(String key, SessionId sessionId, JsonType jsonType)
      throws IOException, AccessDeniedException {
    Preconditions.checkNotNull(key, "Null object key");
    Preconditions.checkNotNull(sessionId, "Null sessionId");

    ObjectId id;
    if (!key.contains(ObjectId.SEPERATE)) {
      id = new ObjectId(key, random64.next(96 / 6));
      create(id, sessionId, jsonType);
    } else {
      id = new ObjectId(key);
      Entity existing = findObject(id);
      if (existing == null) {
        create(id, sessionId, jsonType);
      }
    }
    try {
      return load(id, sessionId);
    } catch (SlobNotFoundException e) {
      throw new IllegalStateException("PermanentFailure creating object", e);
    }
  }

  public String loadStaticAtVersion(ObjectId objectId, @Nullable Long version) throws IOException,
      AccessDeniedException, SlobNotFoundException {
    Preconditions.checkNotNull(objectId, "Null objectId");
    return store.loadAtVersion(objectId, version);
  }

  @SuppressWarnings("unchecked")
  private List<Delta<String>> createInitOp(SessionId sessionId, JsonType jsonType) {
    JsonValue initJson;
    switch (jsonType) {
      case ARRAY:
        initJson = Json.createArray();
        break;
      case BOOLEAN:
        initJson = Json.create(false);
        break;
      case NUMBER:
        initJson = Json.create(0);
        break;
      case OBJECT:
        initJson = Json.createObject();
        break;
      case STRING:
        initJson = Json.create("");
        break;
      case NULL:
      default:
        throw new UnsupportedOperationException();
    }
    JsonOp<?> op =
        new JsonOp<ReplaceTarget<JsonValue>>(Path.of(), new ReplaceOp<JsonValue>(Serializer.JSON)
            .replace(null, initJson));
    Delta<String> delta = new Delta<String>(sessionId, op.toString());
    return Arrays.<Delta<String>> asList(delta);
  }

  private Entity findObject(final ObjectId id) throws IOException {
    try {
      CheckedTransaction tx = datastore.beginTransaction();
      try {
        return tx.get(facilities.get().makeRootEntityKey(id));
      } finally {
        tx.close();
      }
    } catch (PermanentFailure e) {
      log.log(Level.SEVERE, "Failed to look up " + id, e);
      throw new IOException(e);
    } catch (RetryableFailure e) {
      log.log(Level.SEVERE, "Failed to look up " + id, e);
      throw new IOException(e);
    }
  }
}