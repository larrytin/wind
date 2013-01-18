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
package com.goodow.wind.model.op.basic;

import com.goodow.wind.model.op.ComposeException;
import com.goodow.wind.model.op.TransformException;
import com.goodow.wind.model.util.Pair;

import junit.framework.TestCase;

public class ReplaceOpTest extends TestCase {

  public void testComposeExceptions() {
    try {
      new ReplaceOp<Object>().replace(null, "a").replace("b", true);
      fail();
    } catch (ComposeException e) {
      // ok
    }
  }

  public void testComposeNoOp() {
    ReplaceOp<Object> op =
        new ReplaceOp<Object>().replace(5, 5).replace("", "").replace(null, null);
    assertTrue(op.isNoOp());

    op.replace("a", 4).replace(4, true).replace(true, "a");
    assertTrue(op.isNoOp());
  }

  public void testComposeSimple() {
    ReplaceOp<Object> op = new ReplaceOp<Object>().replace(null, "a").replace("a", true);
    assertEquals(new ReplaceOp<Object>().replace(null, true), op);
  }

  public void testInvertNoOp() {
    ReplaceOp<Object> op = new ReplaceOp<Object>().invert();
    assertEquals(new ReplaceOp<Object>(), op);
  }

  public void testInvertSimple() {
    ReplaceOp<Object> op = new ReplaceOp<Object>().replace(null, 5).invert();
    assertEquals(new ReplaceOp<Object>().replace(5, null), op);
  }

  public void testParseFromJson() {
    ReplaceOp<Object> op = new ReplaceOp<Object>().replace(null, "a");
    assertEquals(op, new ReplaceOp<Object>(op.toString(), null));
    op = new ReplaceOp<Object>().replace(-5.3d, true);
    assertEquals(op, new ReplaceOp<Object>(op.toString(), null));
  }

  public void testTransformException() {
    try {
      new ReplaceOp<Object>().replace("a", "b").transformWith(
          new ReplaceOp<Object>().replace("b", "a"));
      fail();
    } catch (TransformException e) {
      // ok
    }
  }

  public void testTransformNoOp() {
    // no-op vs no-op
    Pair<ReplaceOp<Object>, ReplaceOp<Object>> pair =
        new ReplaceOp<Object>().replace(null, null).transformWith(
            new ReplaceOp<Object>().replace(true, true));
    assertTrue(pair.first.isNoOp());
    assertTrue(pair.second.isNoOp());

    // op vs no-op
    ReplaceOp<Object> serverOp = new ReplaceOp<Object>().replace(null, 5);
    pair = serverOp.transformWith(new ReplaceOp<Object>());
    assertEquals(serverOp, pair.first);
    assertTrue(pair.second.isNoOp());

    // no-op vs op
    ReplaceOp<Object> clientOp = new ReplaceOp<Object>().replace(null, 5);
    pair = new ReplaceOp<Object>().transformWith(clientOp);
    assertTrue(pair.first.isNoOp());
    assertEquals(clientOp, pair.second);
  }

  public void testTransformSimple() {
    Pair<ReplaceOp<Object>, ReplaceOp<Object>> pair =
        new ReplaceOp<Object>().replace(null, 5).transformWith(
            new ReplaceOp<Object>().replace(null, "a"));
    assertTrue(pair.first.isNoOp());
    assertEquals(new ReplaceOp<Object>().replace(5, "a"), pair.second);
  }
}
