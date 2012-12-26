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
package com.goodow.wind.model.op.number;

import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.util.Pair;

public class NumberOp implements Op<NumberTarget>, NumberTarget {
  public static final String TYPE = "n";
  private double num;

  @Override
  public NumberOp add(double num) {
    this.num += num;
    return this;
  }

  @Override
  public void apply(NumberTarget target) {
    if (!isNoOp()) {
      target.add(num);
    }
  }

  @Override
  public NumberOp composeWith(Op<NumberTarget> op) {
    assert op instanceof NumberOp;
    return new NumberOp().add(num).add(((NumberOp) op).num);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NumberOp)) {
      return false;
    }
    return num == ((NumberOp) obj).num;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public NumberOp invert() {
    return new NumberOp().add(-num);
  }

  @Override
  public boolean isNoOp() {
    return num == 0;
  }

  @Override
  public String toString() {
    return "" + num;
  }

  @Override
  public Pair<NumberOp, NumberOp> transformWith(Op<?> clientOp) {
    assert clientOp instanceof NumberOp;
    double clientNum = ((NumberOp) clientOp).num;
    return Pair.of(new NumberOp().add(num - clientNum), new NumberOp().add(clientNum - num));
  }
}