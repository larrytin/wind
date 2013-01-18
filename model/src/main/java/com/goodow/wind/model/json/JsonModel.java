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

import com.goodow.wind.model.json.Visitor.InitializeVisitor;
import com.goodow.wind.model.op.Model;
import com.goodow.wind.model.op.OpSink;
import com.goodow.wind.model.op.list.algorithm.ListOp;
import com.goodow.wind.model.op.list.algorithm.ListOpCollector;
import com.goodow.wind.model.util.Pair;

import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;
import elemental.util.Collections;

public class JsonModel implements Model<JsonOp<?>, JsonValue> {
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public ArrayOf<JsonOp<?>> compose(ArrayOf<JsonOp<?>> ops) {
    if (ops.isEmpty()) {
      return ops;
    }
    ArrayOf<JsonOp<?>> toRtn = Collections.arrayOf();
    String currentPath = null;
    ListOpCollector<?> collector = null;
    for (int i = 0, len = ops.length(); i < len; i++) {
      JsonOp op = ops.get(i);
      assert !op.isNoOp();
      if (!op.getPath().toString().equals(currentPath)) {
        if (collector != null) {
          composeListOps(toRtn, collector);
          collector = null;
        }
        toRtn.push(op);
      } else {
        assert toRtn.peek().getType() == op.getType();
        if (op.getOp() instanceof ListOp) {
          if (collector == null) {
            collector = ((ListOp) op.getOp()).createOpCollector();
            collector.add((ListOp) toRtn.peek().getOp());
          }
          collector.add((ListOp) op.getOp());
        } else {
          JsonOp<?> composition = toRtn.pop().composeWith(op);
          if (!composition.isNoOp()) {
            toRtn.push(composition);
          }
        }
      }
      currentPath = op.getPath().toString();
    }
    if (collector != null) {
      composeListOps(toRtn, collector);
      collector = null;
    }
    return toRtn;
  }

  @Override
  public JValue create(JsonValue snapshot) {
    return Context.create(snapshot);
  }

  @Override
  public JsonOp<?> createOp(JsonValue delta) {
    return JsonOp.parse((JsonObject) delta);
  }

  public void init(JValue snapshot, String key, OpSink<JsonOp<?>> outputSink,
      JsonHandlerRegistry registry) {
    snapshot.ctx.setKey(key).setOutputSink(outputSink).setRegistry(registry);
    if (registry != null) {
      new InitializeVisitor().accept(snapshot);
    }
  }

  @Override
  public Pair<ArrayOf<JsonOp<?>>, ArrayOf<JsonOp<?>>> transform(ArrayOf<JsonOp<?>> serverOps,
      ArrayOf<JsonOp<?>> clientOps) {
    ArrayOf<JsonOp<?>> sOps = Collections.<JsonOp<?>> arrayOf().concat(serverOps);
    ArrayOf<JsonOp<?>> cOps = Collections.<JsonOp<?>> arrayOf().concat(clientOps);
    sLoop : for (int i = 0; i < sOps.length(); i++) {
      JsonOp<?> serverOp = sOps.get(i);
      assert !serverOp.isNoOp();
      for (int j = 0; j < cOps.length(); j++) {
        JsonOp<?> clientOp = cOps.get(j);
        assert !clientOp.isNoOp();
        Pair<? extends JsonOp<?>, ? extends JsonOp<?>> pair = serverOp.transformWith(clientOp);
        serverOp = pair.first;
        clientOp = pair.second;
        if (serverOp.isNoOp()) {
          sOps.removeByIndex(i--);
          if (clientOp.isNoOp()) {
            cOps.removeByIndex(j--);
          }
          continue sLoop;
        } else if (clientOp.isNoOp()) {
          cOps.removeByIndex(j--);
          continue;
        }
        cOps.set(j, clientOp);
      }
      sOps.set(i, serverOp);
    }
    return Pair.of(sOps, cOps);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void composeListOps(ArrayOf<JsonOp<?>> ops, ListOpCollector<?> collector) {
    ListOp<?> composition = collector.composeAll();
    if (!composition.isNoOp()) {
      ops.set(ops.length() - 1, new JsonOp(ops.peek().getPath(), composition));
    } else {
      ops.removeByIndex(ops.length() - 1);
    }
  }
}