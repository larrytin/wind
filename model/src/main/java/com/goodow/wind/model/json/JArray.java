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
import com.goodow.wind.model.json.Visitor.DestroyVisitor;
import com.goodow.wind.model.json.Visitor.InitializeVisitor;
import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.op.list.algorithm.ListOp;
import com.goodow.wind.model.op.list.algorithm.ListTarget;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;

public class JArray extends JValue {
  private JsonArray array;

  JArray() {
    super(Json.createArray());
  }

  JArray(Context ctx, Path path) {
    super(ctx, path);
  }

  public JArray asArray(int idx) {
    assert idx <= length();
    if (idx == length()) {
      insert(idx, Json.createArray());
    } else if (JsonType.NULL == array().get(idx).getType()) {
      return this.<JNull> get(idx).asArray();
    }
    return getArray(idx);
  }

  public JObject asObject(int idx) {
    assert idx <= length();
    if (idx == length()) {
      insert(idx, Json.createObject());
    } else if (JsonType.NULL == array().get(idx).getType()) {
      return this.<JNull> get(idx).asObject();
    }
    return getObject(idx);
  }

  @SuppressWarnings("unchecked")
  public <T extends JValue> T get(int idx) {
    JsonValue val = array().get(idx);
    if (val == null) {
      return null;
    }
    return (T) at(Path.of().at(idx));
  }

  public JArray getArray(int idx) {
    assert idx < length();
    return new JArray(ctx, path.at(idx));
  }

  public boolean getBoolean(int idx) {
    return array().getBoolean(idx);
  }

  public double getNumber(int idx) {
    return array().getNumber(idx);
  }

  public JObject getObject(int idx) {
    assert idx < length();
    return new JObject(ctx, path.at(idx));
  }

  public String getString(int idx) {
    return array().getString(idx);
  }

  @Override
  public JsonType getType() {
    return JsonType.ARRAY;
  }

  public JArray insert(int idx, boolean bool) {
    insert(idx, Json.create(bool));
    return this;
  }

  public JArray insert(int idx, double num) {
    insert(idx, Json.create(num));
    return this;
  }

  public JArray insert(int idx, String str) {
    assert str != null;
    insert(idx, Json.create(str));
    return this;
  }

  public int length() {
    return array().length();
  }

  public HandlerRegistration on(ArrayHandler handler) {
    return super.on(handler);
  }

  public JArray remove(int idx) {
    return remove(idx, 1);
  }

  public JArray remove(int idx, int length) {
    assert length > 0;
    JsonArray list = Json.createArray();
    for (int i = 0; i < length; i++) {
      list.set(i, array().get(idx + i));
    }
    ArrayOp op = new ArrayOp(false, idx, list, length());
    consumeAndSubmit(op);
    return this;
  }

  JsonArray array() {
    if (array == null) {
      array = (JsonArray) getValue();
    }
    return array;
  }

  @SuppressWarnings("unchecked")
  @Override
  void doConsume(Op<?> op) {
    assert op instanceof ListOp;
    ((ListOp<JsonArray>) op).apply(new ListTarget<JsonArray>() {
      private int cursor;

      @Override
      public ListTarget<JsonArray> delete(JsonArray list) {
        assert list.length() > 0;
        assert cursor + list.length() <= length();
        for (int i = list.length() - 1; i >= 0; i--) {
          assert array().get(cursor + i).toJson().equals(list.get(i).toJson());
          removeAndFireEvent(cursor + i);
        }
        return null;
      }

      @Override
      public ListTarget<JsonArray> insert(JsonArray list) {
        assert list.length() > 0;
        assert cursor <= length();
        for (int i = 0, len = list.length(); i < len; i++) {
          insertAndFireEvent(cursor + i, list.get(i));
        }
        cursor += list.length();
        return null;
      }

      @Override
      public ListTarget<JsonArray> retain(int length) {
        cursor += length;
        return null;
      }
    });
  }

  void fireEvent(int idx, JsonValue del, JValue ins, boolean isInit) {
    ArrayOf<ArrayHandler> handlers = getHandlers();
    if (handlers != null && !handlers.isEmpty()) {
      for (int i = 0, len = handlers.length(); i < len; i++) {
        ArrayHandler handler = handlers.get(i);
        if (isInit) {
          handler.setValue(this);
          handler.render(this);
          continue;
        }
        if (ins != null) {
          handler.onInsert(idx, ins);
        } else {
          assert del != null;
          handler.onDelete(idx, del);
        }
      }
    }
  }

  @Override
  void traverse(Visitor visitor) {
    if (visitor.visit(this)) {
      for (int i = 0; i < length(); i++) {
        if (visitor.visitIndex(this, i)) {
          visitor.accept(get(i));
        }
        visitor.endVisitIndex(this, i);
      }
    }
    visitor.endVisit(this);
  }

  private void insert(int idx, JsonValue value) {
    if (value == null) {
      value = Json.createNull();
    }
    JsonArray list = Json.createArray();
    list.set(0, value);
    ArrayOp op = new ArrayOp(true, idx, list, length());
    consumeAndSubmit(op);
  }

  private void insertAndFireEvent(int idx, JsonValue value) {
    assert value != null;
    array().insert(idx, value);
    if (ctx.shouldFireEvent()) {
      JValue val = get(idx);
      fireEvent(idx, null, val, false);
      new InitializeVisitor().accept(val);
      fireEventToParent();
    }
  }

  private void removeAndFireEvent(int idx) {
    JsonValue toRemove = null;
    if (ctx.shouldFireEvent()) {
      new DestroyVisitor().accept(get(idx));
      toRemove = array().get(idx);
    }
    array().remove(idx);
    if (ctx.shouldFireEvent()) {
      fireEvent(idx, toRemove, null, false);
      fireEventToParent();
    }
  }
}