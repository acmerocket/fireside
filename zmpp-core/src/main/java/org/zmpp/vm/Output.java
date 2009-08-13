/*
 * Created on 2006/02/14
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
package org.zmpp.vm;

/**
 * The Output interface.
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface Output {

  /** The output stream number for the screen. */
  int OUTPUTSTREAM_SCREEN = 1;

  /** The output stream number for the transcript. */
  int OUTPUTSTREAM_TRANSCRIPT = 2;

  /** The output stream number for the memory stream. */
  int OUTPUTSTREAM_MEMORY = 3;

  /**
   * Selects/unselects the specified output stream. If the streamnumber
   * is negative, |streamnumber| is deselected, if positive, it is selected.
   * Stream 3 (the memory stream) can not be selected by this function,
   * but can be deselected here.
   * @param streamnumber the output stream number
   * @param flag true to enable, false to disable
   */
  void selectOutputStream(int streamnumber, boolean flag);

  /**
   * Selects the output stream 3 which writes to memory.
   * @param tableAddress the table address to write to
   * @param tableWidth the table width
   */
  void selectOutputStream3(int tableAddress, int tableWidth);

  /**
   * Prints the ZSCII string at the specified address to the active
   * output streams.
   * @param stringAddress the address of an ZSCII string
   */
  void printZString(int stringAddress);

  /**
   * Prints the specified string to the active output streams.
   * @param str the string to print, encoding is ZSCII
   */
  void print(String str);

  /**
   * Prints a newline to the active output streams.
   */
  void newline();

  /**
   * Prints the specified ZSCII character.
   * @param zchar the ZSCII character to print
   */
  void printZsciiChar(char zchar);

  /**
   * Prints the specified signed number.
   * @param num the number to print?
   */
  void printNumber(short num);

  /**
   * Flushes the active output streams.
   */
  void flushOutput();

  /**
   * Resets the output streams.
   */
  void reset();
}
