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

import elemental.util.ArrayOf;
import elemental.util.Collections;
import elemental.util.MapFromStringTo;

public class JsonHandlerRegistry {
  public static final JsonHandlerRegistry ROOT = new JsonHandlerRegistry();

  private static final String SEP = "/";
  private MapFromStringTo<ArrayOf<JValue.JsonHandler>> handlers;

  private JsonHandlerRegistry() {
  }

  public JsonHandlerRegistry createExtension() {
    return new JsonHandlerRegistry();
  }

  public ArrayOf<JValue.JsonHandler> getHandlers(String key, Path path) {
    return handlers == null ? null : handlers.get(fullPath(key, path));
  }

  public HandlerRegistration registerHandler(final String key, final Path path,
      final JValue.JsonHandler handler) {
    final String fullPath = fullPath(key, path);
    if (handlers == null) {
      handlers = Collections.mapFromStringTo();
    }
    ArrayOf<JValue.JsonHandler> arrayOf = handlers.get(fullPath);
    if (arrayOf == null) {
      arrayOf = Collections.arrayOf();
      handlers.put(fullPath, arrayOf);
    } else {
      assert !arrayOf.contains(handler);
      assert handler.toString().equals(arrayOf.peek().toString());
    }
    arrayOf.push(handler);
    return new HandlerRegistration() {
      @Override
      public void removeHandler() {
        JsonHandlerRegistry.this.unregisterHandler(key, path, handler);
      }
    };
  }

  public void unregisterHandler(String key, Path path, JValue.JsonHandler handler) {
    unregisterHandler(fullPath(key, path), handler);

  }

  private String fullPath(final String key, final Path path) {
    return key + SEP + (path == null ? "" : path.toString());
  }

  private void unregisterHandler(String fullPath, JValue.JsonHandler handler) {
    if (handlers == null || handlers.get(fullPath) == null) {
      return;
    }
    ArrayOf<JValue.JsonHandler> arrayOf = handlers.get(fullPath);
    arrayOf.remove(handler);
    if (arrayOf.isEmpty()) {
      handlers.remove(fullPath);
      if (handlers.keys().isEmpty()) {
        handlers = null;
      }
    }
  }
}