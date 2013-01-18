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
import com.goodow.wind.model.op.map.MapOp;
import com.goodow.wind.model.op.map.MapTarget;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;

public class JObject extends JValue {
  private JsonObject obj;

  JObject() {
    super(Json.createObject());
  }

  JObject(Context ctx, Path path) {
    super(ctx, path);
  }

  public JArray asArray(String key) {
    if (!object().hasKey(key)) {
      put(key, Json.createArray());
    } else if (JsonType.NULL == object().get(key).getType()) {
      return this.<JNull> get(key).asArray();
    }
    return getArray(key);
  }

  public JObject asObject(String key) {
    if (!object().hasKey(key)) {
      put(key, Json.createObject());
    } else if (JsonType.NULL == object().get(key).getType()) {
      return this.<JNull> get(key).asObject();
    }
    return getObject(key);
  }

  @SuppressWarnings("unchecked")
  public <T extends JValue> T get(String key) {
    JsonValue val = object().get(key);
    if (val == null) {
      return null;
    }
    return (T) at(Path.of(key));
  }

  public JArray getArray(String key) {
    return new JArray(ctx, path.at(key));
  }

  public boolean getBoolean(String key) {
    return object().getBoolean(key);
  }

  public double getNumber(String key) {
    return object().getNumber(key);
  }

  public JObject getObject(String key) {
    return new JObject(ctx, path.at(key));
  }

  public String getString(String key) {
    return object().getString(key);
  }

  @Override
  public JsonType getType() {
    return JsonType.OBJECT;
  }

  public boolean hasKey(String key) {
    return object().hasKey(key);
  }

  public String[] keys() {
    return object().keys();
  }

  public HandlerRegistration on(ObjectHandler handler) {
    return super.on(handler);
  }

  public JObject put(String key, boolean bool) {
    put(key, Json.create(bool));
    return this;
  }

  public JObject put(String key, double num) {
    put(key, Json.create(num));
    return this;
  }

  public JObject put(String key, String str) {
    assert str != null;
    put(key, Json.create(str));
    return this;
  }

  public JObject remove(String key) {
    if (!hasKey(key)) {
      return this;
    }
    MapOp<JsonValue> op = op(key, null);
    consumeAndSubmit(op);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  void doConsume(Op<?> op) {
    assert op instanceof MapOp;
    ((MapOp<JsonValue>) op).apply(new MapTarget<JsonValue>() {
      @Override
      public MapTarget<JsonValue> update(String key, JsonValue oldValue, JsonValue newValue) {
        assert oldValue == null || object().get(key).toJson().equals(oldValue.toJson());
        if (newValue == null) {
          removeAndFireEvent(key);
        } else {
          putAndFireEvent(key, newValue);
        }
        return null;
      }
    });
  }

  void fireEvent(String key, JsonValue oldVal, JValue newVal, boolean isInit) {
    assert isInit || (oldVal != null && newVal == null) || (oldVal == null && newVal != null)
        || !oldVal.toJson().equals(newVal.toString());
    ArrayOf<ObjectHandler> handlers = getHandlers();
    if (handlers != null && !handlers.isEmpty()) {
      for (int i = 0, len = handlers.length(); i < len; i++) {
        ObjectHandler handler = handlers.get(i);
        if (isInit) {
          handler.setValue(this);
          handler.render(this);
          continue;
        }
        if (oldVal == null) {
          handler.onAdded(key, newVal);
        } else if (newVal == null) {
          handler.onRemoved(key, oldVal);
        } else {
          handler.onUpdated(key, oldVal, newVal);
        }
      }
    }
  }

  JsonObject object() {
    if (obj == null) {
      obj = (JsonObject) getValue();
    }
    return obj;
  }

  @Override
  void traverse(Visitor visitor) {
    if (visitor.visit(this)) {
      for (String key : keys()) {
        if (visitor.visitKey(this, key)) {
          visitor.accept(get(key));
        }
        visitor.endVisitKey(this, key);
      }
    }
    visitor.endVisit(this);
  }

  private MapOp<JsonValue> op(String key, JsonValue newVal) {
    return new ObjectOp().update(key, object().get(key), newVal);
  }

  private void put(String key, JsonValue val) {
    Path.checkKey(key);
    if (val == null) {
      val = Json.createNull();
    }
    MapOp<JsonValue> op = op(key, val);
    consumeAndSubmit(op);
  }

  private void putAndFireEvent(String key, JsonValue val) {
    Path.checkKey(key);
    assert val != null;
    JsonValue oldVal = null;
    if (ctx.shouldFireEvent() && hasKey(key)) {
      oldVal = object().get(key);
      new DestroyVisitor().accept(get(key));
    }
    object().put(key, val);
    if (ctx.shouldFireEvent()) {
      JValue newVal = get(key);
      fireEvent(key, oldVal, newVal, false);
      new InitializeVisitor().accept(newVal);
      fireEventToParent();
    }
  }

  private void removeAndFireEvent(String key) {
    assert hasKey(key);
    JsonValue toRemove = null;
    if (ctx.shouldFireEvent()) {
      new DestroyVisitor().accept(get(key));
      toRemove = object().get(key);
    }
    object().remove(key);
    if (ctx.shouldFireEvent()) {
      fireEvent(key, toRemove, null, false);
      fireEventToParent();
    }
  }
}