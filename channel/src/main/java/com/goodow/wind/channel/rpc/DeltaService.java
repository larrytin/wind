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
package com.goodow.wind.channel.rpc;

import com.goodow.wind.channel.ChannelRegistry;
import com.goodow.wind.channel.rpc.Constants.Params;

import java.util.logging.Level;
import java.util.logging.Logger;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.util.Collections;
import elemental.util.MapFromStringToString;

/**
 * Handles getting a channel token for connecting a browser channel, and fetching missing messages.
 */
public class DeltaService {
  public interface Callback {
    void onConnectionError(Throwable e);

    void onFatalError(Throwable e);

    void onMessage(JsonObject msg);
  }

  private static final Logger log = Logger.getLogger(DeltaService.class.getName());
  private final Rpc rpc;

  public DeltaService(Rpc rpc) {
    this.rpc = rpc;
  }

  public void fetchHistories(final String sessionId, final JsonArray keys,
      final ChannelRegistry registry) {
    MapFromStringToString params = Collections.mapFromStringToString();
    params.put(Constants.Params.SESSION_ID, sessionId);
    params.put(Constants.Params.ID, keys.toJson());
    rpc.makeRequest(Rpc.Method.GET, Constants.Services.DELTA, params, new Rpc.RpcCallback() {
      @Override
      public void onConnectionError(Throwable e) {
        log.log(Level.WARNING, "onConnectionError ", e);
      }

      @Override
      public void onFatalError(Throwable e) {
        log.log(Level.WARNING, "onFatalError ", e);
      }

      @Override
      public void onSuccess(String data) {
        JsonObject msgs = RpcUtil.evalPrefixed(data);
        registry.getChannel().connect(msgs.getString(Params.TOKEN));
        JsonArray array = msgs.getArray(Params.DELTAS);
        for (int i = 0, len = array.length(); i < len; i++) {
          JsonObject msg = array.getObject(i);
          assert msg.hasKey(Params.VERSION) && msg.hasKey(Params.ID);
          registry.getChannel().publishMessage(msg);
        }
      }
    });
  }

  public void fetchHistory(String key, final int version, final Callback callback) {
    MapFromStringToString params = Collections.mapFromStringToString();
    params.put(Constants.Params.ID, key);
    params.put(Constants.Params.VERSION, version + "");
    rpc.makeRequest(Rpc.Method.GET, Constants.Services.DELTA, params, new Rpc.RpcCallback() {
      @Override
      public void onConnectionError(Throwable e) {
        callback.onConnectionError(e);
      }

      @Override
      public void onFatalError(Throwable e) {
        callback.onFatalError(e);
      }

      @Override
      public void onSuccess(String data) {
        JsonObject msg = RpcUtil.evalPrefixed(data);
        assert msg.hasKey(Params.HAS_MORE) && msg.getArray(Params.DELTAS).length() > 0;
        callback.onMessage(msg);
      }
    });
  }
}