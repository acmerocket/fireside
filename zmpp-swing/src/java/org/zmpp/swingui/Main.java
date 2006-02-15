/*
 * $Id$
 * 
 * Created on 2005/10/17
 * Copyright 2005-2006 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.swingui;

import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

/**
 * This class starts the ZMPP swing interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Main {

  /**
   * The application name.
   */
  public static final String APPNAME =
    "Z-Machine Preservation Project Version 0.86";
  
  /**
   * The main method.
   * 
   * @param args the arguments
   */
  public static void main(String[] args) {
    
    if (System.getProperty("mrj.version") != null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode",
          "false");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          "ZMPP");
    }
    
    JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
    fileChooser.setDialogTitle("Open story file...");
    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      
      File storyfile = fileChooser.getSelectedFile();
      File blorbfile = searchForResources(storyfile);
      ApplicationMachineFactory factory =
        new ApplicationMachineFactory(storyfile, blorbfile);
      factory.buildMachine();
      ZmppFrame frame = factory.getUI();      
      frame.startMachine();
      frame.pack();
      frame.setVisible(true);
    }
  }
  
  /**
   * Trys to find a resource file in Blorb format.
   * 
   * @param storyfile the storyfile
   * @return the blorb file if one exists or null
   */
  private static File searchForResources(File storyfile) {
    
    StringTokenizer tok = new StringTokenizer(storyfile.getName(), ".");
    String prefix = tok.nextToken();
    String blorbpath = storyfile.getParent()
                       + System.getProperty("file.separator")
                       + prefix + ".blb";

    File blorbfile = new File(blorbpath);
    System.out.printf("does '%s' exist ? -> %b\n", blorbfile.getPath(),
        blorbfile.exists());
    if (blorbfile.exists()) return blorbfile;
    return null;
  }  
}
