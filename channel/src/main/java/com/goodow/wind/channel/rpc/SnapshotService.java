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

import java.util.logging.Level;
import java.util.logging.Logger;

import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import elemental.util.Collections;
import elemental.util.MapFromStringToString;

public class SnapshotService {
  public interface Callback {
    void onConnect(String channelToken);

    void onSuccess(JsonValue snapshot, int version);
  }

  private final Rpc rpc;

  private static final Logger logger = Logger.getLogger(SnapshotService.class.getName());

  public SnapshotService(Rpc rpc) {
    this.rpc = rpc;
  }

  public void getOrCreate(final String key, String sessionId, JsonType jsonType,
      final Callback callback) {
    MapFromStringToString params = Collections.mapFromStringToString();
    params.put(Constants.Params.ID, key);
    params.put(Constants.Params.SESSION_ID, sessionId);
    if (jsonType != null) {
      params.put(Constants.Params.KIND, jsonType.name());
    }
    rpc.makeRequest(Rpc.Method.GET, Constants.Services.SNAPSHOT, params, new Rpc.RpcCallback() {

      @Override
      public void onConnectionError(Throwable e) {
        logger.log(Level.WARNING, "Connection Error when get snapshot for " + key, e);
      }

      @Override
      public void onFatalError(Throwable e) {
        logger.log(Level.SEVERE, "Fatal Error when get snapshot for " + key, e);
      }

      @Override
      public void onSuccess(String data) {
        JsonObject obj = RpcUtil.evalPrefixed(data);
        callback.onConnect(obj.getString(Constants.Params.TOKEN));
        callback.onSuccess(obj.get(Constants.Params.SNAPSHOT), (int) obj
            .getNumber(Constants.Params.VERSION));
      }
    });
  }
}