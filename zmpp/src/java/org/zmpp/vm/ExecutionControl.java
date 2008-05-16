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
    
    // ZMPP should support everything by default
    enableHeaderFlag(Attribute.SUPPORTS_SCREEN_SPLITTING);
    enableHeaderFlag(Attribute.DEFAULT_FONT_IS_VARIABLE);
    enableHeaderFlag(Attribute.SUPPORTS_BOLD);
    enableHeaderFlag(Attribute.SUPPORTS_FIXED_FONT);
    enableHeaderFlag(Attribute.SUPPORTS_COLOURS);
    enableHeaderFlag(Attribute.SUPPORTS_ITALIC);
    enableHeaderFlag(Attribute.SUPPORTS_STATUSLINE);
    enableHeaderFlag(Attribute.SUPPORTS_TIMED_INPUT);
    machine.getGameData().getStoryFileHeader().setDefaultBackgroundColor(
      ScreenModel.COLOR_WHITE);
    machine.getGameData().getStoryFileHeader().setDefaultForegroundColor(
      ScreenModel.COLOR_BLACK);
  }

  private void enableHeaderFlag(Attribute attr) {
    getFileHeader().setEnabled(attr, true);
  }
  
  private StoryFileHeader getFileHeader() {
    return machine.getGameData().getStoryFileHeader();
  }
  
  public int getVersion() { return machine.getVersion(); }
  
  public void resizeScreen(int numRows, int numCharsPerRow) {
    if (getVersion() == 4) {
      getFileHeader().setScreenHeight(numRows);
      getFileHeader().setScreenWidth(numCharsPerRow);
    } else if (getVersion() >= 5) {
      getFileHeader().setFontHeight(1);
      getFileHeader().setFontWidth(1);
      getFileHeader().setScreenHeightUnits(numRows);
      getFileHeader().setScreenWidthUnits(numCharsPerRow);
    }
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
}