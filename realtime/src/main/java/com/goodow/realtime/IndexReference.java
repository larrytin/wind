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
import com.goodow.realtime.util.NativeInterfaceFactory;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;

import java.util.Set;

import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * An IndexReference is a pointer to a specific location in a collaborative list or string. This
 * pointer automatically shifts as new elements are added to and removed from the object.
 * <p>
 * To listen for changes to the referenced index, add an EventListener for
 * <ul>
 * <li>{@link com.goodow.realtime.EventType#REFERENCE_SHIFTED}
 * </ul>
 * <p>
 * This class should not be instantiated directly. To create an index reference, call
 * registerReference on the appropriate string or list.
 */
@ExportPackage(NativeInterfaceFactory.PACKAGE_PREFIX_REALTIME)
@Export(all = true)
public class IndexReference extends CollaborativeObject {
  private JsonArray snapshot;

  /**
   * @param model The document model.
   */
  IndexReference(Model model) {
    super(model);
  }

  public void addReferenceShiftedListener(EventHandler<ReferenceShiftedEvent> handler) {
    addEventListener(EventType.REFERENCE_SHIFTED.toString(), handler, false);
  }

  /**
   * @return Whether this reference can be deleted. Read-only. This property affects the behavior of
   *         the index reference when the index the reference points to is deleted. If this is true,
   *         the index reference will be deleted. If it is false, the index reference will move to
   *         point at the beginning of the deleted range.
   */
  public boolean canBeDeleted() {
    return snapshot.getBoolean(2);
  }

  /**
   * @return The index of the current location the reference points to. Write to this property to
   *         change the referenced index.
   */
  public int getIndex() {
    return (int) snapshot.getNumber(1);
  }

  /**
   * @return The object this reference points to. Read-only.
   */
  public CollaborativeObject getReferencedObject() {
    return model.getObject(snapshot.getString(0));
  }

  public void removeReferenceShiftedListener(EventHandler<ReferenceShiftedEvent> handler) {
    removeEventListener(EventType.REFERENCE_SHIFTED.toString(), handler, false);
  }

  @Override
  protected void consume(RealtimeOp operation) {
    throw new UnsupportedOperationException();
  }

  void initialize(String id, JsonArray snapshot) {
    this.id = id;
    this.snapshot = snapshot;
    model.objects.put(id, this);
    model.registerIndexReference(id, snapshot.getString(0));
  }

  void initializeCreate(String id, CollaborativeObject referencedObject, int index,
      boolean canBeDeleted) {
    JsonArray snapshot = Json.createArray();
    snapshot.set(0, referencedObject.id);
    snapshot.set(1, index);
    snapshot.set(2, canBeDeleted);
    initialize(id, snapshot);
  }

  void setIndex(boolean isInsert, int index, int length, String sessionId, String userId) {
    int cursor = getIndex();
    if (cursor < index) {
      return;
    }
    int newIndex = -2;
    if (isInsert) {
      newIndex = cursor + length;
    } else {
      if (cursor < index + length) {
        if (canBeDeleted()) {
          newIndex = -1;
        } else {
          newIndex = index;
        }
      } else {
        newIndex = cursor - length;
      }
    }
    boolean isLocal = model.document.isLocalSession(sessionId);
    ReferenceShiftedEvent event =
        new ReferenceShiftedEvent(this, cursor, newIndex, sessionId, userId, isLocal);
    snapshot.set(1, newIndex);
    fireEvent(event);
  }

  @Override
  Op<?> toInitialization() {
    return null;
  }

  @Override
  void toString(Set<String> seen, StringBuilder sb) {
    if (seen.contains(id)) {
      sb.append("<IndexReference: ").append(id).append(">");
      return;
    }
    seen.add(id);
    sb.append("DefaultIndexReference [");
    sb.append("id=").append(getId());
    sb.append(", objectId=").append(getReferencedObject().getId());
    sb.append(", index=").append(getIndex());
    sb.append(", canBeDeleted=").append(canBeDeleted());
    sb.append("]");
  }
}
