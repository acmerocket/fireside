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
package org.zmpp.vm;

import org.zmpp.io.LineBufferInputStream;
import java.io.IOException;
import org.zmpp.vm.Machine.MachineRunState;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.vm.StoryFileHeader.Attribute;

/**
 * This is the execution control instance. Execution is handled by temporarily
 * suspending the VM on an input instruction, resuming after the input
 * buffer was filled and picking up from there.
 * This is the main public interface to the user interface.
 * 
 * TODO:
 * 
 * - initStruct initialization completely in here
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ExecutionControl {

  private Machine machine;
  private InstructionDecoder instructionDecoder =
          new InstructionDecoder();
  private LineBufferInputStream inputStream = new LineBufferInputStream();
  private int step = 1;
  private static final boolean DEBUG = false;
  
  /**
   * Constructor.
   * @param machine
   * @param view
   */
  public ExecutionControl(MachineInitStruct initStruct)
      throws IOException, InvalidStoryException {
    initStruct.keyboardInputStream = inputStream;
    MachineFactory factory = new MachineFactory(initStruct);
    machine = factory.buildMachine();
    machine.start();
    instructionDecoder.initialize(machine);
    int version = machine.getVersion();
    // ZMPP should support everything by default
    if (version <= 3) {
      enableHeaderFlag(Attribute.DEFAULT_FONT_IS_VARIABLE);
      enableHeaderFlag(Attribute.SUPPORTS_STATUSLINE);
      enableHeaderFlag(Attribute.SUPPORTS_SCREEN_SPLITTING);
    }
    if (version >= 4) {
      enableHeaderFlag(Attribute.SUPPORTS_BOLD);
      enableHeaderFlag(Attribute.SUPPORTS_FIXED_FONT);
      enableHeaderFlag(Attribute.SUPPORTS_ITALIC);
      enableHeaderFlag(Attribute.SUPPORTS_TIMED_INPUT);
    }
    if (version >= 5) {
      enableHeaderFlag(Attribute.SUPPORTS_COLOURS);
      
    }
    System.out.println("DEFAULT FOREGROUND: " + getFileHeader().getDefaultForeground());
    System.out.println("DEFAULT BACKGROUND: " + getFileHeader().getDefaultBackground());
  }

  private void enableHeaderFlag(Attribute attr) {
    getFileHeader().setEnabled(attr, true);
  }
  
  private StoryFileHeader getFileHeader() { return machine.getFileHeader(); }
  
  public int getVersion() { return machine.getVersion(); }
  
  public void setDefaultColors(int defaultBackground, int defaultForeground) {
    getFileHeader().setDefaultBackground(defaultBackground);
    getFileHeader().setDefaultForeground(defaultForeground);
  }
  
  public int getDefaultBackground() {
    return getFileHeader().getDefaultBackground();
  }
  
  public int getDefaultForeground() {
    return getFileHeader().getDefaultForeground();
  }

  public void resizeScreen(int numRows, int numCharsPerRow) {
    if (getVersion() >= 4) {
      getFileHeader().setScreenHeight(numRows);
      getFileHeader().setScreenWidth(numCharsPerRow);
    }
    if (getVersion() >= 5) {
      getFileHeader().setFontHeight(1);
      getFileHeader().setFontWidth(1);
      getFileHeader().setScreenHeightUnits(numRows);
      getFileHeader().setScreenWidthUnits(numCharsPerRow);
    }
  }

  public MachineRunState run() {
    while (machine.getRunState() != MachineRunState.STOPPED) {
      int pc = machine.getPC();
      Instruction instr = instructionDecoder.decodeInstruction(pc);
      // if the print is executed after execute(), the result is different !!
      if (DEBUG && machine.getRunState() == MachineRunState.RUNNING)
        System.out.printf("%03d: $%04x %s\n", step, pc, instr.toString());
      instr.execute();
        
      // handle input situations here
      if (machine.getRunState() == MachineRunState.READ_LINE) {
        break;
      } else if (machine.getRunState() == MachineRunState.READ_CHAR) {
        break;
      } else {
        step++;
      }
    }
    return machine.getRunState();
  }
  
  public MachineRunState resumeWithInput(String input) {
    inputStream.addInputLine(convertToZsciiInputLine(input));
    return run();
  }

  private String convertToZsciiInputLine(String input) {
    return input + "\r";
  }

  // ************************************************************************
  // ****** Interrupt functions
  // ****** These are for timed input.
  // *************************************
  
  /**
   * The flag to indicate interrupt execution.
   */
  private boolean executeInterrupt;
  
  /**
   * Indicates if the last interrupt routine performed any output.
   * 
   * @return true if the routine performed output, false otherwise
   */
  public boolean interruptDidOutput() {
    
    return interruptDidOutput;
  }
  
  /**
   * The flag to indicate interrupt output.
   */
  private boolean interruptDidOutput;
  
 /**
   * Calls the specified interrupt routine.
   * 
   * @param routineAddress the routine address
   * @return the return value
   */
  public short callInterrupt(final int routineAddress) {
    
    interruptDidOutput = false;
    executeInterrupt = true;
    final int originalRoutineStackSize = machine.getRoutineContexts().size();
    final RoutineContext routineContext =  machine.call(routineAddress,
        machine.getPC(),
        new short[0], (short) RoutineContext.DISCARD_RESULT);
    
    for (;;) {
      final Instruction instr =
        instructionDecoder.decodeInstruction(machine.getPC());
      instr.execute();
      // check if something was printed
      if (instr.isOutput()) {
        interruptDidOutput = true;
      }
      if (machine.getRoutineContexts().size() == originalRoutineStackSize) {
        break;
      }
    }
    executeInterrupt = false;
    return routineContext.getReturnValue();
  }  

  public void setInterruptRoutine(final int routineAddress) {
    
    // TODO
  }
  
  /**
   * Returns the interrupt status of the cpu object.
   * 
   * @return the interrup status
   */
  public boolean isExecutingInterrupt() { return executeInterrupt; }
}
