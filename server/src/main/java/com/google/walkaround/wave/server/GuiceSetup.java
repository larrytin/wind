/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.walkaround.wave.server;

import com.goodow.wind.server.WindServletModule;

import com.google.appengine.api.users.User;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;

import java.util.LinkedHashSet;
import java.util.logging.Logger;

import javax.servlet.Filter;

/**
 * @author ohler@google.com (Christian Ohler)
 */
public class GuiceSetup {

  private static final long INTERACTIVE_DATASTORE_TIMEOUT_MILLIS = 10000L;
  private static final long NONINTERACTIVE_DATASTORE_TIMEOUT_MILLIS = 60000L;

  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(GuiceSetup.class.getName());

  private static final LinkedHashSet<Module> extraModules = Sets.newLinkedHashSet();
  private static final LinkedHashSet<ServletModule> extraServletModules = Sets.newLinkedHashSet();
  private static final LinkedHashSet<Filter> extraFilters = Sets.newLinkedHashSet();

  /** Hacky hook to add extra filters. */
  public static void addExtraFilter(Filter f) {
    extraFilters.add(f);
  }

  /** Hacky hook to add extra modules. */
  public static void addExtraModule(Module m) {
    extraModules.add(m);
  }

  /** Hacky hook to add extra servlet modules. */
  public static void addExtraServletModule(ServletModule m) {
    extraServletModules.add(m);
  }

  public static Injector getInjectorForTaskQueueTask() {
    return Guice.createInjector(
    // Stage.DEVELOPMENT here because this is meant to be called from
    // mappers, possibly for each invocation, and the mappers probably won't
    // need all singletons.
        Stage.DEVELOPMENT, getRootModule(), getTaskQueueTaskModule());
  }

  public static Module getRootModule() {
    return getRootModule("WEB-INF");
  }

  public static Module getRootModule(final String webinfRoot) {
    return Modules.combine(Modules.combine(extraModules), new AbstractModule() {
      @Override
      public void configure() {
        bind(String.class).annotatedWith(Names.named("webinf root")).toInstance(webinfRoot);
      }
    });
  }

  public static Module getServletModule() {
    return Modules.combine(Modules.combine(extraServletModules),
        new WindServletModule(extraFilters), new AbstractModule() {
          @Override
          public void configure() {
            bind(Long.class).annotatedWith(DatastoreTimeoutMillis.class).toInstance(
                INTERACTIVE_DATASTORE_TIMEOUT_MILLIS);
          }
        });
  }

  static Module getTaskQueueTaskModule() {
    return new AbstractModule() {
      @Override
      public void configure() {
        bind(User.class).toProvider(getThrowingProvider(User.class));
        bind(Long.class).annotatedWith(DatastoreTimeoutMillis.class).toInstance(
            NONINTERACTIVE_DATASTORE_TIMEOUT_MILLIS);
      }
    };
  }

  private static <T> Provider<T> getThrowingProvider(final Class<T> clazz) {
    return new Provider<T>() {
      @Override
      public T get() {
        throw new RuntimeException("Attempt to call get() on " + this);
      }

      @Override
      public String toString() {
        return "ThrowingProvider(" + clazz + ")";
      }
    };
  }
}