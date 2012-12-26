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
package com.goodow.wind.model.op.map;

import com.goodow.wind.model.op.ComposeException;
import com.goodow.wind.model.op.TransformException;
import com.goodow.wind.model.util.Pair;

import junit.framework.TestCase;

public class MapOpTest extends TestCase {
  private static final class ReversibleTestParameters extends TestParameters {
    ReversibleTestParameters(MapOp<String> serverOp, MapOp<String> clientOp,
        MapOp<String> transformedServerOp, MapOp<String> transformedClientOp) {
      super(serverOp, clientOp, transformedServerOp, transformedClientOp);
    }

    @Override
    void run() {
      singleTest(serverOp, clientOp, transformedServerOp, transformedClientOp);
      singleTest(clientOp, serverOp, transformedClientOp, transformedServerOp);
    }
  }

  private static class TestParameters {
    final MapOp<String> serverOp;
    final MapOp<String> clientOp;
    final MapOp<String> transformedServerOp;
    final MapOp<String> transformedClientOp;

    TestParameters(MapOp<String> serverOp, MapOp<String> clientOp,
        MapOp<String> transformedServerOp, MapOp<String> transformedClientOp) {
      this.serverOp = serverOp;
      this.clientOp = clientOp;
      this.transformedServerOp = transformedServerOp;
      this.transformedClientOp = transformedClientOp;
    }

    void run() {
      singleTest(serverOp, clientOp, transformedServerOp, transformedClientOp);
    }
  }

  private static void singleTest(MapOp<String> serverOp, MapOp<String> clientOp,
      MapOp<String> transformedServerOp, MapOp<String> transformedClientOp) {
    Pair<MapOp<String>, MapOp<String>> pair = serverOp.transformWith(clientOp);
    assertEquals(transformedServerOp, pair.first);
    assertEquals(transformedClientOp, pair.second);
  }

  public void testComposeDifferentKey() {
    MapOp<String> op = new MapOp<String>().update("a", null, "new a");
    op = op.composeWith(new MapOp<String>().update("b", "old b", null));
    MapOp<String> expected =
        new MapOp<String>().update("a", null, "new a").update("b", "old b", null);
    assertEquals(expected, op);
  }

  public void testComposeException() {
    try {
      new MapOp<String>().update("a", "", "should same").composeWith(
          new MapOp<String>().update("a", "should same but diff", ""));
      fail();
    } catch (ComposeException e) {
      // ok
    }
  }

  public void testComposeNoOp() {
    MapOp<String> op = new MapOp<String>().update("a", "initial", "should same");
    op = op.composeWith(new MapOp<String>().update("a", "should same", "initial"));
    assertTrue(op.isNoOp());

    op = new MapOp<String>().update("a", "same", "same");
    assertTrue(op.isNoOp());
  }

  public void testComposeSameKey() {
    MapOp<String> op = new MapOp<String>().update("a", "old a", "should same");
    op = op.composeWith(new MapOp<String>().update("a", "should same", "new b"));
    MapOp<String> expected = new MapOp<String>().update("a", "old a", "new b");
    assertEquals(expected, op);
  }

  public void testInvert() {
    MapOp<Object> op =
        new MapOp<Object>().update("a", null, "new a").update("b", "old b", null).update("c",
            "old c", "new c");
    MapOp<Object> expected =
        new MapOp<Object>().update("a", "new a", null).update("b", null, "old b").update("c",
            "new c", "old c");
    assertEquals(expected, op.invert());
    assertTrue(op.composeWith(op.invert()).isNoOp());
  }

  public void testInvertNoOp() {
    assertEquals(new MapOp<Object>(), new MapOp<String>().invert());
  }

  public void testParseFromJson() {
    MapOp<?> op =
        new MapOp<Object>().update("a", "old a", null).update("b", false, 1f).update("c",
            Boolean.TRUE, -2.2);
    assertEquals(op, new MapOp<Object>(op.toString()));
  }

  public void testTransformDifferentKey() {
    new ReversibleTestParameters(new MapOp<String>().update("a", "initial", "new a"),
        new MapOp<String>().update("b", "initial", "new b"), new MapOp<String>().update("a",
            "initial", "new a"), new MapOp<String>().update("b", "initial", "new b")).run();
  }

  public void testTransformException() {
    try {
      new MapOp<String>().update("a", "should same", "new a").transformWith(
          new MapOp<String>().update("a", "should same but diff", "new b"));
      fail();
    } catch (TransformException e) {
      // ok
    }
  }

  public void testTransformNoOp() {
    new ReversibleTestParameters(new MapOp<String>().update("a", "initial", "new a"),
        new MapOp<String>(), new MapOp<String>().update("a", "initial", "new a"),
        new MapOp<String>()).run();
  }

  public void testTransformSameKey() {
    new TestParameters(new MapOp<String>().update("a", "initial", "new a"), new MapOp<String>()
        .update("a", "initial", "new b"), new MapOp<String>(), new MapOp<String>().update("a",
        "new a", "new b")).run();
    new TestParameters(new MapOp<String>().update("a", "initial", "new a"), new MapOp<String>()
        .update("a", "initial", "new a"), new MapOp<String>(), new MapOp<String>()).run();
  }
}
