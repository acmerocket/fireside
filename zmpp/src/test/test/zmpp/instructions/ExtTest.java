/*
 * $Id: ExtendedInstructionTest.java 520 2007-11-13 19:14:51Z weiju $
 * 
 * Created on 12/08/2005
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
package test.zmpp.instructions;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.blorb.BlorbImage;
import org.zmpp.blorb.NativeImage;
import org.zmpp.instructions.ExtInstruction;
import org.zmpp.instructions.InstructionInfoDb;
import org.zmpp.instructions.Operand;
import org.zmpp.media.PictureManager;
import org.zmpp.media.Resolution;
import org.zmpp.vm.Machine;
import org.zmpp.windowing.ScreenModel6;
import org.zmpp.windowing.Window6;
import static org.zmpp.base.MemoryUtil.*;
import static org.zmpp.vm.Instruction.*;
import static org.zmpp.vm.Instruction.OperandCount.*;

/**
 * This class tests the dynamic and static aspects of the extended
 * instructions.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class ExtTest extends InstructionTestBase {

  private ScreenModel6 screen6;
  private Window6 window6;
  private PictureManager picturemanager;
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    screen6 = context.mock(ScreenModel6.class);
    picturemanager = context.mock(PictureManager.class);
    window6 = context.mock(Window6.class);
  }

  @Test
  public void testStoresResult() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(EXT, EXT_SAVE_UNDO, 5).isStore());
    assertTrue(infoDb.getInfo(EXT, EXT_RESTORE_UNDO, 5).isStore());
    assertTrue(infoDb.getInfo(EXT, EXT_LOG_SHIFT, 5).isStore());
    assertTrue(infoDb.getInfo(EXT, EXT_ART_SHIFT, 5).isStore());
    assertTrue(infoDb.getInfo(EXT, EXT_SET_FONT, 5).isStore());
  }
  @Test
  public void testSaveUndoSuccess() {
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save_undo(with(any(char.class))); will(returnValue(true));
      atLeast(1).of (machine).setVariable((char) 0, (char) 1);
    }});
    ExtMock save_undo = createExtMock(EXT_SAVE_UNDO, new Operand[0]);
    save_undo.execute();
    assertTrue(save_undo.nextInstructionCalled);
  }
  @Test
  public void testSaveUndoFail() {
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save_undo(with(any(char.class))); will(returnValue(false));
      atLeast(1).of (machine).setVariable((char) 0, (char) 0);
    }});
    ExtMock save_undo = createExtMock(EXT_SAVE_UNDO, new Operand[0]);
    save_undo.execute();
    assertTrue(save_undo.nextInstructionCalled);
  }
  
  // **************************************************************************
  // ******** ART_SHIFT
  // **********************************

  @Test
  public void testArtShift0() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).setVariable((char) 1, (char) 12);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0);
    char storevar = 0x01;
    ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] {op1, op2}, storevar);
    art_shift.execute();
    assertTrue(art_shift.nextInstructionCalled);
  }

  @Test
  public void testArtShiftPositivePositiveShift() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).setVariable((char) 1, (char) 24);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    char storevar = 0x01;
    ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] {op1, op2}, storevar);
    art_shift.execute();
    assertTrue(art_shift.nextInstructionCalled);
  }
  @Test
  public void testArtShiftNegativePositiveShift() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).setVariable((char) 1, signedToUnsigned16((short) -24));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT,
            signedToUnsigned16((short) -12));
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    char storevar = 0x01;
    ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] {op1, op2}, storevar);
    art_shift.execute();
    assertTrue(art_shift.nextInstructionCalled);
  }    

  @Test
  public void testArtShiftPositiveNegativeShift() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).setVariable((char) 1, (char) 6);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -1));
    char storevar = 0x01;
    ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] {op1, op2}, storevar);
    art_shift.execute();
    assertTrue(art_shift.nextInstructionCalled);
  }    

  @Test
  public void testArtShiftNegativeNegativeShift() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).setVariable((char) 1, signedToUnsigned16((short) -6));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -12));
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -1));
    char storevar = 0x01;
    ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] {op1, op2}, storevar);
    art_shift.execute();
    assertTrue(art_shift.nextInstructionCalled);
  }
  
  // **************************************************************************
  // ******** LOG_SHIFT
  // **********************************

  // TODO
  // **************************************************************************
  // ******** Version 6
  // **********************************
  
  // **************************************************************************
  // ******** MOUSE_WINDOW
  // **********************************
  
  @Test
  public void testMouseWindow() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).setMouseWindow(3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    ExtMock mouse_window = createExtMock(EXT_MOUSE_WINDOW, new Operand[] {op1});
    mouse_window.execute();
    assertTrue(mouse_window.nextInstructionCalled);
  }

  // **************************************************************************
  // ******** PICTURE_DATA
  // **********************************

  @Test
  public void testPictureDataGetPictureFileInfo() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getRelease(); will(returnValue(2));
      atLeast(1).of (picturemanager).getNumPictures(); will(returnValue(32));
      one (machine).writeUnsigned16(1000, (char) 32);
      one (machine).writeUnsigned16(1002, (char) 2);
    }});
    // the 0 value indicates that we want to retrieve the information about
    // the picture file
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0);
    
    // The target array
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000);
    ExtMock picture_data = createExtMock(EXT_PICTURE_DATA, new Operand[] {op1, op2});
    picture_data.execute();
    assertTrue(picture_data.branchOnTestCalled);
  }

  @Test
  public void testPictureDataGetPictureInfo() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getPictureSize(1); will(returnValue(new Resolution(320, 200)));
      one (machine).writeUnsigned16(1000, (char) 200);
      one (machine).writeUnsigned16(1002, (char) 320);
    }});
    // the 1 value indicates that we want to retrieve the information about
    // the picture number 1
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    
    // The target array
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000);
    
    ExtMock picture_data = createExtMock(EXT_PICTURE_DATA, new Operand[] {op1, op2});
    picture_data.execute();
    assertTrue(picture_data.branchOnTestCalled);
    assertTrue(picture_data.branchOnTestCondition);
  }
  
  @Test
  public void testPictureDataGetPictureNonExistent() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getPictureSize(55); will(returnValue(null));
      //one (machine).incrementPC(3);
    }});
    // the 55 value indicates that we want to retrieve the information about
    // the picture number 55, which in this test does not exist
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 55);
    
    // The target array
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000);    
    ExtMock picture_data = createExtMock(EXT_PICTURE_DATA, new Operand[] {op1, op2});    
    picture_data.execute();
    assertTrue(picture_data.branchOnTestCalled);
    assertFalse(picture_data.branchOnTestCondition);
  }

  // **************************************************************************
  // ******** DRAW_PICTURE
  // **********************************

  @Test
  public void testDrawPicture() {   
    NativeImage image = new NativeImage() {
      public int getWidth() { return 320; }
      public int getHeight() { return 200; }      
    };
    final BlorbImage blorbimage = new BlorbImage(image);
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getPicture(1); will(returnValue(blorbimage));
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getSelectedWindow(); will(returnValue(window6));
      one (window6).drawPicture(blorbimage, 2, 3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    ExtMock draw_picture = createExtMock(EXT_DRAW_PICTURE, new Operand[] {op1, op2, op3});
    draw_picture.execute();
    assertTrue(draw_picture.nextInstructionCalled);
  }
  
  // **************************************************************************
  // ******** ERASE_PICTURE
  // **********************************

  @Test
  public void testErasePicture() {
    NativeImage image = new NativeImage() {
      public int getWidth() { return 320; }
      public int getHeight() { return 200; }      
    };
    final BlorbImage blorbimage = new BlorbImage(image);
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getPicture(1); will(returnValue(blorbimage));
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getSelectedWindow(); will(returnValue(window6));
      one (window6).erasePicture(blorbimage, 2, 3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    ExtMock erase_picture = createExtMock(EXT_ERASE_PICTURE, new Operand[] {op1, op2, op3});
    erase_picture.execute();
    assertTrue(erase_picture.nextInstructionCalled);
  }

  // **************************************************************************
  // ******** MOVE_WINDOW
  // **********************************
  @Test
  public void testMoveWindow() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(1); will(returnValue(window6));
      one (window6).move(2, 3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    ExtMock move_window = createExtMock(EXT_MOVE_WINDOW, new Operand[] {op1, op2, op3});
    move_window.execute();
    assertTrue(move_window.nextInstructionCalled);
  }

  // **************************************************************************
  // ******** WINDOW_SIZE
  // **********************************

  @Test
  public void testWindowSize() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(1); will(returnValue(window6));
      one (window6).setSize(2, 3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    ExtMock window_size = createExtMock(EXT_WINDOW_SIZE, new Operand[] {op1, op2, op3});
    window_size.execute();
    assertTrue(window_size.nextInstructionCalled);
  }
  
  // **************************************************************************
  // ******** WINDOW_STYLE
  // **********************************
  
  @Test
  public void testWindowStyle() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(1); will(returnValue(window6));
      one (window6).setStyle(2, 3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    ExtMock window_style = createExtMock(EXT_WINDOW_STYLE, new Operand[] {op1, op2, op3});
    window_style.execute();
    assertTrue(window_style.nextInstructionCalled);
  }

  @Test
  public void testWindowStyle2Params() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(1); will(returnValue(window6));
      one (window6).setStyle(2, 0);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    ExtMock window_style = createExtMock(EXT_WINDOW_STYLE, new Operand[] {op1, op2});
    window_style.execute();
    assertTrue(window_style.nextInstructionCalled);
  }
  
  // **************************************************************************
  // ******** SET_MARGINS
  // **********************************
  
  @Test
  public void testSetMargins() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(3); will(returnValue(window6));
      one (window6).setMargins(1, 2);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    ExtMock set_margins = createExtMock(EXT_SET_MARGINS, new Operand[] {op1, op2, op3});
    set_margins.execute();
    assertTrue(set_margins.nextInstructionCalled);
  }

  // **************************************************************************
  // ******** PICTURE_TABLE
  // **********************************
  
  @Test
  public void testPictureTable() {
    ExtMock picture_table = createExtMock(EXT_PICTURE_TABLE, new Operand[0]);
    picture_table.execute();
    assertTrue(picture_table.nextInstructionCalled);
  }

  // **************************************************************************
  // ******** PUSH_STACK
  // **********************************
  @Test
  public void testPushStackStdStack() {
    context.checking(new Expectations() {{
      one (machine).pushStack((char) 0, (char) 11); will(returnValue(true));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11);
    ExtMock push_stack = createExtMock(EXT_PUSH_STACK, new Operand[] {op1});
    push_stack.execute();
    assertTrue(push_stack.branchOnTestCalled);
    assertTrue(push_stack.branchOnTestCondition);
  }
  
  @Test
  public void testPushStackUserStackOk() {
    context.checking(new Expectations() {{
      one (machine).pushStack((char) 1112, (char) 11); will(returnValue(true));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11);
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1112);
    ExtMock push_stack = createExtMock(EXT_PUSH_STACK, new Operand[] {op1, op2});
    push_stack.execute();
    assertTrue(push_stack.branchOnTestCalled);
    assertTrue(push_stack.branchOnTestCondition);
  }

  @Test
  public void testPushStackUserStackOverflow() {
    context.checking(new Expectations() {{
      one (machine).pushStack((char) 1112, (char) 11); will(returnValue(false));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11);
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1112);
    ExtMock push_stack = createExtMock(EXT_PUSH_STACK, new Operand[] {op1, op2});
    push_stack.execute();
    assertTrue(push_stack.branchOnTestCalled);
    assertFalse(push_stack.branchOnTestCondition);
  }
  
  // **************************************************************************
  // ******** POP_STACK
  // **********************************
  
  @Test
  public void testPopStackStdStack() {
    context.checking(new Expectations() {{
      exactly(5).of (machine).popStack((char) 0); will(returnValue((char) 11));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    ExtMock pop_stack = createExtMock(EXT_POP_STACK, new Operand[] {op1});
    pop_stack.execute();
    assertTrue(pop_stack.nextInstructionCalled);
  }
  
  @Test
  public void testPopStackUserStack() {
    context.checking(new Expectations() {{
      exactly(3).of (machine).popStack((char) 1113); will(returnValue((char) 11));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1113);
    ExtMock pop_stack = createExtMock(EXT_POP_STACK, new Operand[] {op1, op2});
    pop_stack.execute();
    assertTrue(pop_stack.nextInstructionCalled);
  }

  // **************************************************************************
  // ******** SCROLL_WINDOW
  // **********************************
  
  @Test
  public void testScrollWindow() {
    context.checking(new Expectations() {{
      one (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(2); will(returnValue(window6));
      one (window6).scroll(5);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    ExtMock scroll_window = createExtMock(EXT_SCROLL_WINDOW, new Operand[] {op1, op2});
    scroll_window.execute();
    assertTrue(scroll_window.nextInstructionCalled);    
  }
  // **************************************************************************
  // ******** Private helpers
  // **********************************
  private ExtMock createExtMock(int opcode, Operand[] operands) {
    return createExtMock(machine, opcode, operands, (char) 0);
  }
  private ExtMock createExtMock(int opcode, Operand[] operands, char storevar) {
    return createExtMock(machine, opcode, operands, storevar);
  }
  static ExtMock createExtMock(Machine machine, int opcode, Operand[] operands, char storevar) {
    return new ExtMock(machine, opcode, operands, storevar);
  }

  static class ExtMock extends ExtInstruction {  
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public ExtMock(Machine machine, int opcode, Operand[] operands, char storeVar) {
      super(machine, opcode, operands, storeVar, null, 5);
    }
    
    @Override
    protected void nextInstruction() {
      nextInstructionCalled = true;
    }
    
    @Override
    protected void returnFromRoutine(char retval) {
      returned = true;
      returnValue = retval;
    }
    
    @Override
    protected void branchOnTest(boolean flag) {
      branchOnTestCalled = true;
      branchOnTestCondition = flag;
    }
  }
}
