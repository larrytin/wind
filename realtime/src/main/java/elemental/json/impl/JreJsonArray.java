/*
 * Copyright 2010 Google Inc.
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
package elemental.json.impl;

import java.util.ArrayList;
import java.util.List;

import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonFactory;
import elemental.json.JsonNumber;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonType;
import elemental.json.JsonValue;

/**
 * Server-side implementation of JsonArray.
 */
public class JreJsonArray extends JreJsonValue implements JsonArray {

  private final ArrayList<JsonValue> arrayValues = new ArrayList<JsonValue>();

  private final JsonFactory factory;

  public JreJsonArray(JsonFactory factory) {
    this.factory = factory;
  }

  @Override
  public boolean asBoolean() {
    return true;
  }

  @Override
  public double asNumber() {
    switch (length()) {
      case 0:
        return 0;
      case 1:
        return get(0).asNumber();
      default:
        return Double.NaN;
    }
  }

  @Override
  public String asString() {
    StringBuilder toReturn = new StringBuilder();
    for (int i = 0; i < length(); i++) {
      if (i > 0) {
        toReturn.append(", ");
      }
      toReturn.append(get(i).asString());
    }
    return toReturn.toString();
  }

  @Override
  public JsonValue get(int index) {
    return arrayValues.get(index);
  }

  @Override
  public JsonArray getArray(int index) {
    return (JsonArray) get(index);
  }

  @Override
  public boolean getBoolean(int index) {
    return ((JsonBoolean) get(index)).getBoolean();
  }

  @Override
  public double getNumber(int index) {
    return ((JsonNumber) get(index)).getNumber();
  }

  @Override
  public Object getObject() {
    List<Object> objs = new ArrayList<Object>();
    for (JsonValue val : arrayValues) {
      objs.add(((JreJsonValue) val).getObject());
    }
    return objs;
  }

  @Override
  public JsonObject getObject(int index) {
    return (JsonObject) get(index);
  }

  @Override
  public String getString(int index) {
    return ((JsonString) get(index)).getString();
  }

  @Override
  public JsonType getType() {
    return elemental.json.JsonType.ARRAY;
  }

  @Override
  public void insert(int index, JsonValue value) {
    if (value == null) {
      value = factory.createNull();
    }
    arrayValues.add(index, value);
  }

  // @Override
  // public void insertAll(int index, JsonValue... values) {
  // for(int i=0,len=values.length;i<len;i++){
  // if (values[i] == null) {
  // values[i] = factory.createNull();
  // }
  // }
  // arrayValues.addAll(index, Arrays.asList(values));
  // }

  @Override
  public boolean jsEquals(JsonValue value) {
    return getObject().equals(((JreJsonValue) value).getObject());
  }

  @Override
  public int length() {
    return arrayValues.size();
  }

  @Override
  public void remove(int index) {
    arrayValues.remove(index);
  }

  @Override
  public void set(int index, boolean bool) {
    set(index, factory.create(bool));
  }

  @Override
  public void set(int index, double number) {
    set(index, factory.create(number));
  }

  @Override
  public void set(int index, JsonValue value) {
    if (value == null) {
      value = factory.createNull();
    }
    if (index == arrayValues.size()) {
      arrayValues.add(index, value);
    } else {
      arrayValues.set(index, value);
    }
  }

  @Override
  public void set(int index, String string) {
    set(index, factory.create(string));
  }

  @Override
  public String toJson() {
    return JsonUtil.stringify(this);
  }

  @Override
  public void traverse(elemental.json.impl.JsonVisitor visitor, elemental.json.impl.JsonContext ctx) {
    if (visitor.visit(this, ctx)) {
      JsonArrayContext arrayCtx = new JsonArrayContext(this);
      for (int i = 0; i < length(); i++) {
        arrayCtx.setCurrentIndex(i);
        if (visitor.visitIndex(arrayCtx.getCurrentIndex(), arrayCtx)) {
          visitor.accept(get(i), arrayCtx);
          arrayCtx.setFirst(false);
        }
      }
    }
    visitor.endVisit(this, ctx);
  }
}