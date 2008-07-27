/*
 * $Id$
 * 
 * Created on 2008/07/19
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
 * Utility functions for address conversion.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MemoryUtil {
  /**
   * Convert an integer value to a char, which is an unsigned 16 bit value.
   * @param value the value to convert
   * @return the converted value
   */
  public static char toUnsigned16(int value) {
    return (char) (value & 0xffff);
  }

  /**
   * Reads the unsigned 32 bit word at the specified address.
   * @param memory the Memory object
   * @param address the address
   * @return the 32 bit unsigned value as long
   */
  public static long readUnsigned32(Memory memory, int address) {
      final long a24 = (memory.readUnsigned8(address) & 0xffL) << 24;
      final long a16 = (memory.readUnsigned8(address + 1) & 0xffL) << 16;
      final long a8  = (memory.readUnsigned8(address + 2) & 0xffL) << 8;
      final long a0  = (memory.readUnsigned8(address + 3) & 0xffL);
      return a24 | a16 | a8 | a0;
  }

  /**
   * Writes an unsigned 32 bit value to the specified address.
   * @param memory the Memory object
   * @param address the address to write to
   * @param value the value to write
   */
  public static void writeUnsigned32(Memory memory, final int address,
                                     final long value) {
    memory.writeUnsigned8(address, (char) ((value & 0xff000000) >> 24));
    memory.writeUnsigned8(address + 1, (char) ((value & 0x00ff0000) >> 16));
    memory.writeUnsigned8(address + 2, (char) ((value & 0x0000ff00) >> 8));
    memory.writeUnsigned8(address + 3, (char) (value & 0x000000ff));
  }
  
  /**
   * Converts the specified signed 16 bit value to an unsigned 16 bit value.
   * @param value the signed value
   * @return the unsigned value
   */
  public static char signedToUnsigned16(short value) {
    return (char) (value >= 0 ? value : Character.MAX_VALUE + (value + 1));
  }
  
  /**
   * Converts the specified unsigned 16 bit value to a signed 16 bit value.
   * @param value the unsigned value
   * @return the signed value
   */
  public static short unsignedToSigned16(char value) {
    return (short) (value > Short.MAX_VALUE ?
      -(Character.MAX_VALUE - (value - 1)) : value);
  }

  /**
   * Converts the specified unsigned 8 bit value to a signed 8 bit value.
   * If the value specified is actually a 16 bit value, only the lower 8 bit
   * will be used.
   * @param value the unsigned value
   * @return the signed value
   */
  public static short unsignedToSigned8(char value) {
    char workvalue = (char) (value & 0xff);
    return (short) (workvalue > Byte.MAX_VALUE ?
      -(255 - (workvalue - 1)) : workvalue);
  }
}
