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

import com.goodow.wind.model.op.list.StringOp;
import com.goodow.wind.model.op.number.NumberOp;
import com.goodow.wind.model.util.Pair;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonObject;

public class ObjectOpTest extends TestCase {

  JsonOp<?> parentOp;
  ObjectOp op;
  JsonObject obj;

  public void testParseFromJson() {
    assertEquals(op, new ObjectOp(op.toString()));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testTransformWithChild() {
    // different key
    JsonOp<?> childOp = new JsonOp(Path.of("diffKey"), new NumberOp().add(-3));
    // server as parent vs client as child
    Pair<JsonOp<Object>, ? extends JsonOp<?>> pair = op.transformWithChild(true, parentOp, childOp);
    assertEquals(parentOp, pair.first);
    assertEquals(childOp, pair.second);
    // server as child vs client as parent
    pair = op.transformWithChild(false, parentOp, childOp);
    assertEquals(childOp, pair.first);
    assertEquals(parentOp, pair.second);

    // object update vs child num op
    childOp = new JsonOp(Path.of("updateKey"), new NumberOp().add(-3));
    JsonOp<?> transformedParentOp =
        new JsonOp(Path.of(), new ObjectOp().update("updateKey", Json.create(2), Json.create(10))
            .update("deleteKey", obj, null));
    // server as parent vs client as child
    pair = op.transformWithChild(true, parentOp, childOp);
    assertEquals(transformedParentOp, pair.first);
    assertTrue(pair.second.isNoOp());
    // server child vs client parent
    pair = op.transformWithChild(false, parentOp, childOp);
    assertEquals(transformedParentOp, pair.second);
    assertTrue(pair.first.isNoOp());

    // object delete vs child str op
    childOp =
        new JsonOp(Path.of("deleteKey.strKey"), new StringOp().retain(2).delete("cd").retain(1));
    JsonObject object = Json.createObject();
    object.put("strKey", "abe");
    transformedParentOp =
        new JsonOp(Path.of(), new ObjectOp().update("updateKey", Json.create(5), Json.create(10))
            .update("deleteKey", object, null));
    // server as parent vs client as child
    pair = op.transformWithChild(true, parentOp, childOp);
    assertEquals(transformedParentOp, pair.first);
    assertTrue(pair.second.isNoOp());
    // server as child vs client as parent
    pair = op.transformWithChild(false, parentOp, childOp);
    assertEquals(transformedParentOp, pair.second);
    assertTrue(pair.first.isNoOp());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  protected void setUp() throws Exception {
    obj = Json.createObject();
    obj.put("strKey", "abcde");
    op =
        (ObjectOp) new ObjectOp().update("updateKey", Json.create(5), Json.create(10)).update(
            "deleteKey", obj, null);
    parentOp = new JsonOp(Path.of(), op);
  }
}