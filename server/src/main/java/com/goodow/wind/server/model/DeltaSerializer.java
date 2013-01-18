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

import com.goodow.wind.channel.rpc.Constants;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.logging.Logger;

public class DeltaSerializer {

  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(DeltaSerializer.class.getName());

  /** The largest integer that can be represented losslessly by a double */
  public static final long MAX_DOUBLE_INTEGER = 1L << 52 - 1;

  public static JsonObject dataToClientJson(Delta<String> data, long resultingRevision) {
    Preconditions.checkArgument(resultingRevision <= MAX_DOUBLE_INTEGER,
        "Resulting revision %s is too large", resultingRevision);

    // Assume payload is JSON, and parse it to avoid nested json.
    // TODO: Consider using Delta<JSONObject> instead.
    // The reason I haven't done it yet is because it's not immutable,
    // and also for reasons described in Delta.
    JsonObject payloadJson;
    try {
      payloadJson = new JsonParser().parse(data.getPayload()).getAsJsonObject();
    } catch (JsonParseException e) {
      throw new IllegalArgumentException("Invalid payload for " + data, e);
    }

    JsonObject json = new JsonObject();
    try {
      Preconditions.checkArgument(resultingRevision >= 0, "invalid rev %s", resultingRevision);
      json.addProperty(Constants.Params.VERSION, resultingRevision);
      long sanityCheck = json.get(Constants.Params.VERSION).getAsLong();
      if (sanityCheck != resultingRevision) {
        throw new AssertionError("resultingRevision " + resultingRevision
            + " not losslessly represented in JSON, got back " + sanityCheck);
      }
      json.addProperty(Constants.Params.SESSION_ID, data.getClientId().getId());
      json.add(Constants.Params.OPERATION, payloadJson);
      return json;
    } catch (JsonParseException e) {
      throw new Error(e);
    }
  }

  private DeltaSerializer() {
  }
}