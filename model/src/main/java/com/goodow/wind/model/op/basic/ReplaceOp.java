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
import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.op.TransformException;
import com.goodow.wind.model.util.Pair;
import com.goodow.wind.model.util.Serializer;

import elemental.json.Json;
import elemental.json.JsonArray;

public class ReplaceOp<T> implements Op<ReplaceTarget<T>>, ReplaceTarget<T> {
  public static final String TYPE = "x";
  private T oldValue;
  private T newValue;
  private final Serializer<T> serializer;

  public ReplaceOp() {
    this(null);
  }

  @SuppressWarnings("unchecked")
  public ReplaceOp(Serializer<T> serializer) {
    this.serializer = serializer == null ? (Serializer<T>) Serializer.PRIMITIVE : serializer;
  }

  public ReplaceOp(String json, Serializer<T> serializer) {
    this(serializer);
    JsonArray op = Json.instance().parse(json);
    assert op.length() == 2;
    replace(this.serializer.fromJson(op.get(0)), this.serializer.fromJson(op.get(1)));
  }

  @Override
  public void apply(ReplaceTarget<T> target) {
    if (!isNoOp()) {
      target.replace(oldValue, newValue);
    }
  }

  @Override
  public ReplaceOp<T> composeWith(Op<ReplaceTarget<T>> op) {
    assert op instanceof ReplaceOp;
    ReplaceOp<T> o = (ReplaceOp<T>) op;
    return newInstance().replace(oldValue, newValue).replace(o.oldValue, o.newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ReplaceOp)) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    ReplaceOp other = (ReplaceOp) obj;
    if (newValue == null) {
      if (other.newValue != null) {
        return false;
      }
    } else if (!newValue.equals(other.newValue)) {
      return false;
    }
    if (oldValue == null) {
      if (other.oldValue != null) {
        return false;
      }
    } else if (!oldValue.equals(other.oldValue)) {
      return false;
    }
    return true;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
    result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
    return result;
  }

  @Override
  public ReplaceOp<T> invert() {
    return newInstance().replace(newValue, oldValue);
  }

  @Override
  public boolean isNoOp() {
    return oldValue == null && newValue == null;
  }

  @Override
  public ReplaceOp<T> replace(T oldValue, T newValue) {
    if (serializer.areEqual(oldValue, newValue)) {
      return this;
    }
    if (isNoOp()) {
      this.oldValue = oldValue;
      this.newValue = newValue;
      return this;
    }
    if (!serializer.areEqual(this.newValue, oldValue)) {
      throw new ComposeException("Mismatched value: attempt to compose " + toString() + " with "
          + "[" + serializer.toString(oldValue) + "," + serializer.toString(newValue) + "]");
    }
    if (serializer.areEqual(this.oldValue, newValue)) {
      this.oldValue = null;
      this.newValue = null;
    } else {
      this.newValue = newValue;
    }
    return this;
  }

  @Override
  public String toString() {
    return "[" + serializer.toString(oldValue) + "," + serializer.toString(newValue) + "]";
  }

  @Override
  public Pair<ReplaceOp<T>, ReplaceOp<T>> transformWith(Op<?> clientOp) {
    assert clientOp instanceof ReplaceOp;
    @SuppressWarnings("unchecked")
    ReplaceOp<T> op = (ReplaceOp<T>) clientOp;
    if (isNoOp()) {
      return Pair.of(newInstance(), newInstance().composeWith(op));
    } else if (op.isNoOp()) {
      return Pair.of(newInstance().composeWith(this), newInstance());
    }
    if (!serializer.areEqual(oldValue, op.oldValue)) {
      throw new TransformException("Mismatched initial value: attempt to transform " + toString()
          + " with " + op.toString());
    }
    return Pair.of(newInstance(), newInstance().replace(this.newValue, op.newValue));
  }

  private ReplaceOp<T> newInstance() {
    return new ReplaceOp<T>(serializer);
  }
}