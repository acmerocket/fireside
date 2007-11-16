/*
 * $Id$
 *
 * Copyright 2005-2007 by Wei-ju Wu
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
package test.zmpp.instructions;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.jmock.Mock;
import org.zmpp.blorb.BlorbImage;
import org.zmpp.instructions.ExtendedInstruction;
import org.zmpp.instructions.ExtendedStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.media.PictureManager;
import org.zmpp.vm.ScreenModel6;
import org.zmpp.vm.Window6;

public class InstructionExtV6Test extends InstructionTestBase {

  private Mock mockscreen6, mockwindow6, mockPictureManager;
  
  private ScreenModel6 screen6;
  private Window6 window6;
  private PictureManager picturemanager;
  
  protected void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(6));
    mockscreen6 = mock(ScreenModel6.class);
    screen6 = (ScreenModel6) mockscreen6.proxy();
    mockPictureManager = mock(PictureManager.class);
    picturemanager = (PictureManager) mockPictureManager.proxy();
    mockwindow6 = mock(Window6.class);
    window6 = (Window6) mockwindow6.proxy();
  }

  // **************************************************************************
  // ******** MOUSE_WINDOW
  // **********************************
  
  public void testMouseWindow() {
    
    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("setMouseWindow").with(eq(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction mouse_window = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_MOUSE_WINDOW);
    mouse_window.setLength(3);
    mouse_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    mouse_window.execute();
  }

  // **************************************************************************
  // ******** PICTURE_DATA
  // **********************************
  
  public void testPictureDataGetPictureFileInfo() {
    
    mockMachine.expects(atLeastOnce()).method("getPictureManager").will(returnValue(picturemanager));
    mockPictureManager.expects(once()).method("getRelease").will(returnValue(2));
    mockPictureManager.expects(atLeastOnce()).method("getNumPictures").will(returnValue(32));
    mockMachine.expects(atLeastOnce()).method("getGameData")
    	.will(returnValue(gamedata));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(1000), eq(32));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(1002), eq(2));

    // Branch
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("computeBranchTarget").with(eq((short) 123), eq(3)).will(returnValue(1234));
    mockCpu.expects(once()).method("setProgramCounter").with(eq(1234));
    
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    picture_data.setBranchOffset((short) 123);
    
    // the 0 value indicates that we want to retrieve the information about
    // the picture file
    picture_data.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    
    // The target array
    picture_data.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1000));
    picture_data.execute();
  }

  public void testPictureDataGetPictureInfo() {
    
    mockMachine.expects(atLeastOnce()).method("getPictureManager").will(returnValue(picturemanager));
    mockPictureManager.expects(once()).method("getPictureSize").with(eq(1)).will(returnValue(new Dimension(320, 200)));
    mockMachine.expects(atLeastOnce()).method("getGameData")
  		.will(returnValue(gamedata));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(1000), eq(200));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(1002), eq(320));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    
    // computes a branch
    mockCpu.expects(atLeastOnce()).method("computeBranchTarget").with(eq((short) 42), eq(3)).will(returnValue(4711));
    mockCpu.expects(atLeastOnce()).method("setProgramCounter").with(eq(4711));
    
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    
    // the 1 value indicates that we want to retrieve the information about
    // the picture number 1
    picture_data.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    
    // The target array
    picture_data.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1000));
    
    picture_data.setBranchIfTrue(true);
    picture_data.setBranchOffset((short) 42);
    
    picture_data.execute();
  }

  public void testPictureDataGetPictureNonExistent() {
    
    mockMachine.expects(atLeastOnce()).method("getPictureManager").will(returnValue(picturemanager));
    mockPictureManager.expects(once()).method("getPictureSize").with(eq(55)).will(returnValue(null));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    
    // the 55 value indicates that we want to retrieve the information about
    // the picture number 55, which in this test does not exist
    picture_data.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 55));
    
    // The target array
    picture_data.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1000));    
    picture_data.execute();
  }

  // **************************************************************************
  // ******** DRAW_PICTURE
  // **********************************

  public void testDrawPicture() {
    
    BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_4BYTE_ABGR);
    BlorbImage blorbimage = new BlorbImage(image);
    
    mockMachine.expects(atLeastOnce()).method("getPictureManager").will(returnValue(picturemanager));
    mockPictureManager.expects(once()).method("getPicture").with(eq(1)).will(returnValue(blorbimage));
    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getSelectedWindow").will(returnValue(window6));
    mockwindow6.expects(once()).method("drawPicture").with(eq(blorbimage), eq(2), eq(3));
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction draw_picture = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_DRAW_PICTURE);
    draw_picture.setLength(3);
    
    draw_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    draw_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    draw_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    
    draw_picture.execute();
  }
  
  // **************************************************************************
  // ******** ERASE_PICTURE
  // **********************************

  public void testErasePicture() {
    
    BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_4BYTE_ABGR);
    BlorbImage blorbimage = new BlorbImage(image);
    
    mockMachine.expects(atLeastOnce()).method("getPictureManager").will(returnValue(picturemanager));
    mockPictureManager.expects(once()).method("getPicture").with(eq(1)).will(returnValue(blorbimage));
    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getSelectedWindow").will(returnValue(window6));
    mockwindow6.expects(once()).method("erasePicture").with(eq(blorbimage), eq(2), eq(3));
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction erase_picture = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ERASE_PICTURE);
    erase_picture.setLength(3);
    
    erase_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    erase_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    
    erase_picture.execute();
  }
  // **************************************************************************
  // ******** MOVE_WINDOW
  // **********************************
  
  public void testMoveWindow() {

    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getWindow").with(eq(1)).will(returnValue(window6));
    mockwindow6.expects(once()).method("move").with(eq(2), eq(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction move_window = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_MOVE_WINDOW);
    move_window.setLength(3);
    
    move_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    move_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    move_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    
    move_window.execute();
  }

  // **************************************************************************
  // ******** WINDOW_SIZE
  // **********************************

  public void testWindowSize() {

    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getWindow").with(eq(1)).will(returnValue(window6));
    mockwindow6.expects(once()).method("setSize").with(eq(2), eq(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction window_size = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_WINDOW_SIZE);
    window_size.setLength(3);
    
    window_size.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    window_size.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    window_size.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    
    window_size.execute();
  }
  
  // **************************************************************************
  // ******** WINDOW_STYLE
  // **********************************
  
  public void testWindowStyle() {

    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getWindow").with(eq(1)).will(returnValue(window6));
    mockwindow6.expects(once()).method("setStyle").with(eq(2), eq(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction window_style = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_WINDOW_STYLE);
    window_style.setLength(3);
    
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    
    window_style.execute();
  }

  public void testWindowStyle2Params() {

    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getWindow").with(eq(1)).will(returnValue(window6));
    mockwindow6.expects(once()).method("setStyle").with(eq(2), eq(0));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction window_style = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_WINDOW_STYLE);
    window_style.setLength(3);
    
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    
    window_style.execute();
  }
  
  // **************************************************************************
  // ******** SET_MARGINS
  // **********************************
  
  public void testSetMargins() {
    
    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getWindow").with(eq(3)).will(returnValue(window6));
    mockwindow6.expects(once()).method("setMargins").with(eq(1), eq(2));
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction set_margins = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SET_MARGINS);
    set_margins.setLength(3);
    
    set_margins.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    set_margins.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_margins.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    
    set_margins.execute();
  }

  // **************************************************************************
  // ******** PICTURE_TABLE
  // **********************************
  
  public void testPictureTable() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction picture_table = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_TABLE);
    picture_table.setLength(3);
    picture_table.execute();
  }

  // **************************************************************************
  // ******** PUSH_STACK
  // **********************************
  
  public void testPushStackStdStack() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0), eq((short) 11));
    mockCpu.expects(atLeastOnce()).method("computeBranchTarget").with(eq((short) 13), eq(3)).will(returnValue(5412));
    mockCpu.expects(atLeastOnce()).method("setProgramCounter").with(eq(5412));
    
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PUSH_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 11));
    push_stack.setLength(3);
    push_stack.setBranchOffset((short) 13);
    push_stack.execute();
  }
  
  public void testPushStackUserStackOk() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("pushUserStack").with(eq(1112), eq((short) 11)).will(returnValue(true));
    mockCpu.expects(atLeastOnce()).method("computeBranchTarget").with(eq((short) 13), eq(3)).will(returnValue(5412));
    mockCpu.expects(atLeastOnce()).method("setProgramCounter").with(eq(5412));
    
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PUSH_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 11));
    push_stack.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1112));
    push_stack.setLength(3);
    push_stack.setBranchOffset((short) 13);
    push_stack.execute();
  }

  public void testPushStackUserStackOverflow() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("pushUserStack").with(eq(1112), eq((short) 11)).will(returnValue(false));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PUSH_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 11));
    push_stack.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1112));
    push_stack.setLength(3);
    push_stack.setBranchOffset((short) 13);
    push_stack.execute();
  }
  
  // **************************************************************************
  // ******** POP_STACK
  // **********************************
  
  public void testPopStackStdStack() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(exactly(5)).method("getVariable").with(eq(0)).will(returnValue((short) 11));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_POP_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 5));
    push_stack.setLength(3);
    push_stack.execute();
  }
  
  public void testPopStackUserStack() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(exactly(3)).method("popUserStack").with(eq(1113)).will(returnValue((short) 11));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_POP_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 3));
    push_stack.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1113));
    push_stack.setLength(3);
    push_stack.execute();
  }

  // **************************************************************************
  // ******** SCROLL_WINDOW
  // **********************************
  
  public void testScrollWindow() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockMachine.expects(once()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getWindow").with(eq(2)).will(returnValue(window6));
    mockwindow6.expects(once()).method("scroll").with(eq(5));
    
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction scroll_window = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SCROLL_WINDOW);
    scroll_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    scroll_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 5));
    scroll_window.setLength(3);
    scroll_window.execute();
  }
}
