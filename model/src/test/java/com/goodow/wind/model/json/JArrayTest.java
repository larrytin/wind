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
import elemental.json.JsonValue;

public class JArrayTest extends TestCase {

  public void testInsertDelete() {
    JArray array = new JArray();
    assertEquals(0, array.length());
    final Object[] a = new Object[1];
    array.on(new ArrayHandler() {
      @Override
      public void onChildChanged(int idx) {
        fail();
      }

      @Override
      public void onDelete(int idx, JsonValue val) {
        assertEquals(a[0], idx);
        a[0] = val;
      }

      @Override
      public void onInsert(int idx, JValue val) {
        assertEquals(a[0], idx);
        a[0] = val;
      }
    });
    // insert boolean at 0
    a[0] = 0;
    array.insert(0, true);
    assertEquals(1, array.length());
    assertEquals(true, array.getBoolean(0));
    assertEquals(true, ((JBoolean) a[0]).getBoolean());

    // insert str at 0
    a[0] = 0;
    array.insert(0, "abc");
    assertEquals(2, array.length());
    assertEquals("abc", array.getString(0));
    assertEquals(true, array.getBoolean(1));
    assertEquals("abc", ((JString) a[0]).getString());

    final double[] n = new double[1];
    array.on(Path.of().at(1), new NumberHandler() {
      @Override
      public void onAdd(double num) {
        n[0] = num;
      }
    });
    // insert number at 1
    a[0] = 1;
    array.insert(1, 5);
    assertEquals(3, array.length());
    assertEquals("abc", array.getString(0));
    assertEquals(5, array.getNumber(1), 0);
    assertEquals(true, array.getBoolean(2));
    assertEquals(5, ((JNumber) a[0]).getNumber(), 0);
    assertEquals(5, n[0], 0);

    // remove number at 1
    a[0] = 1;
    array.remove(1, 1);
    assertEquals(2, array.length());
    assertEquals("abc", array.getString(0));
    assertEquals(true, array.getBoolean(1));
    assertEquals(5, ((JsonNumber) a[0]).getNumber(), 0);
    assertEquals(-5, n[0], 0);
  }
}