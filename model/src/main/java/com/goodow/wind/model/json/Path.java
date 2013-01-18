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
package com.goodow.wind.model.json;

import com.goodow.wind.model.util.Pair;

public class Path {
  private static final Path ROOT = new Path("");

  public static final Path of() {
    return ROOT;
  }

  public static final Path of(String path) {
    return new Path(path);
  }

  static final void checkKey(String key) {
    assert key != null && !key.isEmpty() && !key.contains(".") && !key.contains("[")
        && !key.contains("]");
  }

  private final String path;

  private Path(String path) {
    assert path != null;
    this.path = path;
  }

  public Path at(int index) {
    return new Path(path + "[" + index + "]");
  }

  public Path at(Path subPath) {
    assert subPath != null;
    if (subPath.path.isEmpty()) {
      return this;
    }
    if (subPath.path.charAt(0) == '[') {
      return new Path(path + subPath.path);
    }
    return new Path((path.isEmpty() ? "" : path + ".") + subPath.path);
  }

  public Path at(String key) {
    checkKey(key);
    return new Path((path.isEmpty() ? "" : path + ".") + key);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Path)) {
      return false;
    }
    Path other = (Path) obj;
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    return true;
  }

  public Path getParent() {
    if (path.isEmpty()) {
      return null;
    }
    if (isIndexed()) {
      return new Path(path.substring(0, path.lastIndexOf('[')));
    }
    int lastIndexOf = path.lastIndexOf('.');
    if (lastIndexOf == -1) {
      return Path.ROOT;
    }
    return new Path(path.substring(0, lastIndexOf));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return path;
  }

  int getIndex() {
    assert isIndexed();
    int idx = path.lastIndexOf('[');
    return Integer.parseInt(path.substring(idx + 1, path.length() - 1));
  }

  String getKey() {
    assert !isIndexed();
    int idx = path.lastIndexOf('.');
    if (idx == -1) {
      return path;
    }
    return path.substring(idx + 1, path.length());
  }

  boolean isAncestorOf(Path childPath) {
    String child = childPath.toString();
    if (child.isEmpty()) {
      return false;
    }
    if (path.isEmpty()) {
      return true;
    }
    if (child.startsWith(path + ".") || child.startsWith(path + "[")) {
      return true;
    }
    return false;
  }

  boolean isIndexed() {
    return path.endsWith("]");
  }

  Pair<Integer, String> nextIndex(String parentPath) {
    if (path.isEmpty() || (parentPath.isEmpty() && path.charAt(0) != '[')
        || (!parentPath.isEmpty() && !path.startsWith(parentPath + "["))) {
      return null;
    }
    final int cursor = parentPath.length();
    assert path.charAt(cursor) == '[';
    int indexOf = path.indexOf(']', cursor);
    String rest = "";
    if (path.length() > indexOf + 1) {
      rest = path.substring(indexOf + (path.charAt(indexOf + 1) == '[' ? 1 : 2));
    }
    return Pair.of(Integer.parseInt(path.substring(cursor + 1, indexOf)), rest);
  }

  Pair<String, String> nextKey(String parentPath) {
    if (path.isEmpty() || (parentPath.isEmpty() && path.charAt(0) == '[')
        || (!parentPath.isEmpty() && !path.startsWith(parentPath + "."))) {
      return null;
    }
    int cursor = path.length();
    int indexOf = path.indexOf('[', parentPath.length() + 1);
    if (indexOf != -1) {
      cursor = indexOf;
    }
    indexOf = path.indexOf('.', parentPath.length() + 1);
    if (indexOf != -1 && indexOf < cursor) {
      cursor = indexOf;
    }
    String nextKey = path.substring(parentPath.isEmpty() ? 0 : parentPath.length() + 1, cursor);
    String rest =
        cursor == path.length() ? "" : path.substring(path.charAt(cursor) == '[' ? cursor
            : cursor + 1);
    return Pair.of(nextKey, rest);
  }
}
