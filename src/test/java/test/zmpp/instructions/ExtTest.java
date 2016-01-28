/*
 * Created on 12/08/2005
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package test.zmpp.instructions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;
import static org.zmpp.vm.Instruction.EXT_ART_SHIFT;
import static org.zmpp.vm.Instruction.EXT_DRAW_PICTURE;
import static org.zmpp.vm.Instruction.EXT_ERASE_PICTURE;
import static org.zmpp.vm.Instruction.EXT_LOG_SHIFT;
import static org.zmpp.vm.Instruction.EXT_MOUSE_WINDOW;
import static org.zmpp.vm.Instruction.EXT_MOVE_WINDOW;
import static org.zmpp.vm.Instruction.EXT_PICTURE_DATA;
import static org.zmpp.vm.Instruction.EXT_PICTURE_TABLE;
import static org.zmpp.vm.Instruction.EXT_POP_STACK;
import static org.zmpp.vm.Instruction.EXT_PUSH_STACK;
import static org.zmpp.vm.Instruction.EXT_RESTORE_UNDO;
import static org.zmpp.vm.Instruction.EXT_SAVE_UNDO;
import static org.zmpp.vm.Instruction.EXT_SCROLL_WINDOW;
import static org.zmpp.vm.Instruction.EXT_SET_FONT;
import static org.zmpp.vm.Instruction.EXT_SET_MARGINS;
import static org.zmpp.vm.Instruction.EXT_WINDOW_SIZE;
import static org.zmpp.vm.Instruction.EXT_WINDOW_STYLE;
import static org.zmpp.vm.Instruction.OperandCount.EXT;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zmpp.blorb.BlorbImage;
import org.zmpp.blorb.NativeImage;
import org.zmpp.instructions.ExtInstruction;
import org.zmpp.instructions.InstructionInfoDb;
import org.zmpp.instructions.Operand;
import org.zmpp.media.PictureManager;
import org.zmpp.media.Resolution;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;
import org.zmpp.windowing.ScreenModel6;
import org.zmpp.windowing.Window6;

/**
 * This class tests the dynamic and static aspects of the extended instructions.
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
		context.checking(new Expectations() {
			{
				one(machine).getPC();
				will(returnValue(1234));
				one(machine).save_undo(with(any(char.class)));
				will(returnValue(true));
				atLeast(1).of(machine).setVariable((char) 0, (char) 1);
			}
		});
		ExtMock save_undo = createExtMock(EXT_SAVE_UNDO, new Operand[0]);
		save_undo.execute();
		assertTrue(save_undo.nextInstructionCalled);
	}

	@Test
	public void testSaveUndoFail() {
		context.checking(new Expectations() {
			{
				one(machine).getPC();
				will(returnValue(1234));
				one(machine).save_undo(with(any(char.class)));
				will(returnValue(false));
				atLeast(1).of(machine).setVariable((char) 0, (char) 0);
			}
		});
		ExtMock save_undo = createExtMock(EXT_SAVE_UNDO, new Operand[0]);
		save_undo.execute();
		assertTrue(save_undo.nextInstructionCalled);
	}

	@Test
	public void testRestoreUndoSuccess() {
		context.checking(new Expectations() {
			{
				PortableGameState state = new PortableGameState();
				one(machine).readUnsigned8(0);
				will(returnValue((char) 0));
				one(machine).restore_undo();
				will(returnValue(state));
				one(machine).setVariable((char) 0, (char) ExtInstruction.RESTORE_TRUE);
			}
		});
		ExtMock restore_undo = createExtMock(EXT_RESTORE_UNDO, new Operand[0]);
		restore_undo.execute();
		assertFalse(restore_undo.nextInstructionCalled);
	}

	@Test
	public void testRestoreUndoFail() {
		context.checking(new Expectations() {
			{
				one(machine).restore_undo();
				will(returnValue(null));
				one(machine).setVariable((char) 0, (char) ExtInstruction.FALSE);
			}
		});
		ExtMock restore_undo = createExtMock(EXT_RESTORE_UNDO, new Operand[0]);
		restore_undo.execute();
		assertTrue(restore_undo.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** ART_SHIFT
	// **********************************

	@Test
	public void testArtShift0() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).setVariable((char) 1, (char) 12);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0);
		char storevar = 0x01;
		ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] { op1, op2 }, storevar);
		art_shift.execute();
		assertTrue(art_shift.nextInstructionCalled);
	}

	@Test
	public void testArtShiftPositivePositiveShift() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).setVariable((char) 1, (char) 24);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		char storevar = 0x01;
		ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] { op1, op2 }, storevar);
		art_shift.execute();
		assertTrue(art_shift.nextInstructionCalled);
	}

	@Test
	public void testArtShiftNegativePositiveShift() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).setVariable((char) 1, signedToUnsigned16((short) -24));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -12));
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		char storevar = 0x01;
		ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] { op1, op2 }, storevar);
		art_shift.execute();
		assertTrue(art_shift.nextInstructionCalled);
	}

	@Test
	public void testArtShiftPositiveNegativeShift() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).setVariable((char) 1, (char) 6);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -1));
		char storevar = 0x01;
		ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] { op1, op2 }, storevar);
		art_shift.execute();
		assertTrue(art_shift.nextInstructionCalled);
	}

	@Test
	public void testArtShiftNegativeNegativeShift() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).setVariable((char) 1, signedToUnsigned16((short) -6));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -12));
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -1));
		char storevar = 0x01;
		ExtMock art_shift = createExtMock(EXT_ART_SHIFT, new Operand[] { op1, op2 }, storevar);
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
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).setMouseWindow(3);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock mouse_window = createExtMock(EXT_MOUSE_WINDOW, new Operand[] { op1 });
		mouse_window.execute();
		assertTrue(mouse_window.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** PICTURE_DATA
	// **********************************

	@Test
	public void testPictureDataGetPictureFileInfo() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getPictureManager();
				will(returnValue(picturemanager));
				one(picturemanager).getRelease();
				will(returnValue(2));
				atLeast(1).of(picturemanager).getNumPictures();
				will(returnValue(32));
				one(machine).writeUnsigned16(1000, (char) 32);
				one(machine).writeUnsigned16(1002, (char) 2);
			}
		});
		// the 0 value indicates that we want to retrieve the information about
		// the picture file
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0);

		// The target array
		Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000);
		ExtMock picture_data = createExtMock(EXT_PICTURE_DATA, new Operand[] { op1, op2 });
		picture_data.execute();
		assertTrue(picture_data.branchOnTestCalled);
	}

	@Test
	public void testPictureDataGetPictureInfo() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getPictureManager();
				will(returnValue(picturemanager));
				one(picturemanager).getPictureSize(1);
				will(returnValue(new Resolution(320, 200)));
				one(machine).writeUnsigned16(1000, (char) 200);
				one(machine).writeUnsigned16(1002, (char) 320);
			}
		});
		// the 1 value indicates that we want to retrieve the information about
		// the picture number 1
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);

		// The target array
		Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000);

		ExtMock picture_data = createExtMock(EXT_PICTURE_DATA, new Operand[] { op1, op2 });
		picture_data.execute();
		assertTrue(picture_data.branchOnTestCalled);
		assertTrue(picture_data.branchOnTestCondition);
	}

	@Test
	public void testPictureDataGetPictureNonExistent() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getPictureManager();
				will(returnValue(picturemanager));
				one(picturemanager).getPictureSize(55);
				will(returnValue(null));
				// one (machine).incrementPC(3);
			}
		});
		// the 55 value indicates that we want to retrieve the information about
		// the picture number 55, which in this test does not exist
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 55);

		// The target array
		Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1000);
		ExtMock picture_data = createExtMock(EXT_PICTURE_DATA, new Operand[] { op1, op2 });
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
			public int getWidth() {
				return 320;
			}

			public int getHeight() {
				return 200;
			}
		};
		final BlorbImage blorbimage = new BlorbImage(image);
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getPictureManager();
				will(returnValue(picturemanager));
				one(picturemanager).getPicture(1);
				will(returnValue(blorbimage));
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getSelectedWindow();
				will(returnValue(window6));
				one(window6).drawPicture(blorbimage, 2, 3);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock draw_picture = createExtMock(EXT_DRAW_PICTURE, new Operand[] { op1, op2, op3 });
		draw_picture.execute();
		assertTrue(draw_picture.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** ERASE_PICTURE
	// **********************************

	@Test
	public void testErasePicture() {
		NativeImage image = new NativeImage() {
			public int getWidth() {
				return 320;
			}

			public int getHeight() {
				return 200;
			}
		};
		final BlorbImage blorbimage = new BlorbImage(image);
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getPictureManager();
				will(returnValue(picturemanager));
				one(picturemanager).getPicture(1);
				will(returnValue(blorbimage));
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getSelectedWindow();
				will(returnValue(window6));
				one(window6).erasePicture(blorbimage, 2, 3);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock erase_picture = createExtMock(EXT_ERASE_PICTURE, new Operand[] { op1, op2, op3 });
		erase_picture.execute();
		assertTrue(erase_picture.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** MOVE_WINDOW
	// **********************************
	@Test
	public void testMoveWindow() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getWindow(1);
				will(returnValue(window6));
				one(window6).move(2, 3);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock move_window = createExtMock(EXT_MOVE_WINDOW, new Operand[] { op1, op2, op3 });
		move_window.execute();
		assertTrue(move_window.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** WINDOW_SIZE
	// **********************************

	@Test
	public void testWindowSize() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getWindow(1);
				will(returnValue(window6));
				one(window6).setSize(2, 3);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock window_size = createExtMock(EXT_WINDOW_SIZE, new Operand[] { op1, op2, op3 });
		window_size.execute();
		assertTrue(window_size.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** WINDOW_STYLE
	// **********************************

	@Test
	public void testWindowStyle() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getWindow(1);
				will(returnValue(window6));
				one(window6).setStyle(2, 3);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock window_style = createExtMock(EXT_WINDOW_STYLE, new Operand[] { op1, op2, op3 });
		window_style.execute();
		assertTrue(window_style.nextInstructionCalled);
	}

	@Test
	public void testWindowStyle2Params() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getWindow(1);
				will(returnValue(window6));
				one(window6).setStyle(2, 0);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		ExtMock window_style = createExtMock(EXT_WINDOW_STYLE, new Operand[] { op1, op2 });
		window_style.execute();
		assertTrue(window_style.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** SET_MARGINS
	// **********************************

	@Test
	public void testSetMargins() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getWindow(3);
				will(returnValue(window6));
				one(window6).setMargins(1, 2);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		ExtMock set_margins = createExtMock(EXT_SET_MARGINS, new Operand[] { op1, op2, op3 });
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
		context.checking(new Expectations() {
			{
				one(machine).pushStack((char) 0, (char) 11);
				will(returnValue(true));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11);
		ExtMock push_stack = createExtMock(EXT_PUSH_STACK, new Operand[] { op1 });
		push_stack.execute();
		assertTrue(push_stack.branchOnTestCalled);
		assertTrue(push_stack.branchOnTestCondition);
	}

	@Test
	public void testPushStackUserStackOk() {
		context.checking(new Expectations() {
			{
				one(machine).pushStack((char) 1112, (char) 11);
				will(returnValue(true));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11);
		Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1112);
		ExtMock push_stack = createExtMock(EXT_PUSH_STACK, new Operand[] { op1, op2 });
		push_stack.execute();
		assertTrue(push_stack.branchOnTestCalled);
		assertTrue(push_stack.branchOnTestCondition);
	}

	@Test
	public void testPushStackUserStackOverflow() {
		context.checking(new Expectations() {
			{
				one(machine).pushStack((char) 1112, (char) 11);
				will(returnValue(false));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 11);
		Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1112);
		ExtMock push_stack = createExtMock(EXT_PUSH_STACK, new Operand[] { op1, op2 });
		push_stack.execute();
		assertTrue(push_stack.branchOnTestCalled);
		assertFalse(push_stack.branchOnTestCondition);
	}

	// **************************************************************************
	// ******** POP_STACK
	// **********************************

	@Test
	public void testPopStackStdStack() {
		context.checking(new Expectations() {
			{
				exactly(5).of(machine).popStack((char) 0);
				will(returnValue((char) 11));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
		ExtMock pop_stack = createExtMock(EXT_POP_STACK, new Operand[] { op1 });
		pop_stack.execute();
		assertTrue(pop_stack.nextInstructionCalled);
	}

	@Test
	public void testPopStackUserStack() {
		context.checking(new Expectations() {
			{
				exactly(3).of(machine).popStack((char) 1113);
				will(returnValue((char) 11));
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
		Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1113);
		ExtMock pop_stack = createExtMock(EXT_POP_STACK, new Operand[] { op1, op2 });
		pop_stack.execute();
		assertTrue(pop_stack.nextInstructionCalled);
	}

	// **************************************************************************
	// ******** SCROLL_WINDOW
	// **********************************

	@Test
	public void testScrollWindow() {
		context.checking(new Expectations() {
			{
				one(machine).getScreen6();
				will(returnValue(screen6));
				one(screen6).getWindow(2);
				will(returnValue(window6));
				one(window6).scroll(5);
			}
		});
		Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
		Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
		ExtMock scroll_window = createExtMock(EXT_SCROLL_WINDOW, new Operand[] { op1, op2 });
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
