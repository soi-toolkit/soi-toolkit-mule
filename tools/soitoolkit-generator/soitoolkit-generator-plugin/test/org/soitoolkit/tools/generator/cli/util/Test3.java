/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.tools.generator.cli.util;

import org.soitoolkit.tools.generator.cli.util.Options;

/**
 * Based on an article published at: http://www.javaworld.com/javaworld/jw-08-2004/jw-0816-command.html
 */
public class Test3 {

  private static final String SET1 = "Hugo";
  private static final String SET2 = "Hugo2";

  public static void main(String args[]) {

    Options opt = new Options(args);
    
    opt.addSet(SET1, 2, 5).addOption("a").addOption("c");
    opt.addSet(SET2, 1, 3).addOption("b");

    System.out.println(opt);

    check(opt, SET1);
    check(opt, SET2);

  }

  public static void check(Options opt, String setName) {

    System.out.println("*** Set " + setName);

    boolean result = opt.check(setName);

    System.out.println("Check result: " + result);
    System.out.println("Check result:");
    System.out.println(opt.getCheckErrors());

    for (String s : opt.getSet(setName).getData()) {
      System.out.println("Data: " + s);
    }

    for (String s : opt.getSet(setName).getUnmatched()) {
      System.out.println("Unmatched: " + s);
    }

  }

}

