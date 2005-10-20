/*
 * $Id$
 * 
 * Created on 17.10.2005
 * Copyright 2005 by Wei-ju Wu
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
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JFrame;

import org.zmpp.base.MemoryAccess;
import org.zmpp.vm.Abbreviations;
import org.zmpp.vm.Machine3;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vmutil.ZsciiConverter;
import org.zmpp.vmutil.ZsciiString;

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
    
    if (args.length == 0) {
      
      System.out.println("usage: java org.zmpp.swingui.Main <storyfile>");
    }
    try {
      File storyfile = new File(args[0]);
      RandomAccessFile file = new RandomAccessFile(storyfile, "r");
      int fileSize = (int) file.length();
      byte[] buffer = new byte[fileSize];    
      file.read(buffer);
      file.close();
      MemoryAccess memaccess = new MemoryAccess(buffer);
      StoryFileHeader fileheader = new StoryFileHeader(memaccess);
      Abbreviations abbreviations = new Abbreviations(memaccess,
        fileheader.getAbbreviationsAddress());
      ZsciiConverter converter = new ZsciiConverter(3, abbreviations);
      ZsciiString.setZsciiConverter(converter);      
      Machine3 machineState = new Machine3();
      machineState.initialize(memaccess, fileheader);
      
      JFrame frame = new Screen3(machineState);
      frame.pack();
      frame.setVisible(true);
            
    } catch (IOException ex) {
      
      ex.printStackTrace();
    }
  }
}
