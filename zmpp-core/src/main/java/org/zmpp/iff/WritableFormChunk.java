/*
 * Created on 2005/09/23
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
package org.zmpp.iff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.writeUnsigned32;

/**
 * A writable FormChunk class.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class WritableFormChunk implements FormChunk {

  private byte[] subId;
  private static final String FORM_ID = "FORM";
  private List<Chunk> subChunks;
  
  /**
   * Constructor.
   * @param subId the sub id
   */
  public WritableFormChunk(final byte[] subId) {  
    super();
    this.subId = subId;
    this.subChunks = new ArrayList<Chunk>();
  }
  
  /**
   * Adds a sub chunk.
   * @param chunk the sub chunk to add
   */
  public void addChunk(final Chunk chunk) {
    
    subChunks.add(chunk);
  }
  
  /** {@inheritDoc} */
  public String getSubId() {   
    return new String(subId);
  }

  /** {@inheritDoc} */
  public Iterator<Chunk> getSubChunks() {
    return subChunks.iterator();
  }

  /** {@inheritDoc} */
  public Chunk getSubChunk(final String id) {
    for (Chunk chunk : subChunks) {
      if (chunk.getId().equals(id)) {
        return chunk;
      }
    }
    return null;
  }
  
  /** {@inheritDoc} */
  public Chunk getSubChunk(final int address) {
    // We do not need to implement this
    return null;
  }

  /** {@inheritDoc} */
  public String getId() { return FORM_ID; }

  /** {@inheritDoc} */
  public int getSize() {
    int size = subId.length;
    
    for (Chunk chunk : subChunks) {
      int chunkSize = chunk.getSize();
      if ((chunkSize % 2) != 0) {
        chunkSize++; // pad if necessary
      }
      size += (Chunk.CHUNK_HEADER_LENGTH + chunkSize);
    }
    return size;
  }

  /** {@inheritDoc} */
  public boolean isValid() { return true; }

  /** {@inheritDoc} */
  public Memory getMemory() {
    return new DefaultMemory(getBytes());
  }

  /**
   * Returns the data of this chunk.
   * @return the chunk data
   */
  public byte[] getBytes() {
    final int datasize = Chunk.CHUNK_HEADER_LENGTH + getSize();    
    final byte[] data = new byte[datasize];
    final Memory memory = new DefaultMemory(data);
    memory.writeUnsigned8(0, 'F');
    memory.writeUnsigned8(1, 'O');
    memory.writeUnsigned8(2, 'R');
    memory.writeUnsigned8(3, 'M');
    writeUnsigned32(memory, 4, getSize());
    
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    
    // Write sub id
    memory.copyBytesFromArray(subId, 0, offset, subId.length);
    offset += subId.length;
    
    // Write sub chunk data
    for (Chunk chunk : subChunks) {
      final byte[] chunkId = chunk.getId().getBytes();
      final int chunkSize = chunk.getSize();
      
      // Write id
      memory.copyBytesFromArray(chunkId, 0, offset, chunkId.length);
      offset += chunkId.length;
      
      // Write chunk size
      writeUnsigned32(memory, offset, chunkSize);
      offset += 4; // add the size word length
      
      // Write chunk data
      final Memory chunkMem = chunk.getMemory();
      memory.copyBytesFromMemory(chunkMem, Chunk.CHUNK_HEADER_LENGTH, offset,
                                 chunkSize);
      offset += chunkSize;      
      // Pad if necessary
      if ((chunkSize % 2) != 0) {
        memory.writeUnsigned8(offset++, (char) 0);
      }
    }
    return data;
  }
  
  /** {@inheritDoc} */
  public int getAddress() { return 0; }  
}
