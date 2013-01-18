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
package com.goodow.wind.channel;

import com.goodow.wind.channel.impl.GaeChannel;
import com.goodow.wind.channel.impl.PollingChannel;
import com.goodow.wind.channel.op.GenericOperationChannel;
import com.goodow.wind.channel.op.JsonOperationSucker;
import com.goodow.wind.channel.rpc.Rpc;
import com.goodow.wind.channel.rpc.impl.AjaxRpc;
import com.goodow.wind.model.json.JValue;

import com.google.gwt.core.client.Callback;

import elemental.json.JsonType;
import elemental.util.ArrayOfString;
import elemental.util.Collections;
import elemental.util.MapFromStringTo;
import elemental.util.MapFromStringToInt;

public class ChannelRegistry {
  private static class JsonSnapshot {
    final JValue snapshot;
    final GenericOperationChannel<?> channel;

    JsonSnapshot(JValue snapshot, GenericOperationChannel<?> channel) {
      this.snapshot = snapshot;
      this.channel = channel;
    }
  }

  public static final ChannelRegistry ROOT = new ChannelRegistry(null);

  private static final Rpc rpc = new AjaxRpc("", null);

  private final MapFromStringTo<JsonSnapshot> snapshots;
  private final MapFromStringToInt keys = Collections.mapFromStringToInt();
  private final ChannelRegistry parent;

  private ChannelRegistry(ChannelRegistry parent) {
    this.parent = parent;
    if (parent == null) {
      snapshots = Collections.<JsonSnapshot> mapFromStringTo();
    } else {
      this.snapshots = parent.snapshots;
    }
  }

  public void cleanup() {
    for (int i = 0, len = keys.keys().length(); i < len; i++) {
      unregisterSnapshot(keys.keys().get(i));
    }
  }

  public ChannelRegistry createExtension() {
    return new ChannelRegistry(this);
  }

  public ChannelDemuxer getChannel() {
    return GaeChannel.get();
  }

  public ArrayOfString getKeys() {
    return keys.keys();
  }

  public Rpc getRpc() {
    return rpc;
  }

  public String getSessionId() {
    return GaeChannel.get().getSessionId();
  }

  public int getVersion(String key) {
    return snapshots.get(key).channel.version();
  }

  public void open(final String key, JsonType type, final Callback<JValue, Void> callback) {
    if (snapshots.hasKey(key)) {
      addKey(key, 1);
      if (callback != null) {
        JsonSnapshot snapshot = snapshots.get(key);
        callback.onSuccess(snapshot.snapshot);
      }
      return;
    }
    new JsonOperationSucker(key, getSessionId(), getRpc()).open(type, this, callback);
  }

  public void registerSnapshot(String key, JValue snapshot, GenericOperationChannel<?> channel) {
    addKey(key, 1);
    assert !snapshots.hasKey(key);
    snapshots.put(key, new JsonSnapshot(snapshot, channel));
    PollingChannel.get().connect(null);
  }

  public void unregisterSnapshot(String key) {
    int num = keys.get(key);
    ChannelRegistry p = this;
    while (p != null) {
      int origNum = p.keys.get(key);
      if (origNum > num) {
        p.keys.put(key, origNum - num);
      } else {
        assert origNum == num;
        p.keys.remove(key);
      }
      p = p.parent;
    }
  }

  private void addKey(String key, int num) {
    ChannelRegistry p = this;
    while (p != null) {
      p.keys.put(key, p.keys.hasKey(key) ? p.keys.get(key) + num : num);
      p = p.parent;
    }
  }
}