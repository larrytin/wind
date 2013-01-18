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

public interface Serializer<T> {
  public final static Serializer<Object> PRIMITIVE = new Serializer<Object>() {

    @Override
    public boolean areEqual(Object a, Object b) {
      return (a == null) ? b == null : a.equals(b);
    }

    @Override
    public Object fromJson(JsonValue json) {
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
          throw new UnsupportedOperationException("Cannot parse Json Type " + json.getType()
              + ":\n" + json.toJson());
      }
    }

    @Override
    public String toString(Object obj) {
      if (obj instanceof String) {
        return "\"" + obj + "\"";
      }
      return "" + obj;
    }
  };

  public final static Serializer<JsonValue> JSON = new Serializer<JsonValue>() {

    @Override
    public boolean areEqual(JsonValue a, JsonValue b) {
      if (a == null && b == null) {
        return true;
      }
      if (a == null && b != null || a != null && b == null) {
        return false;
      }
      return toString(a).equals(toString(b));
    }

    @Override
    public JsonValue fromJson(JsonValue json) {
      return json;
    }

    @Override
    public String toString(JsonValue json) {
      return json == null ? null : "" + json.toJson();
    }
  };

  boolean areEqual(T a, T b);

  T fromJson(JsonValue s);

  String toString(T x);
}