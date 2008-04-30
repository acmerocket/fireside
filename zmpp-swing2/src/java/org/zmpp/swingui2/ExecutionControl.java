/*
 * $Id$
 * 
 * Created on 2008/04/25
 * Copyright 2005-2008 by Wei-ju Wu
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.swingui2;

import org.zmpp.vm.Instruction;
import org.zmpp.vm.Machine;
import org.zmpp.vm.Machine.MachineRunState;

/**
 * This is the execution control instance. Execution is handled by temporarily
 * suspending the VM on an input instruction, resuming after the input
 * buffer was filled and picking up from there.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ExecutionControl {

  private ScreenView view;
  private Machine machine;
  private int step = 1;
  private static final boolean DEBUG = false;
  
  /**
   * Constructor.
   * @param machine
   * @param view
   */
  public ExecutionControl(Machine machine, StdScreenView view) {
    this.view = view;
    this.machine = machine;
    machine.start();
  }
  
  public MachineRunState run() {
    while (machine.getRunState() != MachineRunState.STOPPED) {
      int pc = machine.getCpu().getProgramCounter();
      Instruction instr = machine.getCpu().nextStep();
      // if the print is executed after execute(), the result is different !!
      if (DEBUG && machine.getRunState() == MachineRunState.RUNNING)
        System.out.printf("%03d: $%04x %s\n", step, pc, instr.toString());
      instr.execute();
        
      // handle input situations here
      if (machine.getRunState() == MachineRunState.READ_LINE) {
        // display cursor
        view.setReadLine(true);
        break;
      } else if (machine.getRunState() == MachineRunState.READ_CHAR) {
        view.setReadChar(true);
      } else {
        step++;
      }
    }
    return machine.getRunState();
  }
}
