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
package com.goodow.wind.server.rpc;

import com.goodow.wind.channel.rpc.Constants;
import com.goodow.wind.model.util.Pair;
import com.goodow.wind.server.model.JsonLoader;
import com.goodow.wind.server.model.ObjectId;
import com.goodow.wind.server.model.SessionId;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.walkaround.slob.server.AccessDeniedException;
import com.google.walkaround.slob.server.SlobNotFoundException;
import com.google.walkaround.slob.server.SlobStore.ConnectResult;
import com.google.walkaround.util.server.servlet.AbstractHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import elemental.json.JsonType;

public class SnapshotHandler extends AbstractHandler {
  private static final Logger log = Logger.getLogger(SnapshotHandler.class.getName());

  @Inject
  private JsonLoader loader;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String key = requireParameter(req, Constants.Params.ID);
    String versionString = optionalParameter(req, Constants.Params.VERSION, null);
    @Nullable
    Long version = versionString == null ? null : Long.parseLong(versionString);

    JsonObject obj;
    try {
      if (version == null) {
        String sid = requireParameter(req, Constants.Params.SESSION_ID);
        String jsonType = optionalParameter(req, Constants.Params.KIND, null);
        Pair<ConnectResult, String> pair =
            loader.loadOrCreate(key, new SessionId(sid), jsonType == null ? null : JsonType
                .valueOf(jsonType));
        obj = serialize(pair);
      } else {
        String snapshot = loader.loadStaticAtVersion(new ObjectId(key), version);
        obj = new JsonObject();
        obj.add(Constants.Params.SNAPSHOT, new JsonParser().parse(snapshot));
      }
    } catch (AccessDeniedException e) {
      log.log(Level.SEVERE, "Object not found or access denied", e);
      return;
    } catch (SlobNotFoundException e) {
      log.log(Level.SEVERE, "Object not found or access denied", e);
      return;
    } catch (IOException e) {
      log.log(Level.SEVERE, "Server error loading object", e);
      return;
    }

    resp.setContentType("application/json");
    Util.writeJsonResult(resp.getWriter(), obj.toString());
  }

  private final JsonObject serialize(Pair<ConnectResult, String> pair) {
    JsonObject obj = new JsonObject();
    obj.addProperty(Constants.Params.TOKEN, pair.first.getChannelToken());
    obj.addProperty(Constants.Params.VERSION, pair.first.getVersion());
    obj.add(Constants.Params.SNAPSHOT, new JsonParser().parse(pair.second));
    return obj;
  }
}