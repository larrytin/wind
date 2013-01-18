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

import com.goodow.wind.channel.op.GenericOperationChannel.SendOpService;
import com.goodow.wind.channel.rpc.Constants.Params;
import com.goodow.wind.model.op.Op;

import java.util.logging.Level;
import java.util.logging.Logger;

import elemental.json.JsonObject;
import elemental.util.ArrayOf;
import elemental.util.Collections;
import elemental.util.MapFromStringToString;

/**
 * Low-level service that submits a delta to a wave. Does not handle being called while another
 * delta is still in flight, that is a job for the channel layer above.
 */
public class SubmitDeltaService<O extends Op<?>> implements SendOpService<O> {
  private static final Logger log = Logger.getLogger(SubmitDeltaService.class.getName());
  private final Rpc rpc;
  private final String sessionId;
  private final String key;

  public SubmitDeltaService(Rpc rpc, String sessionId, String key) {
    this.rpc = rpc;
    this.sessionId = sessionId;
    this.key = key;
  }

  @Override
  public void callbackNotNeeded(SendOpService.Callback callback) {
    // nothing
  }

  @Override
  public void requestRevision(final SendOpService.Callback callback) {
    MapFromStringToString params = Collections.mapFromStringToString();
    params.put(Params.SESSION_ID, sessionId);
    params.put(Params.ID, key);
    rpc.makeRequest(Rpc.Method.GET, Constants.Services.VERSION, params, new Rpc.RpcCallback() {
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
        log.log(Level.FINE, data);
        JsonObject connectResponse = RpcUtil.evalPrefixed(data);
        callback.onSuccess((int) connectResponse.getNumber(Constants.Params.VERSION));
      }
    });
  }

  @Override
  public void submitOperations(int revision, ArrayOf<O> operations,
      final SendOpService.Callback callback) {
    MapFromStringToString params = Collections.mapFromStringToString();
    params.put(Params.SESSION_ID, sessionId);
    params.put(Params.ID, key);
    params.put(Params.VERSION, revision + "");
    params.put(Params.DELTAS, serialize(operations));
    rpc.makeRequest(Rpc.Method.POST, Constants.Services.SUBMIT_DELTA, params,
        new Rpc.RpcCallback() {
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
            JsonObject json = RpcUtil.evalPrefixed(data);
            callback.onSuccess((int) json.getNumber(Constants.Params.VERSION));
          }
        });
  }

  protected String serialize(ArrayOf<O> ops) {
    return "[" + ops.join() + "]";
  }
}
