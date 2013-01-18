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

import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.op.list.StringOp;
import com.goodow.wind.model.util.Pair;

import junit.framework.TestCase;

import elemental.json.Json;

public class JsonOpTest extends TestCase {
  @SuppressWarnings("rawtypes")
  JsonOp serverOp;
  Op<?> op1;
  Op<?> op2;
  Op<?> op3;

  public void testParseFromJson() {
    assertEquals(serverOp, JsonOp.parse(Json.parse(serverOp.toString())));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testTransformWith() {
    // different path
    JsonOp clientOp = new JsonOp(Path.of("diffPath"), op2);
    Pair<JsonOp, JsonOp> pair = serverOp.transformWith(clientOp);
    assertEquals(serverOp, pair.first);
    assertEquals(clientOp, pair.second);

    // same path
    clientOp = new JsonOp(Path.of(), op2);
    pair = serverOp.transformWith(clientOp);
    assertTrue(pair.first.isNoOp());
    JsonOp transformedClientOp =
        new JsonOp(Path.of(), new ObjectOp().update("a", Json.create("value1"), Json
            .create("value2")));
    assertEquals(transformedClientOp, pair.second);

    // embedded path
    clientOp = new JsonOp(Path.of("a"), op3);
    pair = serverOp.transformWith(clientOp);
    JsonOp transfromedServerOp =
        new JsonOp(Path.of(), new ObjectOp().update("a", Json.create("str"), Json.create("value1")));
    assertEquals(transfromedServerOp, pair.first);
    assertTrue(pair.second.isNoOp());

    pair = clientOp.transformWith(serverOp);
    assertTrue(pair.first.isNoOp());
    assertEquals(transfromedServerOp, pair.second);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected void setUp() throws Exception {
    op1 = new ObjectOp().update("a", Json.create("string"), Json.create("value1"));
    op2 = new ObjectOp().update("a", Json.create("string"), Json.create("value2"));
    op3 = new StringOp().retain(3).delete("ing");
    serverOp = new JsonOp(Path.of(), op1);
  }
}