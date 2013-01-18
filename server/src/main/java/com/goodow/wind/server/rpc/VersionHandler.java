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
import com.goodow.wind.server.model.ObjectId;
import com.goodow.wind.server.model.ObjectSession;
import com.goodow.wind.server.model.SessionId;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.walkaround.slob.server.AccessDeniedException;
import com.google.walkaround.slob.server.SlobFacilities;
import com.google.walkaround.slob.server.SlobNotFoundException;
import com.google.walkaround.slob.server.SlobStore.ConnectResult;
import com.google.walkaround.util.server.servlet.AbstractHandler;
import com.google.walkaround.util.server.servlet.BadRequestException;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gets current object version and creates a new session. Used for reconnection.
 */
public class VersionHandler extends AbstractHandler {

  private static final Logger log = Logger.getLogger(VersionHandler.class.getName());

  @Inject
  SlobFacilities slobFacilities;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String sid = requireParameter(req, Constants.Params.SESSION_ID);
    String key = requireParameter(req, Constants.Params.ID);
    ObjectSession session = new ObjectSession(new ObjectId(key), new SessionId(sid));
    log.info("connect " + session);

    ConnectResult result;
    try {
      result =
          slobFacilities.getSlobStore().reconnect(session.getObjectId(), session.getSessionId());
    } catch (SlobNotFoundException e) {
      throw new BadRequestException("Object not found or access denied", e);
    } catch (AccessDeniedException e) {
      throw new BadRequestException("Object not found or access denied", e);
    }
    log.info("connect " + session + ": " + result);

    JsonObject obj = new JsonObject();
    obj.addProperty(Constants.Params.VERSION, result.getVersion());

    resp.setContentType("application/json");
    Util.writeJsonResult(resp.getWriter(), obj.toString());
  }
}