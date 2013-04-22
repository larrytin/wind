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
package com.goodow.realtime.util;

import com.goodow.realtime.CollaborativeString;
import com.goodow.realtime.EventHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.ExportOverlay;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.ExporterUtil;

import java.util.Comparator;

public class JsNativeInterfaceFactory implements NativeInterfaceFactory, EntryPoint {
  @ExportPackage(NativeInterfaceFactory.PACKAGE_PREFIX_OVERLAY)
  @ExportClosure
  public interface __ComparatorExportOverlay__ extends ExportOverlay<Comparator<Object>> {
    int compare(Object o1, Object o2);
  }
  @ExportPackage(NativeInterfaceFactory.PACKAGE_PREFIX_OVERLAY)
  @ExportClosure
  public interface __EventHandlerExportOverlay__ extends ExportOverlay<EventHandler<Object>> {
    void handleEvent(Object event);
  }

  @Override
  public void onModuleLoad() {
    ExporterUtil.exportAll();
  }

  @Override
  public void scheduleDeferred(final Runnable cmd) {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

      @Override
      public void execute() {
        cmd.run();
      }
    });
  }

  @Override
  public void setText(CollaborativeString str, String text) {
    // TODO Auto-generated method stub

  }

}
