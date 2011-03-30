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

import org.soitoolkit.tools.generator.cli.util.Options.Multiplicity;
import org.soitoolkit.tools.generator.cli.util.Options.Separator;

/**
 * Based on an article published at: http://www.javaworld.com/javaworld/jw-08-2004/jw-0816-command.html
 */
public class ATest1 {

  public static void main(String args[]) {

    Options opt = new Options(args, 2, 2);
    
    opt.getSet().addOption("a").addOption("b", Separator.EQUALS).addOption("p", Separator.BLANK);
    opt.getSet().addOption("c", true, Separator.BLANK).addOption("d", true, Separator.COLON);
    opt.getSet().addOption("D", true, Separator.EQUALS, Multiplicity.ZERO_OR_MORE);

    boolean result = opt.check();

    System.out.println(opt);

    System.out.println("Check result: " + result);
    System.out.println("Check result:");
    System.out.println(opt.getCheckErrors());

    OptionData od = opt.getSet().getOption("a");
    for (int i = 0; i < od.getResultCount(); i++) 
      System.out.println("a - " + i + " : result: " + od.getResultDetail(i) + " / " + od.getResultValue(i));

    od = opt.getSet().getOption("b");
    for (int i = 0; i < od.getResultCount(); i++) 
      System.out.println("b - " + i + " : result: " + od.getResultDetail(i) + " / " + od.getResultValue(i));

    od = opt.getSet().getOption("p");
    for (int i = 0; i < od.getResultCount(); i++) 
      System.out.println("p - " + i + " : result: " + od.getResultDetail(i) + " / " + od.getResultValue(i));

    od = opt.getSet().getOption("c");
    for (int i = 0; i < od.getResultCount(); i++) 
      System.out.println("c - " + i + " : result: " + od.getResultDetail(i) + " / " + od.getResultValue(i));

    od = opt.getSet().getOption("d");
    for (int i = 0; i < od.getResultCount(); i++) 
      System.out.println("d - " + i + " : result: " + od.getResultDetail(i) + " / " + od.getResultValue(i));

    od = opt.getSet().getOption("D");
    for (int i = 0; i < od.getResultCount(); i++) 
      System.out.println("D - " + i + " : result: " + od.getResultDetail(i) + " / " + od.getResultValue(i));

    for (String s : opt.getSet().getData()) 
      System.out.println("Data: " + s);

    for (String s : opt.getSet().getUnmatched()) 
      System.out.println("Unmatched: " + s);

  }

}

