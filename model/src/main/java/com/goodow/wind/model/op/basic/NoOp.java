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

import com.goodow.wind.model.op.Op;
import com.goodow.wind.model.util.Pair;

public class NoOp implements Op<Void> {
  public static final NoOp INSTANCE = new NoOp();

  private NoOp() {
  }

  @Override
  public void apply(Void target) {
  }

  @Override
  public Op<Void> composeWith(Op<Void> op) {
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == INSTANCE;
  }

  @Override
  public String getType() {
    return "NoOp";
  }

  @Override
  public Op<Void> invert() {
    return INSTANCE;
  }

  @Override
  public boolean isNoOp() {
    return true;
  }

  @Override
  public String toString() {
    return "NoOp";
  }

  @Override
  public Pair<NoOp, ? extends Op<?>> transformWith(Op<?> clientOp) {
    throw new IllegalStateException();
  }
}