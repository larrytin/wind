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
package com.goodow.realtime;

import com.goodow.realtime.op.Op;
import com.goodow.realtime.op.RealtimeOp;
import com.goodow.realtime.op.list.ArrayOp;
import com.goodow.realtime.op.list.algorithm.ListTarget;
import com.goodow.realtime.util.JsonSerializer;
import com.goodow.realtime.util.NativeInterfaceFactory;

import com.google.common.annotations.GwtIncompatible;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportAfterCreateMethod;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.NoExport;

import java.util.Comparator;
import java.util.Set;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

/**
 * A collaborative list. A list can contain other Realtime collaborative objects, custom
 * collaborative objects, primitive values, or objects that can be serialized to JSON.
 * <p>
 * Changes to the list will automatically be synced with the server and other collaborators. To
 * listen for changes, add EventListeners for the following event types:
 * <ul>
 * <li>{@link com.goodow.realtime.EventType#VALUES_ADDED}
 * <li>{@link com.goodow.realtime.EventType#VALUES_REMOVED}
 * <li>{@link com.goodow.realtime.EventType#VALUES_SET}
 * </ul>
 * <p>
 * This class should not be instantiated directly. To create a new list, use
 * {@link com.goodow.realtime.Model#createList(Object...)}.
 */
@ExportPackage(NativeInterfaceFactory.PACKAGE_PREFIX_REALTIME)
@Export(all = true)
public class CollaborativeList extends CollaborativeObject {
  @GwtIncompatible("J2ObjC blocked by JSNI")
  @ExportAfterCreateMethod
  public native static void __jsRegisterProperties__() /*-{
		var _ = $wnd.gdr.CollaborativeList.prototype;
		Object
				.defineProperties(
						_,
						{
							id : {
								get : function() {
									return this.g.@com.goodow.realtime.CollaborativeObject::id;
								}
							},
							length : {
								get : function() {
									return this.g.@com.goodow.realtime.CollaborativeList::length()();
								}
							}
						});
  }-*/;

  private JsonArray snapshot;

  /**
   * @param model The document model.
   */
  CollaborativeList(Model model) {
    super(model);
  }

  public void addValuesAddedListener(EventHandler<ValuesAddedEvent> handler) {
    addEventListener(EventType.VALUES_ADDED.toString(), handler, false);
  }

  public void addValuesRemovedListener(EventHandler<ValuesRemovedEvent> handler) {
    addEventListener(EventType.VALUES_REMOVED.toString(), handler, false);
  }

  public void addValuesSetListener(EventHandler<ValuesSetEvent> handler) {
    addEventListener(EventType.VALUES_SET.toString(), handler, false);
  }

  /**
   * Returns a copy of the contents of this collaborative list as a array. Changes to the returned
   * object will not affect the original collaborative list.
   * 
   * @return A copy of the contents of this collaborative list.
   */
  public Object[] asArray() {
    int length = length();
    Object[] objects = new Object[length];
    for (int i = 0; i < length; i++) {
      objects[i] = get(i);
    }
    return objects;
  }

  /**
   * Removes all values from the list.
   */
  public void clear() {
    removeRange(0, length());
  }

  /**
   * Gets the value at the given index.
   * 
   * @param index The index.
   * @return The value at the given index.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  @SuppressWarnings("unchecked")
  public <T> T get(int index) {
    checkIndex(index, false);
    return (T) JsonSerializer.jsonToObj(snapshot.get(index), model.objects);
  }

  /**
   * Returns the first index of the given value, or -1 if it cannot be found.
   * 
   * @param value The value to find.
   * @param opt_comparatorFn Optional comparator function used to determine the equality of two
   *          items.
   * @return The index of the given value, or -1 if it cannot be found.
   */
  public int indexOf(Object value, Comparator<Object> opt_comparatorFn) {
    if (opt_comparatorFn == null) {
      JsonArray jsonValue;
      try {
        jsonValue = JsonSerializer.objToJson(value);
      } catch (ClassCastException e) {
        return -1;
      }
      for (int i = 0, len = length(); i < len; i++) {
        if (JsonSerializer.jsonEqual(jsonValue, snapshot.get(i))) {
          return i;
        }
      }
    } else {
      for (int i = 0, len = length(); i < len; i++) {
        if (opt_comparatorFn.compare(value, get(i)) == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Inserts an item into the list at a given index.
   * 
   * @param index The index to insert at.
   * @param value The value to add.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void insert(int index, Object value) {
    insertAll(index, value);
  }

  /**
   * Inserts a list of items into the list at a given index.
   * 
   * @param index The index at which to insert.
   * @param values The values to insert.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void insertAll(int index, Object... values) {
    checkIndex(index, true);
    JsonArray array = Json.createArray();
    if (values == null) {
      array.set(0, (JsonValue) null);
    } else if (values.length == 0) {
      return;
    } else {
      for (int i = 0, len = values.length; i < len; i++) {
        array.set(i, JsonSerializer.objToJson(values[i]));
      }
    }
    ArrayOp op = new ArrayOp(true, index, array, length());
    consumeAndSubmit(op);
  }

  /**
   * Returns the last index of the given value, or -1 if it cannot be found.
   * 
   * @param value The value to find.
   * @param opt_comparatorFn Optional comparator function used to determine the equality of two
   *          items.
   * @return The index of the given value, or -1 if it cannot be found.
   */
  public int lastIndexOf(Object value, Comparator<Object> opt_comparatorFn) {
    if (opt_comparatorFn == null) {
      JsonArray jsonValue;
      try {
        jsonValue = JsonSerializer.objToJson(value);
      } catch (ClassCastException e) {
        return -1;
      }
      for (int i = length() - 1; i >= 0; i--) {
        if (JsonSerializer.jsonEqual(jsonValue, snapshot.get(i))) {
          return i;
        }
      }
    } else {
      for (int i = length() - 1; i >= 0; i--) {
        if (opt_comparatorFn.compare(value, get(i)) == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * @return The number of entries in the list. Assign to this field to reduce the size of the list.
   *         Note that the length given must be < or equal to the current size. The length of a list
   *         cannot be extended in this way.
   */
  @NoExport
  public int length() {
    return snapshot.length();
  }

  /**
   * Adds an item to the end of the list.
   * 
   * @param value The value to add.
   * @return The new array length.
   */
  public int push(Object value) {
    insert(length(), value);
    return length();
  }

  /**
   * Adds an array of values to the end of the list.
   * 
   * @param values The values to add.
   */
  public void pushAll(Object... values) {
    insertAll(length(), values);
  }

  /**
   * Creates an IndexReference at the given index. If canBeDeleted is true, then a delete over the
   * index will delete the reference. Otherwise the reference will shift to the beginning of the
   * deleted range.
   * 
   * @param index The index of the reference.
   * @param canBeDeleted Whether this index is deleted when there is a delete of a range covering
   *          this index.
   * @return The newly registered reference.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public IndexReference registerReference(int index, boolean canBeDeleted) {
    checkIndex(index, true);
    return model.createIndexReference(this, index, canBeDeleted);
  }

  /**
   * Removes the item at the given index from the list.
   * 
   * @param index The index of the item to remove.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void remove(int index) {
    removeRange(index, index + 1);
  }

  public void removeListListener(EventHandler<?> handler) {
    removeEventListener(EventType.VALUES_ADDED.toString(), handler, false);
    removeEventListener(EventType.VALUES_REMOVED.toString(), handler, false);
    removeEventListener(EventType.VALUES_SET.toString(), handler, false);
  }

  /**
   * Removes the items between startIndex (inclusive) and endIndex (exclusive).
   * 
   * @param startIndex The start index of the range to remove (inclusive).
   * @param endIndex The end index of the range to remove (exclusive).
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void removeRange(int startIndex, int endIndex) {
    int length = length();
    if (startIndex < 0 || startIndex >= length || endIndex <= startIndex || endIndex > length) {
      throw new ArrayIndexOutOfBoundsException("StartIndex: " + startIndex + ", EndIndex: "
          + endIndex + ", Size: " + length);
    }
    JsonArray array = Json.createArray();
    for (int i = startIndex; i < endIndex; i++) {
      array.set(i, snapshot.get(i));
    }
    ArrayOp op = new ArrayOp(false, startIndex, array, length);
    consumeAndSubmit(op);
  }

  /**
   * Removes the first instance of the given value from the list.
   * 
   * @param value The value to remove.
   * @return Whether the item was removed.
   */
  public boolean removeValue(Object value) {
    int index = indexOf(value, null);
    if (index == -1) {
      return false;
    }
    remove(index);
    return true;
  }

  /**
   * Replaces items in the list with the given items, starting at the given index.
   * 
   * @param index The index to set at.
   * @param values The values to insert.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void replaceRange(int index, Object... values) {
    model.beginCompoundOperation(null);
    removeRange(index, index + values.length);
    insertAll(index, values);
    model.endCompoundOperation();
  }

  /**
   * Sets the item at the given index
   * 
   * @param index The index to insert at.
   * @param value The value to set.
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void set(int index, Object value) {
    checkIndex(index, false);
    model.beginCompoundOperation(null);
    remove(index);
    insert(index, value);
    model.endCompoundOperation();
  }

  /**
   * @see #length()
   * @param length the new length of the array
   * @exception java.lang.ArrayIndexOutOfBoundsException
   */
  public void setLength(int length) {
    checkIndex(length, true);
    int len = length();
    if (length < len) {
      removeRange(length, len);
    }
  }

  @Override
  protected void consume(final RealtimeOp operation) {
    operation.<ListTarget<JsonArray>> getOp().apply(new ListTarget<JsonArray>() {
      private int cursor;

      @Override
      public ListTarget<JsonArray> delete(JsonArray list) {
        assert list.length() > 0;
        assert cursor + list.length() <= length();
        String sessionId = operation.getSessionId();
        removeAndFireEvent(cursor, list, sessionId, operation.getUserId());
        return null;
      }

      @Override
      public ListTarget<JsonArray> insert(JsonArray list) {
        assert list.length() > 0;
        assert cursor <= length();
        String sessionId = operation.getSessionId();
        insertAndFireEvent(cursor, list, sessionId, operation.getUserId());
        cursor += list.length();
        return null;
      }

      @Override
      public ListTarget<JsonArray> retain(int length) {
        cursor += length;
        return null;
      }
    });
  }

  void initialize(String id, JsonArray snapshot) {
    this.id = id;
    this.snapshot = snapshot;
    model.objects.put(id, this);
    for (int i = 0, len = length(); i < len; i++) {
      model.document.addOrRemoveParent(snapshot.get(i), id, true);
    }
  }

  void initializeCreate(String id, Object... opt_initialValue) {
    JsonArray snapshot = Json.createArray();
    if (opt_initialValue != null) {
      for (int i = 0, len = opt_initialValue.length; i < len; i++) {
        JsonArray array = JsonSerializer.objToJson(opt_initialValue[i]);
        snapshot.set(i, array);
      }
    }
    initialize(id, snapshot);
  }

  @Override
  Op<?> toInitialization() {
    if (length() == 0) {
      return null;
    }
    ArrayOp op = new ArrayOp();
    op.insert(snapshot);
    return op;
  }

  @Override
  void toString(Set<String> seen, StringBuilder sb) {
    if (seen.contains(id)) {
      sb.append("<List: ").append(id).append(">");
      return;
    }
    seen.add(id);
    sb.append("[");
    boolean isFirst = true;
    for (int i = 0, len = length(); i < len; i++) {
      if (!isFirst) {
        sb.append(", ");
        isFirst = false;
      }
      Object value = get(i);
      if (value == null) {
        sb.append("null");
      } else if (value instanceof CollaborativeObject) {
        CollaborativeObject obj = (CollaborativeObject) value;
        obj.toString(seen, sb);
      } else {
        sb.append("[JsonValue " + snapshot.getArray(i).get(1).toJson() + "]");
      }
    }
    sb.append("]");
  }

  private void checkIndex(int index, boolean endBoundIsValid) {
    int length = length();
    if (index < 0 || (endBoundIsValid ? index > length : index >= length)) {
      throw new ArrayIndexOutOfBoundsException("Index: " + index + ", Size: " + length);
    }
  }

  private void insertAndFireEvent(int index, JsonArray array, String sessionId, String userId) {
    int length = array.length();
    Object[] objects = new Object[length];
    for (int i = 0; i < length; i++) {
      JsonValue value = array.get(i);
      objects[i] = JsonSerializer.jsonToObj(value, model.objects);
      snapshot.insert(index + i, value);
      model.document.addOrRemoveParent(value, id, true);
    }
    boolean isLocal = model.document.isLocalSession(sessionId);
    ValuesAddedEvent event = new ValuesAddedEvent(this, sessionId, userId, isLocal, index, objects);
    fireEvent(event);
    model.setIndexReferenceIndex(id, true, index, length, sessionId, userId);
  }

  private void removeAndFireEvent(int index, JsonArray array, String sessionId, String userId) {
    int length = array.length();
    Object[] objects = new Object[length];
    for (int i = 0; i < length; i++) {
      assert JsonSerializer.jsonEqual(snapshot.get(index), array.get(i));
      objects[i] = get(index);
      snapshot.remove(index);
      model.document.addOrRemoveParent(array.get(i), id, false);
    }
    boolean isLocal = model.document.isLocalSession(sessionId);
    ValuesRemovedEvent event =
        new ValuesRemovedEvent(this, sessionId, userId, isLocal, index, objects);
    fireEvent(event);
    model.setIndexReferenceIndex(id, false, index, length, sessionId, userId);
  }
}
