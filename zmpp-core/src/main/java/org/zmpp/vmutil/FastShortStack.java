/*
 * Created on 2006/05/10
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
package org.zmpp.vmutil;

/**
 * This class implements a faster version of the Z-machin main stack.
 * This combines abstract access with the bypassing of unnecessary
 * object creation.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public final class FastShortStack {

  private char[] values;
  private char stackpointer;

  /**
   * Constructor.
   * @param size the stack size
   */
  public FastShortStack(final int size) {
    values = new char[size];
    stackpointer = 0;
  }

  /**
   * Returns the current stack pointer.
   * @return the stack pointer
   */
  public char getStackPointer() { return stackpointer; }

  /**
   * Pushes a value on the stack and increases the stack pointer.
   * @param value the value
   */
  public void push(final char value) {
    values[stackpointer++] = value;
  }

  /**
   * Returns the top value of the stack without modifying the stack pointer.
   * @return the top value
   */
  public char top() { return values[stackpointer - 1]; }

  /**
   * Replaces the top element with the specified value.
   * @param value the value to replace
   */
  public void replaceTopElement(final char value) {
    values[stackpointer - 1] = value;
  }

  /**
   * Returns the size of the stack. Is equal to stack pointer, but has a
   * different semantic meaning.
   * @return the size of the stack
   */
  public int size() { return stackpointer; }

  /**
   * Returns the top value of the stack and decreases the stack pointer.
   * @return the top value
   */
  public char pop() {
    return values[--stackpointer];
  }

  /**
   * Returns the value at index of the stack, here stack is treated as an array.
   * @param index the index
   * @return the value at the index
   */
  public char getValueAt(int index) { return values[index]; }
}
