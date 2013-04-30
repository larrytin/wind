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
package com.goodow.realtime.op;

import com.goodow.realtime.util.Pair;

/**
 * Represents an operation that can be applied to a target.
 * 
 * @param <T> type to which this operation can be applied
 * 
 */
public interface Op<T> {
  /**
   * Applies this operation to the given target
   * 
   * @param target target on which this operation applies itself
   */
  void apply(T target);

  Op<T> composeWith(Op<T> op);

  String getType();

  Op<T> invert();

  boolean isNoOp();

  @Override
  String toString();

  Pair<? extends Op<T>, ? extends Op<?>> transformWith(Op<?> clientOp);
}
