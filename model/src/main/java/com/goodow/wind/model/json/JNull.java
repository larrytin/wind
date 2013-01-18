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

import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.op.basic.ReplaceOp;
import com.goodow.wind.model.op.basic.ReplaceTarget;
import com.goodow.wind.model.util.Serializer;

import elemental.json.Json;
import elemental.json.JsonType;
import elemental.json.JsonValue;

public class JNull extends JValue {
  JNull() {
    super(Json.createNull());
  }

  JNull(Context ctx, Path path) {
    super(ctx, path);
  }

  public JArray asArray() {
    ReplaceOp<JsonValue> op = replaceOp().replace(Json.createNull(), Json.createArray());
    consumeAndSubmit(op);
    return new JArray(ctx, path);
  }

  public JNumber asNumber() {
    ReplaceOp<JsonValue> op = replaceOp().replace(Json.createNull(), Json.create(0));
    consumeAndSubmit(op);
    return new JNumber(ctx, path);
  }

  public JObject asObject() {
    ReplaceOp<JsonValue> op = replaceOp().replace(Json.createNull(), Json.createObject());
    consumeAndSubmit(op);
    return new JObject(ctx, path);
  }

  public JString asString() {
    ReplaceOp<JsonValue> op = replaceOp().replace(Json.createNull(), Json.create(""));
    consumeAndSubmit(op);
    return new JString(ctx, path);
  }

  @Override
  public JsonType getType() {
    return JsonType.NULL;
  }

  @Override
  public String toString() {
    return "null";
  }

  @SuppressWarnings("unchecked")
  @Override
  void doConsume(Op<?> op) {
    assert op instanceof ReplaceOp;
    ((ReplaceOp<JsonValue>) op).apply(new ReplaceTarget<JsonValue>() {
      @Override
      public ReplaceTarget<JsonValue> replace(JsonValue oldValue, JsonValue newValue) {
        assert oldValue != null && getValue().toJson().equals(oldValue.toJson());
        JNull.super.replace(newValue);
        return null;
      }
    });
  }

  @Override
  void traverse(Visitor visitor) {
    visitor.visit(this);
  }

  private ReplaceOp<JsonValue> replaceOp() {
    return new ReplaceOp<JsonValue>(Serializer.JSON);
  }
}