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
public class Example2 {

  public static void main(String args[]) {

    Options opt = new Options(args, 2);
    
    opt.addSet("cset").addOption("c").addOption("D", true, Separator.EQUALS, Multiplicity.ZERO_OR_MORE);
    opt.addSet("aset", 1, 3).addOption("a").addOption("check", Multiplicity.ZERO_OR_ONE);
    opt.addSet("dset", 2, 4).addOption("d").addOption("k", Separator.BLANK).addOption("t", Separator.BLANK);

    opt.addOptionAllSets("v", Multiplicity.ZERO_OR_ONE);

    OptionSet set = opt.getMatchingSet();

    if (set == null) {
      // Print usage hints
      System.exit(1);
    }

    if (set.isSet("v")) {
      System.out.println("v is set");
    }

    // Normal processing

    if (set.getSetName().equals("cset")) {
      // React to the first set
      System.out.println("cset");
      for (String d : set.getData())
        System.out.println(d);
      OptionData d = set.getOption("D");
      for (int i = 0; i < d.getResultCount(); i++) {
        System.out.println("D detail " + i + " : " + d.getResultDetail(i));
        System.out.println("D value  " + i + " : " + d.getResultValue(i));
      }
    } else if (set.getSetName().equals("aset")) {
      // React to the second set
      System.out.println("aset");
      for (String d : set.getData())
        System.out.println(d);
      if (set.isSet("check")) {
        System.out.println("check is set");
      }
    } else {
      // We know it has to be the third set now
      System.out.println("dset");
      for (String d : set.getData())
        System.out.println(d);
      System.out.println(set.getOption("k").getResultValue(0));
      System.out.println(set.getOption("t").getResultValue(0));
    }

  }

}

