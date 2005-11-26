/*
 * $Id$
 * 
 * Created on 15.11.2005
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

import javax.swing.SwingUtilities;

import jozart.swingutils.SwingWorker;

import org.zmpp.vm.Instruction;
import org.zmpp.vm.Machine;

public class GameThread extends SwingWorker<Boolean> {

  private TextViewport viewport;
  private Machine machine;
  
  public GameThread(Machine machine, TextViewport viewport) {
    
    this.machine = machine;
    this.viewport = viewport;
  }
  
  public Boolean construct() {
    
    viewport.waitInitialized();
    
    machine.start();
    
    // on MacOS X, after running the thread keyboard input is suspended
    // for some reason until you either change to another application and
    // back or explicitly request the focus, therefore, do it here, it
    // does no harm on other platforms
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        
        public void run() {
          
          viewport.requestFocus();
        }
      });
    } catch (Exception ex) { }
    
    /*
    if (DEBUG) {
      
      System.out.println("Object tree:\n");
      ObjectTree objTree = machine.getObjectTree();
      int numObjects = objTree.getNumObjects();
      for (int i = 1; i <= numObjects; i++) {
      
        ZObject obj = objTree.getObject(i);
        System.out.println(i + ": " + obj.toString());      
      }
      
      System.out.println("Dictionary:\n" + machine.getDictionary().toString());
    }*/
    
    while (machine.isRunning()) {
      
      Instruction instr = machine.nextStep();
      //System.out.println(String.format("%05x: %s", machine.getProgramCounter(),
      //    instr.toString()));
      instr.execute();
    }
    return Boolean.TRUE;
  }
  
  protected void finished() {
    
    //viewport.printString("*Game ended*");
  }
}
