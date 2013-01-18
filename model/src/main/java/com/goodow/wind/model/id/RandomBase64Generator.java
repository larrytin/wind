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
package com.goodow.wind.model.id;

import java.util.Random;

/**
 * Produces pseudo-random web-safe base-64 strings.
 */
public class RandomBase64Generator {

  /** The 64 valid web-safe characters. */
  static final char[] WEB64_ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

  private final Random random;

  public RandomBase64Generator() {
    this(new Random());
  }

  public RandomBase64Generator(Random random) {
    this.random = random;
  }

  /**
   * Returns a string with {@code length} random base-64 characters.
   */
  public String next(int length) {
    StringBuilder result = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      result.append(WEB64_ALPHABET[random.nextInt(64)]);
    }
    return result.toString();
  }
}