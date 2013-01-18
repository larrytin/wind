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

public class ContextTest extends TestCase {
  public void testAtPath() {
    JObject obj = new JObject();
    assertNull(obj.at(Path.of("a")));
    obj.asArray("a").asObject(0).put("b", true);
    assertEquals(obj.getArray("a").getObject(0), obj.at(Path.of("a[0]")));
    assertEquals(obj.getArray("a").getObject(0).getBoolean("b"), ((JBoolean) obj.at(Path
        .of("a[0].b"))).getBoolean());

    JArray array = new JArray();
    array.insert(0, true).asObject(1).put("a", "abc");
    assertEquals(array.getObject(1), array.at(Path.of("[1]")));
    assertEquals("abc", ((JString) array.at(Path.of("[1].a"))).getString());
  }
}