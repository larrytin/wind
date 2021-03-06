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
package com.google.walkaround.slob.server;

import com.google.gson.Gson;

public class ServerMutateResponse {

  static final ServerMutateResponse fromJson(String json) {
    return new Gson().fromJson(json, ServerMutateResponse.class);
  }

  private final long resultingVersion;

  private final String broadcastData;

  public ServerMutateResponse(long resultingVersion, String broadcastData) {
    this.resultingVersion = resultingVersion;
    this.broadcastData = broadcastData;
  }

  public String getBroadcastData() {
    return broadcastData;
  }

  public long getResultingVersion() {
    return resultingVersion;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}