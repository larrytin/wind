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

import com.goodow.realtime.Error.ErrorHandler;
import com.goodow.realtime.util.JsonSerializer;
import com.goodow.realtime.util.NativeInterfaceFactory;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import elemental.json.JsonArray;
import elemental.json.JsonValue;

/**
 * A Realtime document. A document consists of a Realtime model and a set of collaborators. Listen
 * on the document for the following events:
 * <ul>
 * <li>
 * <p>
 * {@link com.goodow.realtime.EventType#COLLABORATOR_LEFT}
 * <li>
 * <p>
 * {@link com.goodow.realtime.EventType#COLLABORATOR_JOINED}
 * <li>
 * <p>
 * {@link com.goodow.realtime.EventType#DOCUMENT_SAVE_STATE_CHANGED}
 * </ul>
 * <p>
 * This class should not be instantiated directly. The document object is generated during the
 * document load process.
 */
@ExportPackage(NativeInterfaceFactory.PACKAGE_PREFIX_REALTIME)
@Export(all = true)
public class Document implements EventTarget {
  private static final String EVENT_HANDLER_KEY = "document";
  private static final Logger log = Logger.getLogger(Document.class.getName());
  private List<Collaborator> collaborators;
  String sessionId;
  private final Model model;
  private Map<String, Map<String, List<EventHandler<?>>>> handlers;
  private final Map<String, List<String>> parents = new HashMap<String, List<String>>();

  /**
   * @param bridge The driver for the GWT collaborative libraries.
   * @param commService The communication service to dispose when this document is disposed.
   * @param errorHandlerFn The third-party error handling function.
   */
  Document(DocumentBridge bridge, Disposable commService, ErrorHandler errorHandlerFn) {
    model = new Model(bridge, this);
  }

  public void addCollaboratorJoinedListener(EventHandler<CollaboratorJoinedEvent> handler) {
    addEventListener(EventType.COLLABORATOR_JOINED.toString(), handler, false);
  }

  public void addCollaboratorLeftListener(EventHandler<CollaboratorLeftEvent> handler) {
    addEventListener(EventType.COLLABORATOR_LEFT.toString(), handler, false);
  }

  public void addDocumentSaveStateListener(EventHandler<DocumentSaveStateChangedEvent> handler) {
    addEventListener(EventType.DOCUMENT_SAVE_STATE_CHANGED.toString(), handler, false);
  }

  @Override
  public void addEventListener(String type, EventHandler<?> handler, boolean opt_capture) {
    addEventListener(EVENT_HANDLER_KEY, type, handler, opt_capture);
  }

  /**
   * Closes the document and disconnects from the server. After this function is called, event
   * listeners will no longer fire and attempts to access the document, model, or model objects will
   * throw a {@link com.goodow.realtime.DocumentClosedError}. Calling this function after the
   * document has been closed will have no effect.
   * 
   * @throws DocumentClosedError
   */
  public void close() throws DocumentClosedError {

  }

  /**
   * Exports the document to a JSON format.
   * 
   * @param successFn A function that the exported JSON will be passed to when it is available.
   * @param failureFn A function that will be called if the export fails.
   */
  public void exportDocument(Disposable successFn, Disposable failureFn) {

  }

  /**
   * Gets an array of collaborators active in this session. Each collaborator is a jsMap with these
   * fields: sessionId, userId, displayName, color, isMe, isAnonymous.
   * 
   * @return A jsArray of collaborators.
   */
  public Collaborator[] getCollaborators() {
    return collaborators.toArray(new Collaborator[0]);
  }

  /**
   * Gets the collaborative model associated with this document.
   * 
   * @return The collaborative model for this document.
   */
  public Model getModel() {
    return model;
  }

  public void removeCollaboratorJoinedListener(EventHandler<CollaboratorJoinedEvent> handler) {
    removeEventListener(EventType.COLLABORATOR_JOINED.toString(), handler, false);
  }

  public void removeCollaboratorLeftListener(EventHandler<CollaboratorLeftEvent> handler) {
    removeEventListener(EventType.COLLABORATOR_LEFT.toString(), handler, false);
  }

  @Override
  public void removeEventListener(String type, EventHandler<?> handler, boolean opt_capture) {
    removeEventListener(EVENT_HANDLER_KEY, type, handler, opt_capture);
  }

  void addEventListener(String key, String type, EventHandler<?> handler, boolean opt_capture) {
    if (key == null || type == null || handler == null) {
      throw new NullPointerException((key == null ? "Key" : type == null ? "Type" : "Handler")
          + " was null.");
    }
    List<EventHandler<?>> handlersPerType = getEventHandlers(key, type, true);
    if (handlersPerType.contains(handler)) {
      log.warning("The same handler can only be added once per the type.");
    } else {
      handlersPerType.add(handler);
    }
  }

  void addOrRemoveParent(JsonValue childOrNull, String parentId, boolean isAdd) {
    if (JsonSerializer.isNull(childOrNull)) {
      return;
    }
    JsonArray child = (JsonArray) childOrNull;
    if (child.getNumber(0) == JsonSerializer.REFERENCE_TYPE) {
      String childId = child.getString(1);
      List<String> list = parents.get(childId);
      if (isAdd) {
        if (list == null) {
          list = new ArrayList<String>();
          parents.put(childId, list);
        }
        list.add(parentId);
      } else {
        assert list != null && list.contains(parentId);
        list.remove(parentId);
        if (list.isEmpty()) {
          parents.remove(childId);
        }
      }
    }
  }

  void fireEvent(String key, BaseModelEvent event) {
    fireEvent(key, event.type, event);
    if (!event.bubbles) {
      ObjectChangedEvent objectChangedEvent =
          new ObjectChangedEvent(event.target, event.sessionId, event.userId, event.isLocal, event);
      fireEvent(key, objectChangedEvent);
    } else {
      String[] parents = getParents(key);
      if (parents != null) {
        for (String parent : parents) {
          fireEvent(parent, event);
        }
      }
    }
  }

  boolean isLocalSession(String sessionId) {
    return this.sessionId == null || this.sessionId.equals(sessionId);
  }

  void removeEventListener(String key, String type, EventHandler<?> handler, boolean opt_capture) {
    if (handlers == null || handler == null) {
      return;
    }
    Map<String, List<EventHandler<?>>> handlersPerKey = handlers.get(key);
    if (handlersPerKey == null) {
      return;
    }
    List<EventHandler<?>> handlersPerType = handlersPerKey.get(type);
    if (handlersPerType == null) {
      return;
    }
    handlersPerType.remove(handler);
    if (handlersPerType.isEmpty()) {
      handlersPerKey.remove(handlersPerType);
      if (handlersPerKey.isEmpty()) {
        handlers.remove(handlersPerKey);
        if (handlers.isEmpty()) {
          handlers = null;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void fireEvent(String key, String type, Disposable event) {
    List<EventHandler<?>> handlers = getEventHandlers(key, type, false);
    if (handlers == null) {
      return;
    }
    for (int i = 0, len = handlers.size(); i < len; i++) {
      ((EventHandler<Disposable>) handlers.get(i)).handleEvent(event);
    }
  }

  private List<EventHandler<?>> getEventHandlers(String key, String type, boolean createIfNotExist) {
    if (handlers == null) {
      if (!createIfNotExist) {
        return null;
      }
      handlers = new HashMap<String, Map<String, List<EventHandler<?>>>>();
    }
    Map<String, List<EventHandler<?>>> handlersPerKey = handlers.get(key);
    if (handlersPerKey == null) {
      if (!createIfNotExist) {
        return null;
      }
      handlersPerKey = new HashMap<String, List<EventHandler<?>>>();
      handlers.put(key, handlersPerKey);
    }
    List<EventHandler<?>> handlersPerType = handlersPerKey.get(type);
    if (handlersPerType == null) {
      if (!createIfNotExist) {
        return null;
      }
      handlersPerType = new ArrayList<EventHandler<?>>();
      handlersPerKey.put(type, handlersPerType);
    }
    return handlersPerType;
  }

  private String[] getParents(String objectId) {
    List<String> list = parents.get(objectId);
    if (list == null) {
      return null;
    }
    Set<String> set = new HashSet<String>(list);
    return set.toArray(new String[0]);
  }
}
