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

import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.readUnsigned32;
import static org.zmpp.base.MemoryUtil.writeUnsigned32;

/**
 * This is the default implementation of the Chunk interface.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultChunk implements Chunk {

  /** The memory access object. */
  protected Memory memory;

  /** The chunk id. */
  private byte[] id;

  /** The chunk size. */
  private int chunkSize;

  /** The start address within the form chunk. */
  private int address;

  /**
   * Constructor. Used for reading files.
   * @param memory a Memory object to the chunk data
   * @param address the address within the form chunk
   */
  public DefaultChunk(final Memory memory, final int address) {
    super();
    this.memory = memory;
    this.address = address;
    id = new byte[CHUNK_ID_LENGTH];
    memory.copyBytesToArray(id, 0, 0, CHUNK_ID_LENGTH);
    chunkSize = (int) readUnsigned32(memory, CHUNK_ID_LENGTH);
  }

  /**
   * Constructor. Initialize from byte data. This constructor is used
   * when writing a file, in that case chunks really are separate
   * memory areas.
   * @param id the id
   * @param chunkdata the data without header information, number of bytes
   * needs to be even
   */
  public DefaultChunk(final byte[] id, final byte[] chunkdata) {
    this.id = id;
    this.chunkSize = chunkdata.length;
    final byte[] chunkDataWithHeader =
      new byte[chunkSize + Chunk.CHUNK_HEADER_LENGTH];
    this.memory = new DefaultMemory(chunkDataWithHeader);
    memory.copyBytesFromArray(id, 0, 0, id.length);
    writeUnsigned32(memory, id.length, chunkSize);
    memory.copyBytesFromArray(chunkdata, 0, id.length + 4,
                              chunkdata.length);
  }

  /** {@inheritDoc} */
  public boolean isValid() { return true; }

  /** {@inheritDoc} */
  public String getId() { return new String(id); }

  /** {@inheritDoc} */
  public int getSize() { return chunkSize; }

  /** {@inheritDoc} */
  public Memory getMemory() { return memory; }

  /** {@inheritDoc} */
  public int getAddress() { return address; }
}
