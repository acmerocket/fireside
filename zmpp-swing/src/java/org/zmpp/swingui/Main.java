/*
 * $Id$
 * 
 * Created on 17.10.2005
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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.zmpp.instructions.DefaultInstructionDecoder;
import org.zmpp.io.FileInputStream;
import org.zmpp.io.TranscriptOutputStream;
import org.zmpp.vm.DefaultMachineConfig;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineConfig;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vm.MemoryOutputStream;
import org.zmpp.vm.StoryFileHeader;


/**
 * This class starts the ZMPP swing interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    String mrjVersion = System.getProperty("mrj.version");
    if (mrjVersion != null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode",
          "false");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          "ZMPP");
    }
    
    File currentdir = new File(System.getProperty("user.dir"));    
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle("Open story file...");
    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      
      File storyfile = fileChooser.getSelectedFile();
      
      Machine machine = openStoryFile(storyfile);
      Screen3 screen = new Screen3(machine);
      
      // Machine initialization
      
      // Input streams
      FileInputStream fileIs = new FileInputStream(screen);      
      machine.setInputStream(0, screen);
      machine.setInputStream(1, fileIs);
      //machine.selectInputStream(1);
      
      // Output streams
      machine.setOutputStream(1, screen.getOutputStream());
      machine.selectOutputStream(1, true);
      TranscriptOutputStream transcriptStream = new TranscriptOutputStream(screen);
      machine.setOutputStream(2, transcriptStream);
      machine.selectOutputStream(2, false);
      machine.setOutputStream(3, new MemoryOutputStream(machine));
      machine.selectOutputStream(3, false);
      
      machine.setStatusLine(screen);
      machine.setScreen(screen.getScreenModel());
      machine.setSaveGameDataStore(screen);
      //machine.getStoryFileHeader().setForceFixedFont(true);
          
      screen.startMachine();
      screen.pack();
      screen.setVisible(true);
    }
  }
  
  private static Machine openStoryFile(File storyfile) {
    
    java.io.InputStream inputstream = null;
    
    try {
      
      inputstream = new java.io.FileInputStream(storyfile);
      MachineConfig config = new DefaultMachineConfig(inputstream);
      StoryFileHeader fileheader = config.getFileHeader();
      System.out.println("Story file Version: " + fileheader.getVersion());
    
      if (fileheader.getVersion() < 3 || fileheader.getVersion() == 6
          || fileheader.getVersion() > 8) {
      
        JOptionPane.showMessageDialog(null,
          "ZMPP V 0.75 does not support story file versions 1, 2 or 6.",
          "Story file read error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
      }
      Machine machine = new MachineImpl();
      InstructionDecoder decoder = new DefaultInstructionDecoder();
      machine.initialize(config, decoder);
      return machine;
      
    } catch (Exception ex) {
      
      ex.printStackTrace();
      
    } finally {
      
      try {
        
        if (inputstream != null) inputstream.close();
        
      } catch (Exception ex) {
        
        ex.printStackTrace();
      }
    }
    return null;
  }
}
