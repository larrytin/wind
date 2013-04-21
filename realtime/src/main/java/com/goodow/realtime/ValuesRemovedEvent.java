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

import com.goodow.realtime.util.JsNativeInterfaceFactory;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;

/**
 * Event fired when items are removed from a collaborative list.
 */
@ExportPackage(JsNativeInterfaceFactory.PACKAGE_PREFIX_REALTIME)
@Export(all = true)
public class ValuesRemovedEvent extends BaseModelEvent {
  /**
   * The index of the first removed value.
   */
  public final int index;
  /**
   * The values that were removed.
   */
  public final Object[] values;

  /**
   * @param target The target object that generated the event.
   * @param sessionId The id of the session that initiated the event.
   * @param userId The user id of the user that initiated the event.
   * @param isLocal Whether this event originated in the local session.
   * @param index The index where values were removed.
   * @param values The values removed.
   */
  public ValuesRemovedEvent(CollaborativeList target, String sessionId, String userId,
      boolean isLocal, int index, Object[] values) {
    super(EventType.VALUES_REMOVED, target, sessionId, userId, isLocal, false);
    this.index = index;
    this.values = values;
  }
}
