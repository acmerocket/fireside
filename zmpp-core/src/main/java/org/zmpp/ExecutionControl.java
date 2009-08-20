/*
 * Created on 2008/04/25
 * Copyright 2005-2009 by Wei-ju Wu
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
package org.zmpp;

import java.io.IOException;
import java.util.logging.Logger;

import org.zmpp.base.StoryFileHeader;
import org.zmpp.base.StoryFileHeader.Attribute;
import org.zmpp.encoding.IZsciiEncoding;
import org.zmpp.instructions.InstructionDecoder;
import org.zmpp.io.LineBufferInputStream;
import org.zmpp.vm.Instruction;
import org.zmpp.vm.InvalidStoryException;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.MachineRunState;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.MachineFactory.MachineInitStruct;

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

  private static final Logger LOG = Logger.getLogger("org.zmpp.control");
  private Machine machine;
  private InstructionDecoder instructionDecoder =
          new InstructionDecoder();
  private LineBufferInputStream inputStream = new LineBufferInputStream();
  private int step = 1;
  public static final boolean DEBUG = false;
  public static final boolean DEBUG_INTERRUPT = false;

  /**
   * Constructor.
   * @param initStruct initialization data
   * @throws IOException if i/o error occurred
   * @throws InvalidStoryException invalid story file
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

  /**
   * Enables the specified header flag.
   * @param attr the header attribute to enable
   */
  private void enableHeaderFlag(Attribute attr) {
    getFileHeader().setEnabled(attr, true);
  }

  /**
   * Returns the machine object.
   * @return the machine object
   */
  public Machine getMachine() { return machine; }

  /**
   * Returns the file header.
   * @return the file header
   */
  public StoryFileHeader getFileHeader() { return machine.getFileHeader(); }

  /**
   * Returns the story version.
   * @return story version
   */
  public int getVersion() { return machine.getVersion(); }

  /**
   * Sets default colors.
   * @param defaultBackground default foreground color
   * @param defaultForeground default background color
   */
  public void setDefaultColors(int defaultBackground, int defaultForeground) {
    setDefaultBackground(defaultBackground);
    setDefaultForeground(defaultForeground);

    // Also set the default colors in the screen model !!
    machine.getScreen().setBackground(defaultBackground, -1);
    machine.getScreen().setForeground(defaultForeground, -1);
  }

  /**
   * Returns the default background color.
   * @return default background color
   */
  public int getDefaultBackground() {
    return machine.readUnsigned8(StoryFileHeader.DEFAULT_BACKGROUND);
  }

  /**
   * Returns the default foreground color.
   * @return default foreground color
   */
  public int getDefaultForeground() {
    return machine.readUnsigned8(StoryFileHeader.DEFAULT_FOREGROUND);
  }

  /**
   * Sets the default background color.
   * @param color a color
   */
  private void setDefaultBackground(final int color) {
    machine.writeUnsigned8(StoryFileHeader.DEFAULT_BACKGROUND, (char) color);
  }

  /**
   * Sets the default foreground color.
   * @param color a color
   */
  private void setDefaultForeground(final int color) {
    machine.writeUnsigned8(StoryFileHeader.DEFAULT_FOREGROUND, (char) color);
  }

  /**
   * Updates the screen size.
   * @param numRows number of rows
   * @param numCharsPerRow numbers of characters per row
   */
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

  /**
   * The execution loop. It runs until either an input state is reached
   * or the machine is set to stop state.
   * @return the new MachineRunState
   */
  public MachineRunState run() {
    while (machine.getRunState() != MachineRunState.STOPPED) {
      int pc = machine.getPC();
      Instruction instr = instructionDecoder.decodeInstruction(pc);
      // if the print is executed after execute(), the result is different !!
      if (DEBUG && machine.getRunState() == MachineRunState.RUNNING) {
        System.out.println(String.format("%04d: $%05x %s", step, (int) pc,
                           instr.toString()));
      }
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

  /**
   * Resumes from an input state to the run state using the specified Unicode
   * input string.
   * @param input the Unicode input string
   * @return the new MachineRunState
   */
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

  /**
   * Returns the IZsciiEncoding object.
   * @return IZsciiEncoding object
   */
  public IZsciiEncoding getZsciiEncoding() { return machine; }

  /**
   * This method should be called from a timed input method, to fill
   * the text buffer with current input. By using this, it is ensured,
   * the game could theoretically process preliminary input.
   * @param text the input text as Unicode
   */
  public void setTextToInputBuffer(String text) {
    MachineRunState runstate = machine.getRunState();
    if (runstate != null && runstate.isReadLine()) {
      inputStream.addInputLine(convertToZsciiInputLine(text));
      int textbuffer = machine.getRunState().getTextBuffer();
      machine.readLine(textbuffer);
    }
  }

  // ************************************************************************
  // ****** Interrupt functions
  // ****** These are for timed input.
  // *************************************

  /**
   * Indicates if the last interrupt routine performed any output.
   * @return true if the routine performed output, false otherwise
   */
  public boolean interruptDidOutput() { return interruptDidOutput; }

  /**
   * The flag to indicate interrupt output.
   */
  private boolean interruptDidOutput;

  /**
   * Calls the specified interrupt routine.
   * @param routineAddress the routine address
   * @return the return value of the called routine
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
      if (DEBUG_INTERRUPT) {
        System.out.println(String.format("%03d: $%04x %s", step,
                           (int) machine.getPC(), instr.toString()));
      }
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