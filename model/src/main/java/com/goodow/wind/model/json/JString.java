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
import com.goodow.wind.model.op.list.StringOp;
import com.goodow.wind.model.op.list.algorithm.ListOp;
import com.goodow.wind.model.op.list.algorithm.ListTarget;

import elemental.json.Json;
import elemental.json.JsonString;
import elemental.json.JsonType;
import elemental.util.ArrayOf;

public class JString extends JValue {
  JString(Context ctx, Path path) {
    super(ctx, path);
  }

  JString(String str) {
    super(Json.create(str));
  }

  public JString delete(int idx, int length) {
    String snapshot = getString();
    assert idx + length <= snapshot.length();
    ListOp<String> op =
        new StringOp(false, idx, snapshot.substring(idx, idx + length), snapshot.length());
    consumeAndSubmit(op);
    return this;
  }

  public String getString() {
    return ((JsonString) getValue()).getString();
  }

  @Override
  public JsonType getType() {
    return JsonType.STRING;
  }

  public JString insert(int idx, String str) {
    String snapshot = getString();
    assert idx <= snapshot.length();
    ListOp<String> op = new StringOp(true, idx, str, snapshot.length());
    consumeAndSubmit(op);
    return this;
  }

  public HandlerRegistration on(StringHandler handler) {
    return super.on(handler);
  }

  @SuppressWarnings("unchecked")
  @Override
  void doConsume(Op<?> op) {
    assert op instanceof ListOp;
    ((ListOp<String>) op).apply(new ListTarget<String>() {
      private int cursor;

      @Override
      public ListTarget<String> delete(String str) {
        assert getString().substring(cursor, cursor + str.length()).equals(str);
        deleteAndFireEvent(cursor, str.length());
        return null;
      }

      @Override
      public ListTarget<String> insert(String str) {
        insertAndFireEvent(cursor, str);
        cursor += str.length();
        return null;
      }

      @Override
      public ListTarget<String> retain(int length) {
        cursor += length;
        return null;
      }
    });
  }

  void fireEvent(boolean isInsert, int idx, String str, boolean isInit) {
    ArrayOf<StringHandler> handlers = getHandlers();
    if (handlers != null && !handlers.isEmpty()) {
      for (int i = 0, len = handlers.length(); i < len; i++) {
        StringHandler handler = handlers.get(i);
        if (isInit) {
          handler.setValue(this);
          handler.render(this);
        }
        if (isInsert) {
          handler.onInsert(idx, str);
        } else {
          handler.onDelete(idx, str);
        }
      }
    }
  }

  @Override
  void traverse(Visitor visitor) {
    visitor.visit(this);
  }

  private void deleteAndFireEvent(int idx, int length) {
    String snapshot = getString();
    assert idx + length <= snapshot.length();
    replace(Json.create(snapshot.substring(0, idx) + snapshot.substring(idx + length)));
    if (ctx.shouldFireEvent()) {
      fireEvent(false, idx, snapshot.substring(idx, idx + length), false);
      fireEventToParent();
    }
  }

  private void insertAndFireEvent(int idx, String str) {
    String snapshot = getString();
    assert idx <= snapshot.length();
    replace(Json.create(snapshot.substring(0, idx) + str + snapshot.substring(idx)));
    if (ctx.shouldFireEvent()) {
      fireEvent(true, idx, str, false);
      fireEventToParent();
    }
  }
}