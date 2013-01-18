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
import com.goodow.wind.model.op.OpSink;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;

class Context {
  static JValue create(JsonValue snapshot) {
    return create(snapshot.getType(), new Context(snapshot), Path.of());
  }

  private static JValue create(JsonType type, Context ctx, Path path) {
    switch (type) {
      case NULL:
        return new JNull(ctx, path);
      case BOOLEAN:
        return new JBoolean(ctx, path);
      case STRING:
        return new JString(ctx, path);
      case NUMBER:
        return new JNumber(ctx, path);
      case OBJECT:
        return new JObject(ctx, path);
      case ARRAY:
        return new JArray(ctx, path);
      default:
        throw new IllegalStateException("Unknown type: " + type.name());
    }
  }

  private JsonValue snapshot;
  private String key;
  private OpSink<JsonOp<?>> outputSink;
  private JsonHandlerRegistry registry;

  private Context(JsonValue snapshot) {
    // assert snapshot != null;
    this.snapshot = snapshot;
  }

  @SuppressWarnings("unchecked")
  <T extends JValue> T at(Path path) {
    JsonValue val = get(path);
    if (val == null) {
      return null;
    }
    return (T) create(val.getType(), this, path);
  }

  void fireEventToParent(Path path) {
    Path parent = path.getParent();
    while (parent != null) {
      if (path.isIndexed()) {
        ArrayOf<ArrayHandler> handlers = getHandlers(parent);
        if (handlers != null && !handlers.isEmpty()) {
          for (int i = 0, len = handlers.length(); i < len; i++) {
            ArrayHandler handler = handlers.get(i);
            handler.setValue(at(parent));
            handler.onChildChanged(path.getIndex());
          }
        }
      } else {
        ArrayOf<ObjectHandler> handlers = getHandlers(path.getParent());
        if (handlers != null && !handlers.isEmpty()) {
          for (int i = 0, len = handlers.length(); i < len; i++) {
            ObjectHandler handler = handlers.get(i);
            handler.setValue(at(parent));
            handler.onChildChanged(path.getKey());
          }
        }
      }
      path = parent;
      parent = parent.getParent();
    }
  }

  JsonValue get(Path path) {
    if (path.toString().isEmpty()) {
      return snapshot;
    }
    JsonValue toRtn = snapshot;
    String[] keys = path.toString().split("\\.");
    for (String subPath : keys) {
      int idx = subPath.indexOf('[');
      if (idx == -1) {
        assert toRtn instanceof JsonObject;
        toRtn = ((JsonObject) toRtn).get(subPath);
        if (toRtn == null) {
          return null;
        }
        continue;
      }
      if (idx != 0) {
        assert toRtn instanceof JsonObject;
        toRtn = ((JsonObject) toRtn).get(subPath.substring(0, idx));
        if (toRtn == null) {
          return null;
        }
      }
      String[] split = subPath.substring(idx).split("[\\[\\]]");
      assert split.length > 1 && split.length % 2 == 0;
      for (int i = 1, len = split.length; i < len; i += 2) {
        assert split[i - 1].isEmpty();
        assert toRtn instanceof JsonArray;
        toRtn = ((JsonArray) toRtn).get(Integer.parseInt(split[i]));
        if (toRtn == null) {
          return null;
        }
      }
    }
    return toRtn;
  }

  @SuppressWarnings("unchecked")
  <T extends JValue.JsonHandler> ArrayOf<T> getHandlers(Path path) {
    assert registry != null;
    return (ArrayOf<T>) registry.getHandlers(key, path);
  }

  HandlerRegistration registerHandler(Path path, final JValue.JsonHandler handler) {
    if (registry == null) {
      registry = JsonHandlerRegistry.ROOT.createExtension();
    }
    return registry.registerHandler(key, path, handler);
  }

  Context setKey(String key) {
    this.key = key;
    return this;
  }

  Context setOutputSink(OpSink<JsonOp<?>> outputSink) {
    this.outputSink = outputSink;
    return this;
  }

  Context setRegistry(JsonHandlerRegistry registry) {
    this.registry = registry;
    return this;
  }

  void setValue(Path path, JsonValue val) {
    Path parent = path.getParent();
    if (parent == null) {
      snapshot = val;
      return;
    }
    JsonValue parentVal = get(parent);
    assert parentVal != null;
    if (path.isIndexed()) {
      ((JsonArray) parentVal).set(path.getIndex(), val);
    } else {
      ((JsonObject) parentVal).put(path.getKey(), val);
    }
  }

  boolean shouldFireEvent() {
    return registry != null;
  }

  void submitOp(JsonOp<?> op) {
    if (outputSink != null) {
      outputSink.consume(op);
    }
  }
}