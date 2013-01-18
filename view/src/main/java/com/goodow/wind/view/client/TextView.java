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

import com.goodow.wind.model.json.JString;
import com.goodow.wind.model.json.StringHandler;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.html.TextAreaElement;

public class TextView extends StringHandler {

  private final TextAreaElement textArea;
  private JString str;

  public TextView() {
    Document document = Browser.getDocument();
    textArea = document.createTextAreaElement();
    document.getBody().appendChild(textArea);
  }

  @Override
  public void onDelete(int idx, String str) {
    textArea.setValue(getValue().getString());
  }

  @Override
  public void onInsert(int idx, String str) {
    textArea.setValue(getValue().getString());
  }

  @Override
  public void render(JString str) {
    this.str = str;
    str.insert(0, "abc");
    str.delete(0, 2);
  }

}
