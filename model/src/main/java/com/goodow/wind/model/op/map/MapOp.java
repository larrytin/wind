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
package com.goodow.wind.model.op.map;

import com.goodow.wind.model.op.ComposeException;
import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.op.TransformException;
import com.goodow.wind.model.util.JsonUtil;
import com.goodow.wind.model.util.Pair;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.util.ArrayOfString;
import elemental.util.Collections;
import elemental.util.MapFromStringTo;

public class MapOp<T> implements Op<MapTarget<T>>, MapTarget<T> {
  public static final String TYPE = "o";
  private static final String INSERT = "i";
  private static final String DELETE = "d";
  protected final MapFromStringTo<Pair<T, T>> components;

  public MapOp() {
    components = Collections.mapFromStringTo();
  }

  public MapOp(String json) {
    this();
    JsonArray components = Json.instance().parse(json);
    for (int i = 0, len = components.length(); i < len; i++) {
      JsonArray component = components.getArray(i);
      if (component.length() == 3) {
        update(component.getString(0), fromJson(component.get(1)), fromJson(component.get(2)));
      } else {
        assert component.length() == 2;
        JsonObject obj = component.getObject(1);
        assert obj.keys().length == 1;
        if (INSERT.equals(obj.keys()[0])) {
          update(component.getString(0), null, fromJson(obj.get(INSERT)));
        } else {
          assert DELETE.equals(obj.keys()[0]);
          update(component.getString(0), fromJson(obj.get(DELETE)), null);
        }
      }
    }
  }

  @Override
  public void apply(MapTarget<T> target) {
    ArrayOfString keys = components.keys();
    for (int i = 0, len = keys.length(); i < len; i++) {
      String key = keys.get(i);
      target.update(key, components.get(key).first, components.get(key).second);
    }
  }

  @Override
  public MapOp<T> composeWith(Op<MapTarget<T>> op) {
    assert op instanceof MapOp;
    MapOp<T> toRtn = clone();
    MapOp<T> o = (MapOp<T>) op;
    ArrayOfString keys = o.components.keys();
    for (int i = 0, len = keys.length(); i < len; i++) {
      String key = keys.get(i);
      toRtn.update(key, o.components.get(key).first, o.components.get(key).second);
    }
    return toRtn;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MapOp)) {
      return false;
    }
    return toString().equals(obj.toString());
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public MapOp<T> invert() {
    MapOp<T> op = newInstance();
    ArrayOfString keys = components.keys();
    for (int i = 0, len = keys.length(); i < len; i++) {
      String key = keys.get(i);
      op.update(key, components.get(key).second, components.get(key).first);
    }
    return op;
  }

  @Override
  public boolean isNoOp() {
    return components.keys().isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    ArrayOfString keys = components.keys();
    for (int i = 0, len = keys.length(); i < len; i++) {
      String key = keys.get(i);
      if (i != 0) {
        sb.append(",");
      }
      sb.append(toJson(key, components.get(key).first, components.get(key).second));
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public Pair<MapOp<T>, MapOp<T>> transformWith(Op<?> clientOp) {
    assert clientOp instanceof MapOp;
    @SuppressWarnings("unchecked")
    MapOp<T> op = (MapOp<T>) clientOp;
    MapOp<T> transformedClientOp = newInstance();
    ArrayOfString clientKeys = op.components.keys();
    for (int i = 0, len = clientKeys.length(); i < len; i++) {
      String clientKey = clientKeys.get(i);
      T clientOldValue = op.components.get(clientKey).first;
      T clientNewValue = op.components.get(clientKey).second;
      if (!components.hasKey(clientKey)) {
        transformedClientOp.update(clientKey, clientOldValue, clientNewValue);
        continue;
      }
      T serverOldValue = components.get(clientKey).first;
      T serverNewValue = components.get(clientKey).second;
      if (!areEqual(serverOldValue, clientOldValue)) {
        throw new TransformException("Mismatched initial value: attempt to transform "
            + toJson(clientKey, serverOldValue, serverNewValue) + " with "
            + toJson(clientKey, clientOldValue, clientNewValue));
      }
      if (areEqual(serverNewValue, clientNewValue)) {
        continue;
      }
      transformedClientOp.update(clientKey, serverNewValue, clientNewValue);
    }
    MapOp<T> transformedServerOp = exclude(op);
    return Pair.of(transformedServerOp, transformedClientOp);
  }

  @Override
  public MapOp<T> update(String key, T oldValue, T newValue) {
    assert key != null : "Null key";
    if (areEqual(oldValue, newValue)) {
      return this;
    }
    if (!components.hasKey(key)) {
      components.put(key, Pair.of(oldValue, newValue));
      return this;
    }
    if (!areEqual(components.get(key).second, oldValue)) {
      throw new ComposeException("Mismatched value: attempt to compose "
          + toJson(key, components.get(key).first, components.get(key).second) + " with "
          + toJson(key, oldValue, newValue));
    }
    if (areEqual(components.get(key).first, newValue)) {
      components.remove(key);
    } else {
      components.put(key, Pair.of(components.get(key).first, newValue));
    }
    return this;
  }

  protected boolean areEqual(T a, T b) {
    return (a == null) ? b == null : a.equals(b);
  }

  @Override
  protected MapOp<T> clone() {
    MapOp<T> toRtn = newInstance();
    ArrayOfString keys = components.keys();
    for (int i = 0, len = keys.length(); i < len; i++) {
      String key = keys.get(i);
      toRtn.update(key, components.get(key).first, components.get(key).second);
    }
    return toRtn;
  }

  @SuppressWarnings("unchecked")
  protected T fromJson(JsonValue json) {
    return (T) JsonUtil.fromJson(json);
  }

  protected MapOp<T> newInstance() {
    return new MapOp<T>();
  }

  protected String toJson(T value) {
    return JsonUtil.toJson(value);
  }

  private MapOp<T> exclude(MapOp<T> clientOp) {
    MapOp<T> transformedServerOp = newInstance();
    ArrayOfString keys = components.keys();
    for (int i = 0, len = keys.length(); i < len; i++) {
      String key = keys.get(i);
      if (!clientOp.components.hasKey(key)) {
        transformedServerOp.update(key, components.get(key).first, components.get(key).second);
      }
    }
    return transformedServerOp;
  }

  private String toJson(String key, T oldVal, T newVal) {
    StringBuilder sb = new StringBuilder("[\"").append(key).append("\"").append(",");
    if (oldVal == null) {
      assert newVal != null;
      sb.append("{\"" + INSERT + "\":").append(toJson(newVal)).append("}");
    } else if (newVal == null) {
      sb.append("{\"" + DELETE + "\":").append(toJson(oldVal)).append("}");
    } else {
      sb.append(toJson(oldVal)).append(",");
      sb.append(toJson(newVal));
    }
    return sb.append("]").toString();
  }
}