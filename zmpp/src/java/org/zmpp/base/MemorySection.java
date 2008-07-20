/*
 * $Id$
 * 
 * Created on 2005/09/23
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
package org.zmpp.base;

/**
 * A MemorySection object wraps a Memory object, a length and a start to
 * support subsections within memory.
 * All access functions will be relative to the initialized start offset
 * within the global memory.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MemorySection implements Memory {

  private Memory memory;
  private int start;
  private int length;
  
  /**
   * Constructor.
   * @param memory the Memory objeci to wrap
   * @param start the start of the section
   * @param length the length of the section
   */
  public MemorySection(final Memory memory, final int start, final int length) {
    super();
    this.memory = memory;
    this.start = start;
    this.length = length;
  }
  
  /**
   * Returns the length of this object in bytes.
   * 
   * @return the length in bytes
   */
  public int getLength() { return length; }
    
  /**
   * {@inheritDoc}
   */
  public void writeUnsigned16(final int address, final char value) {
    memory.writeUnsigned16(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeSigned16(final int address, final short value) {
    memory.writeSigned16(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeUnsigned8(final int address, final short value) {
    memory.writeUnsigned8(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeSigned8(final int address, final byte value) {
    memory.writeSigned8(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public char readUnsigned16(final int address) {
    return memory.readUnsigned16(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public short readSigned16(final int address) {
    return memory.readSigned16(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public short readUnsigned8(final int address) {
    return memory.readUnsigned8(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public byte readSigned8(final int address) {
    assert address < length : "Accessing an address beyond section length: address: " + address + ", length: " + length + ", start: "  + start;
    return memory.readSigned8(address + start);
  }
}
