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
package com.goodow.wind.server.model;

import com.goodow.wind.model.json.JValue;
import com.goodow.wind.model.json.JsonModel;
import com.goodow.wind.model.json.JsonOp;
import com.goodow.wind.model.op.TransformException;
import com.goodow.wind.model.util.Pair;

import com.google.inject.Inject;
import com.google.walkaround.slob.shared.InvalidSnapshot;
import com.google.walkaround.slob.shared.SlobModel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;
import elemental.util.Collections;

public class JsonModelAdapter implements SlobModel {
  class JsonSlob implements Slob {
    private JValue json;

    JsonSlob(JsonValue json) {
      this.json = json == null ? null : model.create(json);
    }

    @Override
    public void apply(Delta<String> change) throws DeltaRejected {
      JsonOp<?> op;
      try {
        op = model.createOp(Json.parse(change.getPayload()));
      } catch (JsonException e) {
        throw new DeltaRejected("Malformed op: " + change, e);
      }
      try {
        if (json == null) {
          json = model.create(JsonOp.buildJsonFromInitialOp(op));
        } else {
          json.consume(op);
        }
      } catch (RuntimeException e) {
        throw new DeltaRejected("Invalid op: " + op, e);
      }
    }

    @Nullable
    @Override
    public String snapshot() {
      return json == null ? null : json.toString();
    }
  }

  private final JsonModel model;

  @Inject
  JsonModelAdapter(JsonModel model) {
    this.model = model;
  }

  @Override
  public Slob create(@Nullable String snapshot) throws InvalidSnapshot {
    if (snapshot == null) {
      return new JsonSlob(null);
    } else {
      try {
        return new JsonSlob(Json.instance().parse(snapshot));
      } catch (JsonException e) {
        throw new InvalidSnapshot(e);
      }
    }
  }

  @Override
  public List<String> transform(List<Delta<String>> clientOps, List<Delta<String>> serverOps)
      throws DeltaRejected {
    try {
      Pair<ArrayOf<JsonOp<?>>, ArrayOf<JsonOp<?>>> pair =
          model.transform(deserializeOps(serverOps), deserializeOps(clientOps));
      ArrayOf<JsonOp<?>> cOps = pair.second;
      ArrayList<String> toRtn = new ArrayList<String>(cOps.length());
      for (int i = 0, len = cOps.length(); i < len; i++) {
        toRtn.add(cOps.get(i).toString());
      }
      return toRtn;
    } catch (TransformException e) {
      throw new DeltaRejected(e);
    }
  }

  private ArrayOf<JsonOp<?>> deserializeOps(List<Delta<String>> changes) throws DeltaRejected {
    ArrayOf<JsonOp<?>> ops = Collections.arrayOf();
    for (int i = 0; i < changes.size(); i++) {
      JsonOp<?> op;
      try {
        op = model.createOp(Json.instance().create(changes.get(i).getPayload()));
      } catch (JsonException e) {
        throw new DeltaRejected(e);
      }
      ops.push(op);
    }
    return ops;
  }
}