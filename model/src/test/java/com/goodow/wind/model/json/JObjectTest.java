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

import junit.framework.TestCase;

import elemental.json.JsonNumber;
import elemental.json.JsonString;
import elemental.json.JsonValue;

public class JObjectTest extends TestCase {

  public void testAsObject() {
    JObject obj = new JObject();
    final Object[] o = new Object[1];
    obj.on(new ObjectHandler() {

      @Override
      public void onAdded(String key, JValue val) {
        assertEquals(o[0], key);
        o[0] = val;
      }

      @Override
      public void onChildChanged(String key) {
        o[0] = key;
      }

      @Override
      public void onRemoved(String key, JsonValue val) {
        fail();
      }

      @Override
      public void onUpdated(String key, JsonValue oldVal, JValue newVal) {
        fail();
      }
    });
    o[0] = "a";
    JObject asObject = obj.asObject("a");
    assertEquals(new JObject().toString(), ((JObject) o[0]).toString());

    asObject.put("subKey", "subVal");
    assertEquals("a", o[0]);
    assertEquals("subVal", ((JString) obj.at(Path.of("a.subKey"))).getString());
  }

  public void testPutRemove() {
    JObject obj = new JObject();

    final Object[] o = new Object[2];
    obj.on(new ObjectHandler() {

      @Override
      public void onAdded(String key, JValue val) {
        assertEquals(o[0], key);
        o[0] = val;
      }

      @Override
      public void onChildChanged(String key) {
        fail();
      }

      @Override
      public void onRemoved(String key, JsonValue val) {
        assertEquals(o[0], key);
        o[0] = val;
      }

      @Override
      public void onUpdated(String key, JsonValue oldVal, JValue newVal) {
        assertEquals(o[0], key);
        o[0] = oldVal;
        o[1] = newVal;
      }
    });
    // Add number
    o[0] = "a";
    obj.put("a", 5);
    assertEquals(1, obj.keys().length);
    assertEquals(5, obj.getNumber("a"), 0);
    assertEquals(5, ((JNumber) o[0]).getNumber(), 0);

    // update number to str
    o[0] = "a";
    obj.put("a", "abcde");
    assertEquals(1, obj.keys().length);
    assertEquals("abcde", obj.getString("a"));
    assertEquals(5, ((JsonNumber) o[0]).getNumber(), 0);
    assertEquals("abcde", ((JString) o[1]).getString());

    // remove str
    o[0] = "a";
    obj.remove("a");
    assertEquals(0, obj.keys().length);
    assertEquals("abcde", ((JsonString) o[0]).getString());
  }
}