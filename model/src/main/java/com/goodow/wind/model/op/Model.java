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
package com.goodow.wind.model.op;

import com.goodow.wind.model.util.Pair;

import elemental.util.ArrayOf;

public interface Model<O extends Op<?>, T> {
  ArrayOf<O> compose(ArrayOf<O> ops);

  OpSink<O> create(T snapshot);

  O createOp(T delta);

  Pair<ArrayOf<O>, ArrayOf<O>> transform(ArrayOf<O> serverOps, ArrayOf<O> clientOps);
}