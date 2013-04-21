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
package com.goodow.realtime;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

public class CollaborativeMapTest extends TestCase {
  Model mod;
  CollaborativeMap map;

  public void testClear() {
    map.set("k1", "v1");
    map.set("k2", "v2");
    map.clear();
    assertEquals(0, map.size());
  }

  public void testEventHandler() {
    map.addValueChangedListener(new EventHandler<ValueChangedEvent>() {

      @Override
      public void handleEvent(ValueChangedEvent event) {
        // TODO Auto-generated method stub

      }
    });
  }

  public void testIllegalArgumentException() {
    try {
      map.set(null, "");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testInitialize() {
    assertSame(map, mod.getObject(map.getId()));
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());

    JsonObject v4 = Json.createObject();
    v4.put("subKey", "subValue");
    map = mod.createMap("k1", "v1", "k2", 2, "k3", true, "k4", v4);
    assertEquals(4, map.size());
    assertEquals("v1", map.get("k1"));
    assertEquals(2d, (Double) map.get("k2"), 0d);
    assertEquals(true, map.get("k3"));
    assertTrue(v4.jsEquals((JsonValue) map.get("k4")));
    assertFalse(map.isEmpty());
  }

  public void testItems() {
    map.set("k1", "v1");
    map.set("k2", "v2");
    Object[][] items = map.items();
    assertEquals("k1", items[0][0]);
    assertEquals("v1", items[0][1]);
    assertEquals("k2", items[1][0]);
    assertEquals("v2", items[1][1]);
  }

  public void testRemove() {
    map.set("k1", "v1");
    map.remove("k1");
    map.remove("k2");
    assertEquals(0, map.size());
  }

  public void testSet() {
    JsonArray v4 = Json.createArray();
    v4.set(0, "abc");
    map.set("k1", "v1");
    map.set("k2", 4);
    map.set("k3", false);
    map.set("k4", v4);
    map.set("k5", null);
    map.set("k6", Json.createNull());

    assertEquals(4, map.size());
    assertEquals("v1", map.get("k1"));
    assertEquals(4d, (Double) map.get("k2"), 0d);
    assertEquals(false, map.get("k3"));
    assertTrue(v4.jsEquals((JsonValue) map.get("k4")));

    map.set("k1", "");
    map.set("k2", null);
    map.set("k3", Json.createNull());
    assertEquals(2, map.size());
    assertEquals("", map.get("k1"));
    assertFalse(map.has("k2"));
    assertFalse(map.has("k3"));
  }

  public void testValues() {
    map.set("k1", "v1");
    map.set("k2", "v2");
    Object[] values = map.values();
    assertEquals("v1", values[0]);
    assertEquals("v2", values[1]);
  }

  @Override
  protected void setUp() throws Exception {
    DocumentBridge bridge = new DocumentBridge();
    Document doc = bridge.create(Json.createArray());
    mod = doc.getModel();
    map = mod.createMap();
  }
}
