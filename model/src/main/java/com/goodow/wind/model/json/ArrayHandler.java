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
package com.goodow.wind.model.json;

import elemental.json.JsonType;
import elemental.json.JsonValue;

@SuppressWarnings("unused")
public abstract class ArrayHandler extends JValue.JsonHandler {
  @Override
  public JArray getValue() {
    return (JArray) super.getValue();
  }

  public void onChildChanged(int idx) {
  }

  public void onDelete(int idx, JsonValue val) {
  }

  public void onInsert(int idx, JValue val) {
  }

  public void render(JArray array) {
  }

  @Override
  public String toString() {
    return JsonType.ARRAY.name();
  }
}