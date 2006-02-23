/**
 * $Id$
 */
package test.zmpp.instructions;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.jmock.Mock;
import org.zmpp.instructions.ExtendedInstruction;
import org.zmpp.instructions.ExtendedStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.media.PictureManager;
import org.zmpp.vm.ScreenModel6;
import org.zmpp.vm.Window6;

public class ExtendedInstructionV6Test extends InstructionTestBase {

  private Mock mockscreen6, mockwindow6, mockPictureManager;
  
  private ScreenModel6 screen6;
  private Window6 window6;
  private PictureManager picturemanager;
  
  protected void setUp() throws Exception {
    
    super.setUp();
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(6));
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
    mockPictureManager.expects(once()).method("getNumPictures").will(returnValue(32));
    mockGameData.expects(once()).method("getMemoryAccess").will(returnValue(memoryAccess));
    mockMemAccess.expects(once()).method("writeUnsignedShort").with(eq(1000), eq(32));
    mockMemAccess.expects(once()).method("writeUnsignedShort").with(eq(1002), eq(2));

    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    
    ExtendedInstruction picture_data = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_PICTURE_DATA);
    picture_data.setLength(3);
    
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
    mockGameData.expects(once()).method("getMemoryAccess").will(returnValue(memoryAccess));
    mockMemAccess.expects(once()).method("writeUnsignedShort").with(eq(1000), eq(200));
    mockMemAccess.expects(once()).method("writeUnsignedShort").with(eq(1002), eq(320));
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
    
    mockMachine.expects(atLeastOnce()).method("getPictureManager").will(returnValue(picturemanager));
    mockPictureManager.expects(once()).method("getPicture").with(eq(1)).will(returnValue(image));
    mockMachine.expects(atLeastOnce()).method("getScreen6").will(returnValue(screen6));
    mockscreen6.expects(once()).method("getCurrentWindow").will(returnValue(window6));
    mockwindow6.expects(once()).method("drawPicture").with(eq(image), eq(2), eq(3));
    
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
}
