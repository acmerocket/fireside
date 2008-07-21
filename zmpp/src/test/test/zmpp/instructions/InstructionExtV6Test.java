/*
 * $Id$
 *
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
import org.junit.runner.RunWith;
import org.zmpp.blorb.BlorbImage;
import org.zmpp.blorb.BlorbImage.Resolution;
import org.zmpp.blorb.NativeImage;
import org.zmpp.instructions.ExtendedInstruction;
import org.zmpp.instructions.ExtendedStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.media.PictureManager;
import org.zmpp.vm.ScreenModel6;
import org.zmpp.vm.Window6;

/**
 * Test class for EXT instructions on V6.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class InstructionExtV6Test extends InstructionTestBase {

  private ScreenModel6 screen6;
  private Window6 window6;
  private PictureManager picturemanager;
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(6);
    screen6 = context.mock(ScreenModel6.class);
    picturemanager = context.mock(PictureManager.class);
    window6 = context.mock(Window6.class);
  }

  // **************************************************************************
  // ******** MOUSE_WINDOW
  // **********************************
  
  @Test
  public void testMouseWindow() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).setMouseWindow(3);
      atLeast(1).of (machine).incrementPC(3);
    }});
    ExtendedInstruction mouse_window = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_MOUSE_WINDOW);
    mouse_window.setLength(3);
    mouse_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));
    mouse_window.execute();
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
      one (machine).doBranch((short) 123, 3);
    }});
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    picture_data.setBranchOffset((short) 123);
    
    // the 0 value indicates that we want to retrieve the information about
    // the picture file
    picture_data.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0));
    
    // The target array
    picture_data.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000));
    picture_data.execute();
  }

  @Test
  public void testPictureDataGetPictureInfo() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getPictureSize(1); will(returnValue(new Resolution(320, 200)));
      one (machine).writeUnsigned16(1000, (char) 200);
      one (machine).writeUnsigned16(1002, (char) 320);
      one (machine).doBranch((short) 42, 3);
    }});
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    
    // the 1 value indicates that we want to retrieve the information about
    // the picture number 1
    picture_data.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    
    // The target array
    picture_data.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000));
    
    picture_data.setBranchIfTrue(true);
    picture_data.setBranchOffset((short) 42);
    
    picture_data.execute();
  }

  @Test
  public void testPictureDataGetPictureNonExistent() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getPictureManager(); will(returnValue(picturemanager));
      one (picturemanager).getPictureSize(55); will(returnValue(null));
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    
    // the 55 value indicates that we want to retrieve the information about
    // the picture number 55, which in this test does not exist
    picture_data.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 55));
    
    // The target array
    picture_data.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000));    
    picture_data.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction draw_picture = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_DRAW_PICTURE);
    draw_picture.setLength(3);
    draw_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    draw_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    draw_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));    
    draw_picture.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction erase_picture = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ERASE_PICTURE);
    erase_picture.setLength(3);
    erase_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    erase_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    erase_picture.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));    
    erase_picture.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction move_window = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_MOVE_WINDOW);
    move_window.setLength(3);
    
    move_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    move_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    move_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));
    
    move_window.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction window_size = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_WINDOW_SIZE);
    window_size.setLength(3);   
    window_size.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    window_size.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    window_size.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));
    window_size.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction window_style = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_WINDOW_STYLE);
    window_style.setLength(3);    
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));
    window_style.execute();
  }

  @Test
  public void testWindowStyle2Params() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getScreen6(); will(returnValue(screen6));
      one (screen6).getWindow(1); will(returnValue(window6));
      one (window6).setStyle(2, 0);
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction window_style = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_WINDOW_STYLE);
    window_style.setLength(3);    
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    window_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    window_style.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction set_margins = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SET_MARGINS);
    set_margins.setLength(3);
    set_margins.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    set_margins.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    set_margins.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));    
    set_margins.execute();
  }

  // **************************************************************************
  // ******** PICTURE_TABLE
  // **********************************
  
  @Test
  public void testPictureTable() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction picture_table = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_TABLE);
    picture_table.setLength(3);
    picture_table.execute();
  }

  // **************************************************************************
  // ******** PUSH_STACK
  // **********************************
  @Test
  public void testPushStackStdStack() {
    context.checking(new Expectations() {{
      one (machine).pushStack(0, (char) 11); will(returnValue(true));
      one (machine).doBranch((short) 13, 3);
    }});
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PUSH_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11));
    push_stack.setLength(3);
    push_stack.setBranchOffset((short) 13);
    push_stack.execute();
  }
  
  @Test
  public void testPushStackUserStackOk() {
    context.checking(new Expectations() {{
      one (machine).pushStack(1112, (char) 11); will(returnValue(true));
      one (machine).doBranch((short) 13, 3);
    }});
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PUSH_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11));
    push_stack.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1112));
    push_stack.setLength(3);
    push_stack.setBranchOffset((short) 13);
    push_stack.execute();
  }

  @Test
  public void testPushStackUserStackOverflow() {
    context.checking(new Expectations() {{
      one (machine).pushStack(1112, (char) 11); will(returnValue(false));
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PUSH_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11));
    push_stack.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1112));
    push_stack.setLength(3);
    push_stack.setBranchOffset((short) 13);
    push_stack.execute();
  }
  
  // **************************************************************************
  // ******** POP_STACK
  // **********************************
  
  @Test
  public void testPopStackStdStack() {
    context.checking(new Expectations() {{
      exactly(5).of (machine).popStack(0); will(returnValue((char) 11));
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_POP_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5));
    push_stack.setLength(3);
    push_stack.execute();
  }
  
  @Test
  public void testPopStackUserStack() {
    context.checking(new Expectations() {{
      exactly(3).of (machine).popStack(1113); will(returnValue((char) 11));
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction push_stack = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_POP_STACK);
    push_stack.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3));
    push_stack.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1113));
    push_stack.setLength(3);
    push_stack.execute();
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
      one (machine).incrementPC(3);
    }});
    ExtendedInstruction scroll_window = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SCROLL_WINDOW);
    scroll_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    scroll_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5));
    scroll_window.setLength(3);
    scroll_window.execute();
  }
}
