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
package com.goodow.wind.view.client;

import com.goodow.wind.channel.ChannelRegistry;
import com.goodow.wind.model.json.JsonHandlerRegistry;
import com.goodow.wind.model.json.Path;

import com.google.gwt.core.client.EntryPoint;

import java.util.logging.Logger;

import elemental.json.JsonType;

public class Main implements EntryPoint {
  private static final Logger log = Logger.getLogger(Main.class.getName());

  @Override
  public void onModuleLoad() {
    String key = "test/text";
    JsonHandlerRegistry.ROOT.registerHandler(key, Path.of(), new TextView());
    ChannelRegistry.ROOT.open(key, JsonType.STRING, null);
  }
}