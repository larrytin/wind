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
import com.goodow.wind.model.op.number.NumberOp;
import com.goodow.wind.model.op.number.NumberTarget;

import elemental.json.Json;
import elemental.json.JsonNumber;
import elemental.json.JsonType;
import elemental.util.ArrayOf;

public class JNumber extends JValue {
  JNumber(Context ctx, Path path) {
    super(ctx, path);
  }

  JNumber(double num) {
    super(Json.create(num));
  }

  public JNumber add(double num) {
    NumberOp op = new NumberOp().add(num);
    consumeAndSubmit(op);
    return this;
  }

  public double getNumber() {
    return ((JsonNumber) getValue()).getNumber();
  }

  @Override
  public JsonType getType() {
    return JsonType.NUMBER;
  }

  public HandlerRegistration on(NumberHandler handler) {
    return super.on(handler);
  }

  @Override
  void doConsume(Op<?> op) {
    assert op instanceof NumberOp;
    ((NumberOp) op).apply(new NumberTarget() {
      @Override
      public NumberTarget add(double num) {
        addAndFireEvent(num);
        return null;
      }
    });
  }

  void fireEvent(double num, boolean isInit) {
    ArrayOf<NumberHandler> handlers = getHandlers();
    if (handlers != null && !handlers.isEmpty()) {
      for (int i = 0, len = handlers.length(); i < len; i++) {
        NumberHandler handler = handlers.get(i);
        if (isInit) {
          handler.setValue(this);
          handler.render(this);
        }
        handler.onAdd(num);
      }
    }
  }

  @Override
  void traverse(Visitor visitor) {
    visitor.visit(this);
  }

  private void addAndFireEvent(double num) {
    replace(Json.create(getNumber() + num));
    if (ctx.shouldFireEvent()) {
      fireEvent(num, false);
      fireEventToParent();
    }
  }
}