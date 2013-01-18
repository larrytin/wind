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

import com.goodow.wind.model.util.Pair;

import junit.framework.TestCase;

public class PathTest extends TestCase {
  public void testAtKey() {
    assertEquals(Path.of("[5]").at("c"), Path.of("[5].c"));
    assertEquals(Path.of("").at(5).at("c"), Path.of("[5].c"));
  }

  public void testAtSubPath() {
    assertEquals(Path.of("").at(Path.of("")), Path.of());
    assertEquals(Path.of("[5]").at(Path.of("c")), Path.of("[5].c"));
    assertEquals(Path.of("").at(Path.of("[5]")), Path.of("[5]"));
  }

  public void testGetParent() {
    assertNull(Path.of("").getParent());
    assertSame(Path.of(), Path.of("a").getParent());
    assertEquals(Path.of(), Path.of("[1]").getParent());

    assertEquals("a", Path.of("a.b").getParent().toString());
    assertEquals("a", Path.of("a[1]").getParent().toString());
    assertEquals("[1]", Path.of("[1][2]").getParent().toString());
    assertEquals("[1]", Path.of("[1].b").getParent().toString());
  }

  public void testIsAncestorOf() {
    assertFalse(Path.of("").isAncestorOf(Path.of("")));
    assertFalse(Path.of("a").isAncestorOf(Path.of("")));
    assertTrue(Path.of("").isAncestorOf(Path.of("a")));

    assertTrue(Path.of("a").isAncestorOf(Path.of("a[1]")));
    assertTrue(Path.of("a").isAncestorOf(Path.of("a.b")));

    assertFalse(Path.of("a").isAncestorOf(Path.of("a")));
    assertFalse(Path.of("a[1]").isAncestorOf(Path.of("a")));
    assertFalse(Path.of("a.b").isAncestorOf(Path.of("a")));
  }

  public void testNextIndex() {
    assertNull(Path.of("").nextIndex(""));
    assertNull(Path.of("").nextIndex("a"));
    assertNull(Path.of("a").nextIndex(""));

    Pair<Integer, String> pair = Path.of("[1]").nextIndex("");
    assertEquals(1, pair.first.intValue());
    assertEquals("", pair.second);
    pair = Path.of("[1][2]").nextIndex("");
    assertEquals(1, pair.first.intValue());
    assertEquals("[2]", pair.second);

    pair = Path.of("[1][2].a[3]").nextIndex("[1]");
    assertEquals(2, pair.first.intValue());
    assertEquals("a[3]", pair.second);
    pair = Path.of("[1][2][3].a").nextIndex("[1]");
    assertEquals(2, pair.first.intValue());
    assertEquals("[3].a", pair.second);
  }

  public void testNextKey() {
    assertNull(Path.of("").nextKey(""));
    assertNull(Path.of("").nextKey("a"));
    assertNull(Path.of("[1]").nextKey(""));

    Pair<String, String> pair = Path.of("a").nextKey("");
    assertEquals("a", pair.first);
    assertEquals("", pair.second);
    pair = Path.of("a[1]").nextKey("");
    assertEquals("a", pair.first);
    assertEquals("[1]", pair.second);

    pair = Path.of("[1].a.b").nextKey("[1]");
    assertEquals("a", pair.first);
    assertEquals("b", pair.second);
    pair = Path.of("[1].a[2].b").nextKey("[1]");
    assertEquals("a", pair.first);
    assertEquals("[2].b", pair.second);
  }

  @Override
  protected void setUp() throws Exception {
    try {
      assert false;
      fail("Assertions not turned on");
    } catch (AssertionError e) {
      // ok.
    }
  }
}