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
package com.goodow.realtime.op;


public class RealtimeOp {

  private final String sessionId;
  private final String userId;
  private final Op<?> op;

  public RealtimeOp(String objectId, String sessionId, String userId, Op<?> op) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.op = op;
  }

  @SuppressWarnings("unchecked")
  public <T> Op<T> getOp() {
    return (Op<T>) op;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUserId() {
    return userId;
  }

}
