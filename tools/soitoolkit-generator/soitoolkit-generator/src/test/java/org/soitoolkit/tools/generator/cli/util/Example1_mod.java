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
public class Example1_mod {

  public static void main(String args[]) {

    Options opt = new Options(args, Multiplicity.ZERO_OR_ONE, 2);
    
    opt.getSet().addOption("a").addOption("log", Separator.EQUALS);

    if (!opt.check()) {
      // Print usage hints
      System.exit(1);
    }

    // Normal processing

    if (opt.getSet().isSet("a")) {
      // React to option -a
      System.out.println("a is set");
    }
    if (opt.getSet().isSet("log")) {
      // React to option -log
      String logfile = opt.getSet().getOption("log").getResultValue(0);  
      System.out.println("logfile " + logfile);
    }

    String inpfile = opt.getSet().getData().get(0);
    String outfile = opt.getSet().getData().get(1);
    System.out.println("inpfile " + inpfile);
    System.out.println("outfile " + outfile);

  }

}

