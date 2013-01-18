/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.walkaround.util.server.servlet;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @see HandlerServlet
 * @author ohler@google.com (Christian Ohler)
 */
public abstract class AbstractHandler {

  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(AbstractHandler.class.getName());

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // Perhaps this should be a 405 but who cares.
    throw new BadRequestException("GET not supported");
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // Perhaps this should be a 405 but who cares.
    throw new BadRequestException("POST not supported");
  }

  // Other methods from javax.servlet.http.HttpServlet, like doPut etc., could
  // be added here if we need them.


  // TODO(ohler): Move the utilities below somewhere else.

  // Apparently, HttpServletRequest.getParameterMap() returns a bare Map rather
  // than Map<String, String[]>.
  @SuppressWarnings("unchecked")
  public static Map<String, String[]> getParameterMap(HttpServletRequest req) {
    return req.getParameterMap();
  }

  public static String requireParameter(HttpServletRequest req, String key) {
    String value = req.getParameter(key);
    if (value == null) {
      throw new BadRequestException("Missing parameter: " + key);
    }
    return value;
  }

  public static List<String> getParameters(HttpServletRequest req, String key) {
    String[] values = getParameterMap(req).get(key);
    return values == null ? ImmutableList.<String>of() : ImmutableList.copyOf(values);
  }

  public static String optionalParameter(HttpServletRequest req, String key,
      @Nullable String defaultValue) {
    String value = req.getParameter(key);
    return value == null ? defaultValue : value;
  }

  public static Locale getLocale(HttpServletRequest req) {
    return req.getLocale();
  }

}
