/*
 * Created on 2006/01/10
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
package org.zmpp.encoding;

import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.toUnsigned16;
import org.zmpp.encoding.AlphabetTable.Alphabet;

/**
 * This class encodes ZSCII strings into dictionary encoded strings.
 * Since encoding is only needed from version 5, we can always assume
 * a target entry size of 6 bytes containing a maximum of nine characters.
 * Encoding is pretty difficult since there are several variables to
 * remember during the encoding process which would result in ugly code if
 * stored in member variables. We use the strategy of having an encoding
 * state for a target word which is changed and passed around until the
 * word can written out.
 *
 * The encoding has some restrictions defined in the specification:
 * The target string is restricted to 6 bytes and 9 characters, which is
 * the length of dictionary entries and no abbreviations need to be taken
 * into consideration.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZCharEncoder {

  private static final int MAX_ENTRY_LENGTH = 9;
  private static final int NUM_TARGET_BYTES = 6;
  private ZCharTranslator translator;

  /**
   * Constructor.
   * @param translator ZCharTranslator object
   */
  public ZCharEncoder(final ZCharTranslator aTranslator) {
    this.translator = aTranslator;
  }
  public void encode(final Memory memory,
      final int sourceAddress, final int length, final int targetAddress) {
    final int maxlen = Math.min(length, MAX_ENTRY_LENGTH);
    final EncodingState state = new EncodingState();
    state.init(memory, sourceAddress, targetAddress, maxlen);
    encode(state, translator);
  }

  /**
   * Encodes the string at the specified address and writes it to the target
   * address.
   * @param state EncodingState
   * @param translator ZCharTranslator
   */
  private static void encode(EncodingState state, ZCharTranslator translator) {
    while (state.hasMoreInput()) {
      processChar(translator, state);
    }
    // Padding
    // This pads the incomplete last encoded word
    if (state.wordPosition <= 2 && state.target <= (state.targetStart + 4)) {
      int resultword = state.currentWord;
      for (int i = state.wordPosition; i < 3; i++) {
        resultword = writeByteToWord(resultword, (char) 5, i);
      }
      state.memory.writeUnsigned16(state.target, toUnsigned16(resultword));
      state.target += 2;
    }

    // If we did not encode 3 shorts, fill the rest with 0x14a5's
    final int targetOffset = state.getTargetOffset();
    for (int i = targetOffset; i < NUM_TARGET_BYTES; i+= 2) {
      state.memory.writeUnsigned16(state.targetStart + i, toUnsigned16(0x14a5));
    }

    // Always mark the last word as such, the last word is always
    // starting at the fifth byte
    state.markLastWord();
  }

  /**
   * Processes the current character.
   * @param translator ZCharTranslator object
   * @param state the EncodingState
   */
  private static void processChar(ZCharTranslator translator,
                                  final EncodingState state) {
    final char zsciiChar = state.nextChar();
    final AlphabetElement element = translator.getAlphabetElementFor(zsciiChar);
    if (element.getAlphabet() == null) {
      final char zcharCode = element.getZCharCode();
      // This is a ZMPP specialty, we do not want to end the string
      // in the middle of encoding, so we only encode if there is
      // enough space
      // how many slots left ?
      final int numRemainingSlots = getNumRemainingSlots(state);
      if (numRemainingSlots >= 4) {
        // Escape A2
        processWord(state, (char) 5);
        processWord(state, (char) 6);
        processWord(state, getUpper5Bit(zcharCode));
        processWord(state, getLower5Bit(zcharCode));
      } else {
        for (int i = 0; i < numRemainingSlots; i++) {
          processWord(state, (char) 5);
        }
      }
    } else {
      final Alphabet alphabet = element.getAlphabet();
      final char zcharCode = element.getZCharCode();
      if (alphabet == Alphabet.A1) {
        processWord(state, (char) 4);
      } else if (alphabet == Alphabet.A2) {
        processWord(state, (char) 5);
      }
      processWord(state, zcharCode);
    }
  }

  /**
   * Returns the number of remaining slots.
   * @param state the EncodingState
   * @return number of remaining slots
   */
  private static int getNumRemainingSlots(final EncodingState state) {
    final int currentWord = state.getTargetOffset() / 2;
    return ((2 - currentWord) * 3) + (3 - state.wordPosition);
  }

  /**
   * Processes the current word.
   * @param state the EncodingState
   * @param value the char value
   */
  private static void processWord(final EncodingState state, final char value) {
    state.currentWord = writeByteToWord(state.currentWord, value,
                                        state.wordPosition++);
    writeWordIfNeeded(state);
  }

  /**
   * Writes the current word if needed.
   * @param state the EncodingState
   */
  private static void writeWordIfNeeded(final EncodingState state) {
    if (state.wordPosition > 2 && state.target <= (state.targetStart + 4)) {
      // Write the result and increment the target position
      state.memory.writeUnsigned16(state.target,
                                   toUnsigned16(state.currentWord));
      state.target += 2;
      state.currentWord = 0;
      state.wordPosition = 0;
    }
  }

  /**
   * Retrieves the upper 5 bit of the specified ZSCII character.
   * @param zsciiChar the ZSCII character
   * @return the upper 5 bit
   */
  private static char getUpper5Bit(final char zsciiChar) {
    return (char) ((zsciiChar >>> 5) & 0x1f);
  }

  /**
   * Retrieves the lower 5 bit of the specified ZSCII character.
   * @param zsciiChar the ZSCII character
   * @return the lower 5 bit
   */
  private static char getLower5Bit(final char zsciiChar) {
    return (char) (zsciiChar & 0x1f);
  }

  /**
   * This function sets a byte value to the specified position within
   * a word. There are three positions within a 16 bit word and the bytes
   * are truncated such that only the lower 5 bit are taken as values.
   *
   * @param dataword the word to set
   * @param databyte the byte to set
   * @param pos a value between 0 and 2
   * @return the new word with the databyte set in the position
   */
  private static char writeByteToWord(final int dataword,
      final char databyte, final int pos) {
    final int shiftwidth = (2 - pos) * 5;
    return (char) (dataword | ((databyte & 0x1f) << shiftwidth));
  }
}

/**
 * EncodingState class.
 */
class EncodingState {
  private static final int TARGET_LAST_WORD = 4;
  public Memory memory;
  private int source;
  private int sourceStart;
  public int target;
  public int targetStart;
  public int currentWord;
  public int wordPosition;
  public int maxLength;
  public void init(Memory mem, int src, int trgt, int maxlen) {
    memory = mem;
    source = src;
    sourceStart = src;
    target = trgt;
    targetStart = trgt;
    maxLength = maxlen;
  }
  public int getTargetOffset() { return target - targetStart; }
  public char nextChar() { return memory.readUnsigned8(source++); }
  public void markLastWord() {
    final int lastword =
      memory.readUnsigned16(targetStart + TARGET_LAST_WORD);
    memory.writeUnsigned16(targetStart + TARGET_LAST_WORD,
                           toUnsigned16(lastword | 0x8000));
  }
  public boolean hasMoreInput() {
    return source < sourceStart + maxLength;
  }
}
