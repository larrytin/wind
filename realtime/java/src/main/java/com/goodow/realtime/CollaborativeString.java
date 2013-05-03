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
import com.goodow.realtime.op.list.StringOp;
import com.goodow.realtime.op.list.algorithm.ListOp;
import com.goodow.realtime.op.list.algorithm.ListTarget;
import com.goodow.realtime.util.NativeInterface;
import com.goodow.realtime.util.NativeInterfaceFactory;

import com.google.common.annotations.GwtIncompatible;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportAfterCreateMethod;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.NoExport;

import java.util.Set;

/**
 * Creates a new collaborative string. Unlike regular JavaScript strings, collaborative strings are
 * mutable.
 * <p>
 * Changes to the string will automatically be synced with the server and other collaborators. To
 * listen for changes, add EventListeners for the following event types:
 * <ul>
 * <li>{@link com.goodow.realtime.EventType#TEXT_INSERTED}
 * <li>{@link com.goodow.realtime.EventType#TEXT_DELETED}
 * </ul>
 * <p>
 * This class should not be instantiated directly. To create a new collaborative string, use
 * {@link com.goodow.realtime.Model#createString(String)}
 */
@ExportPackage(NativeInterfaceFactory.PACKAGE_PREFIX_REALTIME)
@Export(all = true)
public class CollaborativeString extends CollaborativeObject {
  @GwtIncompatible(NativeInterfaceFactory.JS_REGISTER_PROPERTIES)
  @ExportAfterCreateMethod
  public native static void __jsRunAfter__() /*-{
    var _ = $wnd.gdr.CollaborativeString.prototype;
    Object.defineProperties(_, {
      id : {
        get : function() {
          return this.g.@com.goodow.realtime.CollaborativeObject::id;
        }
      },
      length : {
        get : function() {
          return this.g.@com.goodow.realtime.CollaborativeString::length()();
        }
      }
    });
  }-*/;

  private StringBuilder snapshot;

  CollaborativeString(Model model) {
    super(model);
  }

  public void addTextDeletedListener(EventHandler<TextDeletedEvent> handler) {
    addEventListener(EventType.TEXT_DELETED, handler, false);
  }

  public void addTextInsertedListener(EventHandler<TextInsertedEvent> handler) {
    addEventListener(EventType.TEXT_INSERTED, handler, false);
  }

  /**
   * Appends a string to the end of this one.
   * 
   * @param text The new text to append.
   * @exception java.lang.IllegalArgumentException
   */
  public void append(String text) {
    insertString(length(), text);
  }

  /**
   * Gets a string representation of the collaborative string.
   * 
   * @return A string representation of the collaborative string.
   */
  public String getText() {
    return snapshot.toString();
  }

  /**
   * Inserts a string into the collaborative string at a specific index.
   * 
   * @param index The index to insert at.
   * @param text The new text to insert.
   * @exception java.lang.IllegalArgumentException
   * @exception java.lang.StringIndexOutOfBoundsException
   */
  public void insertString(int index, String text) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one value must be specified for an insert mutation. text: " + text);
    }
    checkIndex(index);
    StringOp op = new StringOp(true, index, text, length());
    consumeAndSubmit(op);
  }

  /**
   * @return The length of the string. Read only.
   */
  @NoExport
  public int length() {
    return snapshot.length();
  }

  /**
   * Creates an IndexReference at the given {@code index}. If {@code canBeDeleted} is set, then a
   * delete over the index will delete the reference. Otherwise the reference will shift to the
   * beginning of the deleted range.
   * 
   * @param index The index of the reference.
   * @param canBeDeleted Whether this index is deleted when there is a delete of a range covering
   *          this index.
   * @return The newly registered reference.
   */
  public IndexReference registerReference(int index, boolean canBeDeleted) {
    checkIndex(index);
    return model.createIndexReference(this, index, canBeDeleted);
  }

  /**
   * Deletes the text between startIndex (inclusive) and endIndex (exclusive).
   * 
   * @param startIndex The start index of the range to delete (inclusive).
   * @param endIndex The end index of the range to delete (exclusive).
   * @exception java.lang.StringIndexOutOfBoundsException
   */
  public void removeRange(int startIndex, int endIndex) {
    int length = length();
    if (startIndex < 0 || startIndex >= length || endIndex <= startIndex || endIndex > length) {
      throw new StringIndexOutOfBoundsException("StartIndex: " + startIndex + ", EndIndex: "
          + endIndex + ", Size: " + length);
    }
    StringOp op = new StringOp(false, startIndex, snapshot.substring(startIndex, endIndex), length);
    consumeAndSubmit(op);
  }

  public void removeStringListener(EventHandler<?> handler) {
    removeEventListener(EventType.TEXT_INSERTED, handler, false);
    removeEventListener(EventType.TEXT_DELETED, handler, false);
  }

  /**
   * Sets the contents of this collaborative string. Note that this method performs a text diff
   * between the current string contents and the new contents so that the string will be modified
   * using the minimum number of text inserts and deletes possible to change the current contents to
   * the newly-specified contents.
   * 
   * @param text The new value of the string.
   */
  public void setText(String text) {
    if (text == null) {
      throw new IllegalArgumentException("Expected string for text, but was: null");
    }
    model.beginCompoundOperation("replaceText");
    NativeInterface.get().setText(this, text);
    model.endCompoundOperation();
  }

  @Override
  protected void consume(final RealtimeOp operation) {
    operation.<ListTarget<String>> getOp().apply(new ListTarget<String>() {
      private int cursor;

      @Override
      public ListTarget<String> delete(String str) {
        assert snapshot.substring(cursor, cursor + str.length()).equals(str);
        String sessionId = operation.getSessionId();
        deleteAndFireEvent(cursor, cursor + str.length(), sessionId, operation.getUserId());
        return null;
      }

      @Override
      public ListTarget<String> insert(String str) {
        String sessionId = operation.getSessionId();
        insertAndFireEvent(cursor, str, sessionId, operation.getUserId());
        cursor += str.length();
        return null;
      }

      @Override
      public ListTarget<String> retain(int length) {
        cursor += length;
        return null;
      }
    });
  }

  void initialize(String id, String opt_initialValue) {
    this.id = id;
    snapshot = new StringBuilder(opt_initialValue == null ? "" : opt_initialValue);
    model.objects.put(id, this);
  }

  @Override
  Op<?> toInitialization() {
    if (length() == 0) {
      return null;
    }
    ListOp<String> op = new StringOp().insert(getText());
    return op;
  }

  @Override
  void toString(Set<String> seen, StringBuilder sb) {
    if (seen.contains(id)) {
      sb.append("<EditableString: ").append(id).append(">");
      return;
    }
    seen.add(id);
    sb.append(getText());
  }

  private void checkIndex(int index) {
    int length = length();
    if (index < 0 || index > length) {
      throw new StringIndexOutOfBoundsException("Index: " + index + ", Size: " + length);
    }
  }

  private void deleteAndFireEvent(int startIndex, int endIndex, String sessionId, String userId) {
    assert startIndex < endIndex && endIndex <= length();
    String toDelete = snapshot.substring(startIndex, endIndex);
    boolean isLocal = model.document.isLocalSession(sessionId);
    TextDeletedEvent event =
        new TextDeletedEvent(this, sessionId, userId, isLocal, startIndex, toDelete);
    snapshot.delete(startIndex, endIndex);
    fireEvent(event);
    model.setIndexReferenceIndex(id, false, startIndex, endIndex - startIndex, sessionId, userId);
  }

  private void insertAndFireEvent(int index, String text, String sessionId, String userId) {
    assert index <= length();
    boolean isLocal = model.document.isLocalSession(sessionId);
    TextInsertedEvent event = new TextInsertedEvent(this, sessionId, userId, isLocal, index, text);
    snapshot.insert(index, text);
    fireEvent(event);
    model.setIndexReferenceIndex(id, true, index, text.length(), sessionId, userId);
  }
}
