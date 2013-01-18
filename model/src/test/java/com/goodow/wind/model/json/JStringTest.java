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

public class JStringTest extends TestCase {

  @SuppressWarnings("cast")
  public void testInsertDelete() {
    JString str = new JString("");

    assertEquals("", str.getString());
    final String[] s = new String[2];
    str.on(new StringHandler() {

      @Override
      public void onDelete(int position, String text) {
        s[1] = text;
        assertEquals(2, 2);
      }

      @Override
      public void onInsert(int position, String text) {
        s[0] = text;
        assertEquals(0, position);
      }
    });
    str.insert(0, "abcd");
    str.delete(2, 2);
    assertEquals("abcd", (String) s[0]);
    assertEquals("cd", (String) s[1]);
    assertEquals("ab", str.getString());
  }
}