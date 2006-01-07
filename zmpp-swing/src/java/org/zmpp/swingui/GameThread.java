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

import org.zmpp.vm.Instruction;
import org.zmpp.vm.Machine;

public class GameThread extends Thread {

  private TextViewport viewport;
  private Machine machine;
  
  public GameThread(Machine machine, TextViewport viewport) {
    
    this.machine = machine;
    this.viewport = viewport;
  }
  
  public void run() {
    
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
        
    while (machine.isRunning()) {
      
      Instruction instr = machine.nextStep();
      boolean debug = false;
      if (debug) {
        System.out.println(String.format("%05x: %s", machine.getProgramCounter(),
                           instr.toString()));
      }
      instr.execute();
    }
  }
}
