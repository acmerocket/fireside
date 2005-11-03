/*
 * $Id$
 * 
 * Created on 24.09.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package test.zmpp.vm;

import java.io.File;
import java.io.RandomAccessFile;

import org.jmock.MockObjectTestCase;
import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.base.MemoryAccess;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.PortableGameState.StackFrame;

/**
 * This tests simply analyzes a given Quetzal file.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class PortableGameStateTest extends MockObjectTestCase {

  private PortableGameState gameState;
  private FormChunk formChunk;
  
  protected void setUp() throws Exception {
  
    File testSaveFile = new File("testfiles/leathersave.ifzs");
    RandomAccessFile saveFile = new RandomAccessFile(testSaveFile, "r");
    int length = (int) saveFile.length();
    byte[] data = new byte[length];
    saveFile.readFully(data);
    MemoryAccess memaccess = new DefaultMemoryAccess(data);
    formChunk = new DefaultFormChunk(memaccess);
    gameState = new PortableGameState();
    
    saveFile.close();
  }
  
  int[] pcs = { 0, 25108, 25132, 25377, 26137, 26457, 26499 };
  int[] retvars = { 0, 0, 1, 7, 0, 4, 0 };
  int[] localLengths = { 0, 1, 11, 2, 7, 4, 0 };
  int[] stackSizes = { 4, 0, 0, 0, 2, 0, 0 };
  int[] numArgs = { 0, 1, 4, 2, 4, 4, 0 };
  
  public void testReadSaveGame() {
    
    assertTrue(gameState.readSaveGame(formChunk));
    assertEquals(59, gameState.getRelease());
    assertEquals("860730", gameState.getSerialNumber());
    assertEquals(53360, gameState.getChecksum());
    assertEquals(35298, gameState.getProgramCounter());
    
    assertEquals(7, gameState.getStackFrames().size());
  
    for (int i = 0; i < gameState.getStackFrames().size(); i++) {
    
      StackFrame sfi = gameState.getStackFrames().get(i);
      assertEquals(pcs[i], sfi.getProgramCounter());
      assertEquals(retvars[i], sfi.getReturnVariable());
      assertEquals(localLengths[i], sfi.getLocals().length);
      assertEquals(stackSizes[i], sfi.getEvalStack().length);
      assertEquals(numArgs[i], sfi.getArgs().length);
    }
    assertEquals(10030, gameState.getDeltaBytes().length);
  }
}
