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
import com.goodow.wind.model.op.basic.NoOp;
import com.goodow.wind.model.op.basic.ReplaceOp;
import com.goodow.wind.model.op.basic.ReplaceTarget;
import com.goodow.wind.model.op.list.StringOp;
import com.goodow.wind.model.op.list.algorithm.ListOp;
import com.goodow.wind.model.op.map.MapOp;
import com.goodow.wind.model.op.number.NumberOp;
import com.goodow.wind.model.util.Pair;
import com.goodow.wind.model.util.Serializer;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import elemental.util.Collections;
import elemental.util.MapFromStringToString;

public class JsonOp<T> implements Op<T> {
  @SuppressWarnings({"unchecked", "rawtypes"})
  static final JsonOp<?> NO_OP = new JsonOp(null, NoOp.INSTANCE);
  private static final String PATH = "p";
  private static final MapFromStringToString KEY_TO_TYPE = Collections.mapFromStringToString();
  static {
    KEY_TO_TYPE.put(ListOp.TYPE, JsonType.ARRAY.name());
    KEY_TO_TYPE.put(ReplaceOp.TYPE, JsonType.NULL.name());
    KEY_TO_TYPE.put(NumberOp.TYPE, JsonType.NUMBER.name());
    KEY_TO_TYPE.put(MapOp.TYPE, JsonType.OBJECT.name());
    KEY_TO_TYPE.put(StringOp.TYPE, JsonType.STRING.name());
  }

  @SuppressWarnings("unchecked")
  public static JsonValue buildJsonFromInitialOp(JsonOp<?> initOp) {
    Op<?> op = initOp.getOp();
    assert Path.of().equals(initOp.getPath()) && op instanceof ReplaceOp;
    final JsonValue[] toRtn = new JsonValue[1];
    ((ReplaceOp<JsonValue>) op).apply(new ReplaceTarget<JsonValue>() {
      @Override
      public ReplaceTarget<JsonValue> replace(JsonValue oldValue, JsonValue newValue) {
        assert oldValue == null;
        toRtn[0] = newValue;
        return null;
      }
    });
    if (toRtn[0] == null) {
      assert op.isNoOp();
      return Json.createNull();
    }
    return toRtn[0];
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  static JsonOp<?> parse(JsonObject op) {
    String[] keys = op.keys();
    assert keys.length == 2;
    String key = PATH.equals(keys[0]) ? keys[1] : keys[0];
    JsonType type = JsonType.valueOf(KEY_TO_TYPE.get(key));
    return new JsonOp(Path.of(op.getString(PATH)), createOp(type, op.get(key)));
  }

  private static Op<?> createOp(JsonType type, JsonValue value) {
    switch (type) {
      case STRING:
        return new StringOp(value.toJson());
      case NUMBER:
        return new NumberOp().add(value.asNumber());
      case OBJECT:
        return new ObjectOp(value.toJson());
      case ARRAY:
        return new ArrayOp(value.toJson());
      case NULL:
        return new ReplaceOp<JsonValue>(value.toJson(), Serializer.JSON);
      case BOOLEAN:
      default:
        throw new IllegalArgumentException("JsonType: " + type.name() + " is not supported");
    }
  }

  private final Op<T> op;
  private final Path path;

  public JsonOp(Path path, Op<T> op) {
    this.op = op;
    this.path = path;
  }

  @Override
  public void apply(T target) {
    throw new IllegalStateException();
  }

  @Override
  public JsonOp<T> composeWith(Op<T> op) {
    assert op instanceof JsonOp;
    assert !(op instanceof ListOp);
    assert !isNoOp() && !op.isNoOp();
    return new JsonOp<T>(path, this.op.composeWith(((JsonOp<T>) op).getOp()));
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JsonOp)) {
      return false;
    }
    return toString().equals(obj.toString());
  }

  @Override
  public String getType() {
    return "json";
  }

  @Override
  public JsonOp<T> invert() {
    return new JsonOp<T>(path, op.invert());
  }

  @Override
  public boolean isNoOp() {
    return op.isNoOp();
  }

  @Override
  public String toString() {
    return "{\"" + JsonOp.PATH + "\":\"" + path.toString() + "\",\"" + op.getType() + "\":"
        + op.toString() + "}";
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Pair<JsonOp<T>, ? extends JsonOp<?>> transformWith(Op<?> clientOp) {
    assert !isNoOp() && !clientOp.isNoOp();
    assert clientOp instanceof JsonOp;
    JsonOp<?> op = (JsonOp) clientOp;
    if (!path.equals(op.getPath())) {
      boolean serverIsParent = path.toString().length() < op.getPath().toString().length();
      if (serverIsParent ? path.isAncestorOf(op.getPath()) : op.getPath().isAncestorOf(path)) {
        JsonOp<?> parentOp = serverIsParent ? this : op;
        JsonOp<?> childOp = serverIsParent ? op : this;
        if (MapOp.TYPE == parentOp.getOp().getType()) {
          return ((ObjectOp) parentOp.getOp()).<T> transformWithChild(serverIsParent, parentOp,
              childOp);
        } else {
          assert ListOp.TYPE == parentOp.getOp().getType();
          return ((ArrayOp) parentOp.getOp()).<T> transformWithChild(serverIsParent, parentOp,
              childOp);
        }
      }
      return Pair.of(this, op);
    }
    assert getType() == op.getType();
    Pair<? extends Op<T>, ? extends Op<?>> pair = this.op.transformWith(op.getOp());
    JsonOp<?> transformedClientOp = new JsonOp(path, pair.second);
    return Pair.of(new JsonOp<T>(path, pair.first), transformedClientOp);
  }

  Op<T> getOp() {
    return op;
  }

  Path getPath() {
    return path;
  }
}