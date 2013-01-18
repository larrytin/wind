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
package com.goodow.wind.channel.op;

import com.goodow.wind.channel.ChannelRegistry;
import com.goodow.wind.channel.op.GenericOperationChannel.ReceiveOpChannel;
import com.goodow.wind.channel.rpc.Rpc;
import com.goodow.wind.channel.rpc.SnapshotService;
import com.goodow.wind.channel.rpc.SubmitDeltaService;
import com.goodow.wind.model.json.JValue;
import com.goodow.wind.model.json.JsonHandlerRegistry;
import com.goodow.wind.model.json.JsonModel;
import com.goodow.wind.model.json.JsonOp;
import com.goodow.wind.model.op.OpSink;

import com.google.gwt.core.client.Callback;

import java.util.logging.Level;
import java.util.logging.Logger;

import elemental.json.JsonType;
import elemental.json.JsonValue;

public class JsonOperationSucker implements GenericOperationChannel.Listener<JsonOp<?>> {
  private static final Logger logger = Logger.getLogger(JsonOperationSucker.class.getName());

  private final GenericOperationChannel<JsonOp<?>> channel;

  private final String key;
  private final String sessionId;
  private final Rpc rpc;
  private final JsonModel model;
  private JValue val;
  private final OpSink<JsonOp<?>> outputSink;

  @SuppressWarnings({"rawtypes", "unchecked"})
  public JsonOperationSucker(final String key, String sessionId, Rpc rpc) {
    this.key = key;
    this.sessionId = sessionId;
    this.rpc = rpc;

    model = new JsonModel();

    ReceiveOpChannel<JsonOp<?>> receiveChannel =
        new ReceiveOpChannelImpl<JsonOp<?>>(key, rpc, model);
    channel =
        new GenericOperationChannel(new TransformQueue(model), receiveChannel,
            new SubmitDeltaService(rpc, sessionId, key), this);
    outputSink = new OpSink<JsonOp<?>>() {
      @Override
      public void consume(JsonOp<?> op) {
        logger.info("Saving " + key);
        channel.send(op);
      }
    };
  }

  @Override
  public void onAck(JsonOp<?> serverHistoryOp, boolean clean) {
    if (clean) {
      logger.info("Saved " + key);
    }
  }

  @Override
  public void onError(Throwable e) {
    logger.log(Level.WARNING, "Unable to save", e);
  }

  @Override
  public void onRemoteOp(JsonOp<?> serverHistoryOp) {
    while (channel.peek() != null) {
      val.consume(channel.receive());
    }
  }

  public void open(JsonType type, final ChannelRegistry registry,
      final Callback<JValue, Void> callback) {
    SnapshotService snapshotService = new SnapshotService(rpc);
    snapshotService.getOrCreate(key, sessionId, type, new SnapshotService.Callback() {

      @Override
      public void onConnect(String channelToken) {
        registry.getChannel().connect(channelToken);
      }

      @Override
      public void onSuccess(JsonValue snapshot, int version) {
        val = model.create(snapshot);
        channel.connect(version, sessionId);
        registry.registerSnapshot(key, val, channel);
        model.init(val, key, outputSink, JsonHandlerRegistry.ROOT);
        if (callback != null) {
          callback.onSuccess(val);
        }
      }
    });
  }
}