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

import com.goodow.wind.model.op.map.MapOp;
import com.goodow.wind.model.util.Pair;
import com.goodow.wind.model.util.Serializer;

import elemental.json.Json;
import elemental.json.JsonValue;

class ObjectOp extends MapOp<JsonValue> {
  ObjectOp() {
    super(Serializer.JSON);
  }

  ObjectOp(String json) {
    super(json, Serializer.JSON);
  }

  @Override
  protected ObjectOp newInstance() {
    return new ObjectOp();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  <T> Pair<JsonOp<T>, ? extends JsonOp<?>> transformWithChild(boolean serverIsParent,
      JsonOp<?> parentOp, JsonOp<?> childOp) {
    Pair<String, String> pair = childOp.getPath().nextKey(parentOp.getPath().toString());
    if (!components.hasKey(pair.first)) {
      return serverIsParent ? Pair.of((JsonOp<T>) parentOp, childOp) : Pair.of((JsonOp<T>) childOp,
          parentOp);
    }
    assert components.get(pair.first).first != null;
    JValue val =
        new JsonModel().create(Json.instance().parse(components.get(pair.first).first.toJson()));
    val.consume(new JsonOp(Path.of(pair.second), childOp.getOp()));

    ObjectOp op = (ObjectOp) copy();
    op.components.put(pair.first, Pair.of(val.getValue(), components.get(pair.first).second));
    JsonOp<?> transformedParentOp = new JsonOp(parentOp.getPath(), op);
    return serverIsParent ? Pair.of((JsonOp<T>) transformedParentOp, JsonOp.NO_OP) : Pair.of(
        (JsonOp<T>) JsonOp.NO_OP, transformedParentOp);
  }
}