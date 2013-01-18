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

@SuppressWarnings("unused")
public abstract class StringHandler extends JValue.JsonHandler {
  @Override
  public JString getValue() {
    return (JString) super.getValue();
  }

  public void onDelete(int idx, String str) {
  }

  public void onInsert(int idx, String str) {
  }

  public void render(JString str) {
  }

  @Override
  public String toString() {
    return JsonType.STRING.name();
  }
}