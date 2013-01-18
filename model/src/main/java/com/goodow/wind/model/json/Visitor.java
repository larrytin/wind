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

/**
 * A visitor for JSON objects. For each unique JSON datatype, a callback is invoked with a
 * {@link Context} that can be used to replace a value or remove it. For Object and Array types, the
 * {@link #visitKey} and {@link #visitIndex} methods are invoked respectively for each contained
 * value to determine if they should be processed or not. Finally, the visit methods for Object and
 * Array types returns a boolean that determines whether or not to process its contained values.
 */
@SuppressWarnings("unused")
public class Visitor {

  static class DestroyVisitor extends Visitor {
    @Override
    public void endVisitIndex(JArray array, int idx) {
      array.fireEvent(0, array.array().get(idx), null, false);
    }

    @Override
    public void endVisitKey(JObject obj, String key) {
      obj.fireEvent(key, obj.object().get(key), null, false);
    }

    @Override
    public void visit(JNumber num) {
      num.fireEvent(-num.getNumber(), false);
    }

    @Override
    public void visit(JString str) {
      str.fireEvent(false, 0, str.getString(), false);
    }
  }

  static class InitializeVisitor extends Visitor {
    @Override
    public boolean visit(JArray array) {
      array.fireEvent(0, null, null, true);
      return true;
    }

    @Override
    public void visit(JNumber num) {
      num.fireEvent(num.getNumber(), true);
    }

    @Override
    public boolean visit(JObject obj) {
      obj.fireEvent(null, null, null, true);
      return true;
    }

    @Override
    public void visit(JString str) {
      str.fireEvent(true, 0, str.getString(), true);
    }

    @Override
    public boolean visitIndex(JArray array, int idx) {
      array.fireEvent(idx, null, array.get(idx), false);
      return true;
    }

    @Override
    public boolean visitKey(JObject obj, String key) {
      obj.fireEvent(key, null, obj.get(key), false);
      return true;
    }
  }

  public void accept(JValue val) {
    if (val == null) {
      return;
    }
    val.traverse(this);
  }

  /**
   * Called after every element of array has been visited.
   */
  public void endVisit(JArray array) {
  }

  /**
   * Called after every field of an object has been visited.
   * 
   * @param obj
   * @param ctx
   */
  public void endVisit(JObject obj) {
  }

  public void endVisitIndex(JArray array, int idx) {
  }

  public void endVisitKey(JObject obj, String key) {
  }

  /**
   * Called for JS arrays present in a JSON object. Return true if array elements should be visited.
   * 
   * @param array a JS array
   * @return true if the array elements should be visited
   */
  public boolean visit(JArray array) {
    return true;
  }

  /**
   * Called for JS boolean present in a JSON object.
   */
  public void visit(JBoolean bool) {
  }

  /**
   * Called for nulls present in a JSON object.
   */
  public void visit(JNull nul) {
  }

  /**
   * Called for JS numbers present in a JSON object.
   */
  public void visit(JNumber num) {
  }

  /**
   * Called for JS objects present in a JSON object. Return true if object fields should be visited.
   * 
   * @param obj a Json object
   * @return true if object fields should be visited
   */
  public boolean visit(JObject obj) {
    return true;
  }

  /**
   * Called for JS strings present in a JSON object.
   */
  public void visit(JString str) {
  }

  /**
   * Return true if the value for a given array index should be visited.
   * 
   * @param idx an index in a JSON array
   * @return true if the value associated with the index should be visited
   */
  public boolean visitIndex(JArray array, int idx) {
    return true;
  }

  /**
   * Return true if the value for a given object key should be visited.
   * 
   * @param key a key in a JSON object
   * @return true if the value associated with the key should be visited
   */
  public boolean visitKey(JObject obj, String key) {
    return true;
  }
}