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

import elemental.util.ArrayOf;
import elemental.util.Collections;

public class JsonModelTest extends TestCase {
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testCompose() {
    ArrayOf<JsonOp<?>> ops = Collections.arrayOf();
    JsonOp op1 = new JsonOp(Path.of("s"), new StringOp().retain(2));
    ops.push(op1);
    ops.push(new JsonOp(Path.of("n"), new NumberOp().add(1)));
    ops.push(new JsonOp(Path.of("n"), new NumberOp().add(2)));
    ops.push(new JsonOp(Path.of("s"), new StringOp().insert("abc")));
    ops.push(new JsonOp(Path.of("s"), new StringOp().delete("abc")));
    ArrayOf<JsonOp<?>> composition = new JsonModel().compose(ops);
    assertEquals(2, composition.length());
    assertEquals(op1, composition.get(0));
    assertEquals(new JsonOp(Path.of("n"), new NumberOp().add(3)), composition.get(1));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testTransform() {
    ArrayOf<JsonOp<?>> serverOps = Collections.arrayOf();
    serverOps.push(new JsonOp(Path.of("s"), new StringOp().delete("abcde")));
    serverOps.push(new JsonOp(Path.of("n"), new NumberOp().add(5)));
    ArrayOf<JsonOp<?>> clientOps = Collections.arrayOf();
    clientOps.push(new JsonOp(Path.of("s"), new StringOp().delete("abc").retain(2)));
    clientOps.push(new JsonOp(Path.of("n"), new NumberOp().add(2)));
    Pair<ArrayOf<JsonOp<?>>, ArrayOf<JsonOp<?>>> pair =
        new JsonModel().transform(serverOps, clientOps);
    assertEquals(2, pair.first.length());
    assertEquals(new JsonOp(Path.of("s"), new StringOp().delete("de")), pair.first.get(0));
    assertEquals(new JsonOp(Path.of("n"), new NumberOp().add(3)), pair.first.get(1));

    assertEquals(1, pair.second.length());
    assertEquals(new JsonOp(Path.of("n"), new NumberOp().add(-3)), pair.second.get(0));
  }
}