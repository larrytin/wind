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

import com.goodow.wind.model.op.list.algorithm.ListHelper;
import com.goodow.wind.model.op.list.algorithm.ListNormalizer;
import com.goodow.wind.model.op.list.algorithm.ListNormalizer.Appender;
import com.goodow.wind.model.op.list.algorithm.ListOp;
import com.goodow.wind.model.op.list.algorithm.ListTarget;
import com.goodow.wind.model.util.Pair;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

class ArrayOp extends ListOp<JsonArray> {
  private static class ArrayAppender implements Appender<JsonArray> {
    private JsonArray array = Json.createArray();

    @Override
    public void append(JsonArray list) {
      for (int i = 0, len = list.length(); i < len; i++) {
        array.set(array.length(), list.get(i));
      }
    }

    @Override
    public JsonArray flush() {
      try {
        return array;
      } finally {
        array = Json.createArray();
      }
    }
  }
  private static class ArrayHelper implements ListHelper<JsonArray> {
    @Override
    public ListNormalizer<JsonArray> createNormalizer() {
      return new ArrayNormalizer();
    }

    @Override
    public int length(JsonArray list) {
      return list.length();
    }

    @Override
    public ListOp<JsonArray> newOp() {
      return new ArrayOp();
    }

    @Override
    public boolean startsWith(JsonArray list, JsonArray prefix) {
      assert list.length() >= prefix.length();
      for (int i = 0, len = prefix.length(); i < len; i++) {
        if ((prefix.get(i) == null && list.get(i) != null)
            || (prefix.get(i) != null && list.get(i) == null)
            || !prefix.get(i).toJson().equals(list.get(i).toJson())) {
          return false;
        }
      }
      return true;
    }

    @Override
    public JsonArray subset(JsonArray list, int beginIdx) {
      return subset(list, beginIdx, list.length());
    }

    @Override
    public JsonArray subset(JsonArray list, int beginIdx, int endIdx) {
      JsonArray array = Json.createArray();
      for (int i = beginIdx; i < endIdx; i++) {
        array.set(i - beginIdx, list.get(i));
      }
      return array;
    }
  }
  private static class ArrayNormalizer extends ListNormalizer<JsonArray> {
    protected ArrayNormalizer() {
      super(new ArrayOp(), new ArrayAppender());
    }

    @Override
    protected boolean isEmpty(JsonArray list) {
      return list.length() == 0;
    }
  }

  ArrayOp() {
  }

  ArrayOp(boolean isInsert, int idx, JsonArray list, int initLength) {
    super(isInsert, idx, list, initLength);
  }

  ArrayOp(String json) {
    super(json);
  }

  @Override
  protected ListHelper<JsonArray> createListHelper() {
    return new ArrayHelper();
  }

  @Override
  protected JsonArray fromJson(JsonValue json) {
    return (JsonArray) json;
  }

  @Override
  protected String toJson(JsonArray list) {
    return list.toJson();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  <T> Pair<JsonOp<T>, ? extends JsonOp<?>> transformWithChild(boolean serverIsParent,
      JsonOp<?> parentOp, final JsonOp<?> childOp) {
    final Pair<Integer, String> pair = childOp.getPath().nextIndex(parentOp.getPath().toString());
    final int[] cursor = new int[2];
    final boolean[] isDelete = new boolean[1];
    apply(new ListTarget<JsonArray>() {
      @Override
      public ListTarget<JsonArray> delete(JsonArray list) {
        if (cursor[0] != -1) {
          cursor[0] += list.length();
          cursor[1] -= list.length();
          if (cursor[0] > pair.first) {
            isDelete[0] = true;
            cursor[0] = -1;
          }
        }
        return null;
      }

      @Override
      public ListTarget<JsonArray> insert(JsonArray list) {
        cursor[1] += list.length();
        return null;
      }

      @Override
      public ListTarget<JsonArray> retain(int length) {
        if (cursor[0] != -1) {
          cursor[0] += length;
          if (cursor[0] > pair.first) {
            isDelete[0] = false;
            cursor[0] = -1;
          }
        }
        return null;
      }
    });
    assert cursor[0] == -1;
    if (!isDelete[0]) {
      if (cursor[1] == 0) {
        return serverIsParent ? Pair.of((JsonOp<T>) parentOp, childOp) : Pair.of(
            (JsonOp<T>) childOp, parentOp);
      } else {
        JsonOp<?> transformedChildOp =
            new JsonOp(childOp.getPath().getParent().at(pair.first + cursor[1]), childOp.getOp());
        return serverIsParent ? Pair.of((JsonOp<T>) parentOp, transformedChildOp) : Pair.of(
            (JsonOp<T>) transformedChildOp, parentOp);
      }
    }
    cursor[0] = 0;
    final ArrayOp op = new ArrayOp();
    apply(new ListTarget<JsonArray>() {
      @Override
      public ListTarget<JsonArray> delete(JsonArray list) {
        cursor[0] += list.length();
        if (cursor[0] > pair.first) {
          JsonArray array = Json.createArray();
          for (int i = 0, len = list.length(); i < len; i++) {
            if (cursor[0] - list.length() + i == pair.first) {
              JValue val = new JsonModel().create(Json.instance().parse(list.get(i).toJson()));
              val.consume(new JsonOp(Path.of(pair.second), childOp.getOp()));
              array.set(i, val.getValue());
            } else {
              array.set(i, list.get(i));
            }
          }
          list = array;
        }
        op.delete(list);
        return null;
      }

      @Override
      public ListTarget<JsonArray> insert(JsonArray list) {
        op.insert(list);
        return null;
      }

      @Override
      public ListTarget<JsonArray> retain(int length) {
        cursor[0] += length;
        op.retain(length);
        return null;
      }
    });
    JsonOp<?> transformedParentOp = new JsonOp(parentOp.getPath(), op);
    return serverIsParent ? Pair.of((JsonOp<T>) transformedParentOp, JsonOp.NO_OP) : Pair.of(
        (JsonOp<T>) JsonOp.NO_OP, transformedParentOp);
  }
}