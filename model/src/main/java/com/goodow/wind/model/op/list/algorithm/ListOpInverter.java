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
package com.goodow.wind.model.op.list.algorithm;

class ListOpInverter<T> implements ListTarget<T> {
  private final ListOp<T> output;

  public ListOpInverter(ListOp<T> op, ListOp<T> output) {
    this.output = output;
    op.apply(this);
  }

  @Override
  public ListTarget<T> delete(T list) {
    output.insert(list);
    return this;
  }

  @Override
  public ListTarget<T> insert(T list) {
    output.delete(list);
    return this;
  }

  @Override
  public ListTarget<T> retain(int length) {
    output.retain(length);
    return this;
  }

  ListOp<T> finish() {
    return output;
  }
}