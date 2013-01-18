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

import com.goodow.wind.model.op.number.NumberOp;
import com.goodow.wind.model.util.Pair;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;

public class ArrayOpTest extends TestCase {
  JsonOp<?> parentOp;
  ArrayOp op;
  private JsonArray array1;
  private JsonArray array2;

  public void testParseFromJson() {
    assertEquals(op, new ArrayOp(op.toString()));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testTransformWithChild() {
    // child idx on parent retain
    JsonOp<?> childOp = new JsonOp(Path.of().at(3), new NumberOp().add(-3));
    // server as parent vs client as child
    Pair<JsonOp<Object>, ? extends JsonOp<?>> pair = op.transformWithChild(true, parentOp, childOp);
    assertEquals(parentOp, pair.first);
    assertEquals(childOp, pair.second);
    // server as child vs client as parent
    pair = op.transformWithChild(false, parentOp, childOp);
    assertEquals(childOp, pair.first);
    assertEquals(parentOp, pair.second);

    // child idx on parent retain
    childOp = new JsonOp(Path.of().at(1), new NumberOp().add(-3));
    // server as parent vs client as child
    JsonOp transformedChildOp = new JsonOp(Path.of().at(2), new NumberOp().add(-3));
    pair = op.transformWithChild(true, parentOp, childOp);
    assertEquals(parentOp, pair.first);
    assertEquals(transformedChildOp, pair.second);
    // server as child vs client as parent
    pair = op.transformWithChild(false, parentOp, childOp);
    assertEquals(transformedChildOp, pair.first);
    assertEquals(parentOp, pair.second);

    // child idx on parent delete
    childOp = new JsonOp(Path.of().at(2), new NumberOp().add(-3));
    JsonArray array3 = Json.createArray();
    array3.set(0, 7);
    JsonOp transformedParentOp =
        new JsonOp(Path.of(), new ArrayOp().retain(1).insert(array1).retain(1).delete(array3)
            .retain(1));
    // server as parent vs client as child
    pair = op.transformWithChild(true, parentOp, childOp);
    assertEquals(transformedParentOp, pair.first);
    assertTrue(pair.second.isNoOp());
    // server as child vs client as parent
    pair = op.transformWithChild(false, parentOp, childOp);
    assertTrue(pair.first.isNoOp());
    assertEquals(transformedParentOp, pair.second);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  protected void setUp() throws Exception {
    array1 = Json.createArray();
    array1.set(0, 5);
    array2 = Json.createArray();
    array2.set(0, 10);
    op = (ArrayOp) new ArrayOp().retain(1).insert(array1).retain(1).delete(array2).retain(1);
    parentOp = new JsonOp(Path.of(), op);
  }
}