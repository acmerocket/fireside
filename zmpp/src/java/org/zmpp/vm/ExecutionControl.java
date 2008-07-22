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
import java.util.logging.Logger;
import org.zmpp.encoding.IZsciiEncoding;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.vm.StoryFileHeader.Attribute;

/**
 * This is the execution control instance. Execution is handled by temporarily
 * suspending the VM on an input instruction, resuming after the input
 * buffer was filled and picking up from there.
 * This is the main public interface to the user interface.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ExecutionControl {

  private static final Logger LOG = Logger.getLogger("org.zmpp");
  private Machine machine;
  private InstructionDecoder instructionDecoder =
          new InstructionDecoder();
  private LineBufferInputStream inputStream = new LineBufferInputStream();
  private int step = 1;
  public static boolean DEBUG = true;
  
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
    int defaultForeground = getDefaultForeground();
    int defaultBackground = getDefaultBackground();
    LOG.info("GAME DEFAULT FOREGROUND: " + defaultForeground);
    LOG.info("GAME DEFAULT BACKGROUND: " + defaultBackground);
    machine.getScreen().setBackground(defaultBackground, -1);
    machine.getScreen().setForeground(defaultForeground, -1);
  }

  private void enableHeaderFlag(Attribute attr) {
    getFileHeader().setEnabled(attr, true);
  }
  
  public StoryFileHeader getFileHeader() { return machine.getFileHeader(); }
  
  public int getVersion() { return machine.getVersion(); }
  
  public void setDefaultColors(int defaultBackground, int defaultForeground) {
    setDefaultBackground(defaultBackground);
    setDefaultForeground(defaultForeground);
    
    // Also set the default colors in the screen model !!
    machine.getScreen().setBackground(defaultBackground, -1);
    machine.getScreen().setForeground(defaultForeground, -1);
  }
  
  public int getDefaultBackground() {
    return machine.readUnsigned8(StoryFileHeader.DEFAULT_BACKGROUND);
  }
  public int getDefaultForeground() {
    return machine.readUnsigned8(StoryFileHeader.DEFAULT_FOREGROUND);
  }
  private void setDefaultBackground(final int color) {
    machine.writeUnsigned8(StoryFileHeader.DEFAULT_BACKGROUND, (char) color);
  }  
  private void setDefaultForeground(final int color) {
    machine.writeUnsigned8(StoryFileHeader.DEFAULT_FOREGROUND, (char) color);
  }
 
  

  public void resizeScreen(int numRows, int numCharsPerRow) {
    if (getVersion() >= 4) {
      machine.writeUnsigned8(StoryFileHeader.SCREEN_HEIGHT, (char) numRows);
      machine.writeUnsigned8(StoryFileHeader.SCREEN_WIDTH,
                             (char) numCharsPerRow);
    }
    if (getVersion() >= 5) {
      getFileHeader().setFontHeight(1);
      getFileHeader().setFontWidth(1);
      machine.writeUnsigned16(StoryFileHeader.SCREEN_HEIGHT_UNITS,
                              (char) numRows);
      machine.writeUnsigned16(StoryFileHeader.SCREEN_WIDTH_UNITS,
                              (char) numCharsPerRow);
    }
  }

  public MachineRunState run() {
    while (machine.getRunState() != MachineRunState.STOPPED) {
      int pc = machine.getPC();
      Instruction instr = instructionDecoder.decodeInstruction(pc);
      // if the print is executed after execute(), the result is different !!
      if (DEBUG && machine.getRunState() == MachineRunState.RUNNING)
        System.out.println(String.format("%03d: $%04x %s", step, (int) pc, instr.toString()));
      instr.execute();
        
      // handle input situations here
      if (machine.getRunState().isWaitingForInput()) {
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

  /**
   * Downcase the input string and convert to ZSCII.
   * @param input the input string
   * @return the converted input string
   */
  private String convertToZsciiInputLine(String input) {
    return machine.convertToZscii(input.toLowerCase()) + "\r";
  }
  
  public IZsciiEncoding getZsciiEncoding() { return machine; }

  // ************************************************************************
  // ****** Interrupt functions
  // ****** These are for timed input.
  // *************************************

  /**
   * Indicates if the last interrupt routine performed any output.
   * 
   * @return true if the routine performed output, false otherwise
   */
  public boolean interruptDidOutput() { return interruptDidOutput; }
  
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
  public char callInterrupt(final char routineAddress) {
    interruptDidOutput = false;
    final int originalRoutineStackSize = machine.getRoutineContexts().size();
    final RoutineContext routineContext =  machine.call(routineAddress,
        machine.getPC(),
        new char[0], RoutineContext.DISCARD_RESULT);
    
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
    return routineContext.getReturnValue();
  }
}
