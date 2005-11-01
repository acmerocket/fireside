/*
 * $Id$
 * 
 * Created on 03.10.2005
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
package org.zmpp.vm;

import org.zmpp.base.MemoryAccess;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;

/**
 * This class represents the state of the Z machine in an external format,
 * so it can be exchanged using the Quetzal IFF format.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class PortableGameState {

  private int release;
  private int checksum;
  private byte[] serialBytes;
  private int pc;
  
  /**
   * Constructor.
   * 
   * @param machine a machine reference
   */
  public PortableGameState() {
    
    serialBytes = new byte[6];
  }
  
  public int getRelease() { return release; }
  public int getChecksum() { return checksum; }
  public String getSerialNumber() { return new String(serialBytes); }
  public int getProgramCounter() { return pc; }
  
  public boolean readSaveGame(FormChunk formChunk) {
    
    if ((new String(formChunk.getSubId())).equals("IFZS")) {
      
      readIfhdChunk(formChunk);
      readStacksChunk(formChunk);
      readMemoryChunk(formChunk);
      
      return true;
    }
    return false;
  }
  
  private void readIfhdChunk(FormChunk formChunk) {
    
    Chunk ifhdChunk = formChunk.getSubChunk("IFhd".getBytes());
    MemoryAccess chunkMem = ifhdChunk.getMemoryAccess();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    
    // read release number
    release = chunkMem.readUnsignedShort(offset);
    offset += 2;
    
    // read serial number
    for (int i = 0; i < 6; i++) {
      
      serialBytes[i] = chunkMem.readByte(offset + i);
    }
    offset += 6;
    
    // read check sum
    checksum = chunkMem.readUnsignedShort(offset);
    offset += 2;

    // read pc
    pc = ((chunkMem.readByte(offset) & 0xff) << 16)
          | ((chunkMem.readByte(offset + 1) & 0xff) << 8)
          | (chunkMem.readByte(offset + 2) & 0xff);
  }
  
  private void readStacksChunk(FormChunk formChunk) {
    
    Chunk stksChunk = formChunk.getSubChunk("Stks".getBytes());    
  }
  
  private void readMemoryChunk(FormChunk formChunk) {
    
    Chunk cmemChunk = formChunk.getSubChunk("CMem".getBytes());
    Chunk umemChunk = formChunk.getSubChunk("UMem".getBytes());
    
  }
}
