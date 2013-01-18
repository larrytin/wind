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

import com.goodow.wind.model.event.HandlerRegistration;
import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.op.OpSink;

import elemental.json.JsonType;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;

public abstract class JValue implements OpSink<JsonOp<?>> {
  public static abstract class JsonHandler {
    private JValue val;

    public JValue getValue() {
      return val;
    }

    void setValue(JValue val) {
      this.val = val;
    }
  }

  final Context ctx;
  final Path path;

  JValue(Context ctx, Path path) {
    this.ctx = ctx;
    this.path = path;
    // assert getValue() != null;
    assert getType() == getValue().getType();
  }

  JValue(JsonValue val) {
    // assert val != null;
    assert getType() == val.getType();
    ctx = new JsonModel().create(val).ctx;
    path = Path.of();
  }

  public <T extends JValue> T at(Path subPath) {
    return ctx.at(path.at(subPath));
  }

  @Override
  public void consume(JsonOp<?> op) {
      // assert ctx.get(op.getPath()) != null;
    ctx.at(op.getPath()).doConsume(op.getOp());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JValue)) {
      return false;
    }
    return toString().equals(obj.toString()) && path.equals(((JValue) obj).path);
  }

  public abstract JsonType getType();

  public HandlerRegistration on(Path subPath, JsonHandler handler) {
    return ctx.registerHandler(path.at(subPath), handler);
  }

  @Override
  public String toString() {
    return getValue().toJson();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  void consumeAndSubmit(Op<?> op) {
    doConsume(op);
    ctx.submitOp(new JsonOp(path, op));
  }

  abstract void doConsume(Op<?> op);

  void fireEventToParent() {
    ctx.fireEventToParent(path);
  }

  <T extends JValue.JsonHandler> ArrayOf<T> getHandlers() {
    return ctx.getHandlers(path);
  }

  JsonValue getValue() {
    return ctx.get(path);
  }

  HandlerRegistration on(JsonHandler handler) {
    return ctx.registerHandler(path, handler);
  }

  void replace(JsonValue val) {
    assert getType() == JsonType.STRING || getType() == JsonType.NUMBER
        || getType() == JsonType.NULL;
    ctx.setValue(path, val);
  }

  abstract void traverse(Visitor visitor);
}