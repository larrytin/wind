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
package com.goodow.wind.view;

import com.goodow.wind.channel.ChannelRegistry;
import com.goodow.wind.model.json.JsonHandlerRegistry;

public class RegistriesImpl implements Registries {

  private final JsonHandlerRegistry jsonHandlerRegistry;
  private final ChannelRegistry channelRegistry;

  public RegistriesImpl(JsonHandlerRegistry jsonHandlerRegistry, ChannelRegistry channelRegistry) {
    this.jsonHandlerRegistry = jsonHandlerRegistry;
    this.channelRegistry = channelRegistry;
  }

  @Override
  public Registries createExtension() {
    return new RegistriesImpl(jsonHandlerRegistry.createExtension(), channelRegistry
        .createExtension());
  }

  @Override
  public ChannelRegistry getChannelRegistry() {
    return channelRegistry;
  }

  @Override
  public JsonHandlerRegistry getJsonHandlerRegistry() {
    return jsonHandlerRegistry;
  }
}
