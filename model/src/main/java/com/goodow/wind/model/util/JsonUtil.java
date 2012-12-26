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
package com.goodow.wind.model.util;

import elemental.json.JsonValue;

public class JsonUtil {
  public static Object fromJson(JsonValue json) {
    switch (json.getType()) {
      case BOOLEAN:
        return json.asBoolean();
      case NULL:
        return null;
      case NUMBER:
        return json.asNumber();
      case STRING:
        return json.asString();
      case ARRAY:
      case OBJECT:
      default:
        throw new UnsupportedOperationException("Cannot parse Json Type " + json.getType() + ":\n"
            + json.toJson());
    }
  }

  public static String toJson(Object obj) {
    if (obj instanceof String) {
      return "\"" + obj + "\"";
    }
    return "" + obj;
  }
}
