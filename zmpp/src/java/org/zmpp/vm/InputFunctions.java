/*
 * $Id$
 * 
 * Created on 12/22/2005
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
package org.zmpp.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zmpp.base.Memory;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.encoding.ZsciiStringBuilder;
import org.zmpp.encoding.ZsciiStringTokenizer;

/**
 * This class contains functions that deal with user input.
 * Note: For version 2.0 a number of changes will be performed on this
 * class. Timed input will be eliminated completely, as well as leftover.
 * Command history might be left out as well
 * 
 * @author Wei-ju Wu
 * @version 2.0
 */
public class InputFunctions {
  private Machine machine;
  /**
   * Constructor.
   * 
   * @param machine the machine object
   */
  public InputFunctions(Machine machine) {
    
    this.machine = machine;
  }

  // *********************************************************************
  // ****** SREAD/AREAD - the most complex and flexible function within the
  // ****** Z-machine. This function takes input from the user and
  // ****** calls the tokenizer for lexical analysis. It also recognizes
  // ****** terminator characters and controls the output as well as
  // ****** calling an optional interrupt routine.
  // *********************************************************************
  
  /**
   * By delegating responsibility for timed input to the user interface,
   * reading input is strongly simplified.
   * @param textbuffer
   * @return
   */
  public char readLine(final int textbuffer) {
    String inputLine = machine.getSelectedInputStream().readLine();
    processInput(textbuffer, inputLine);
    return inputLine.charAt(inputLine.length() - 1);
  }
  
  /**
   * Depending on the terminating character and the story file version,
   * either write a 0 to the end of the text buffer or write the length
   * of to the text buffer's first byte.
   * 
   * @param terminateChar the terminating character
   * @param textbuffer the text buffer
   * @param textpointer points at the position behind the last input char
   */
  public void checkTermination(final char terminateChar, final int textbuffer,
                               final int textpointer) {
    
    final int version = machine.getVersion();
    
    if (version >= 5) {      
      // Check if was cancelled
      final byte numCharsTyped = (terminateChar == ZsciiEncoding.NULL) ?
          0 : (byte) (textpointer - 2);

      // Write the number of characters typed in byte 1
      machine.writeUnsignedByte(textbuffer + 1, numCharsTyped);      
    } else {      
      // Terminate with 0 byte in versions < 5
      // Check if input was cancelled
      int terminatepos = textpointer; // (textpointer - textbuffer + 2);
      if (terminateChar == ZsciiEncoding.NULL) {
        terminatepos = 0;
      }
      machine.writeByte(textbuffer + terminatepos, (byte) 0);
    }
  }
  
  private void processInput(final int textbuffer, String inputString) {
    final int bufferlen = machine.readUnsignedByte(textbuffer);
    int storeOffset = machine.getVersion() <= 4 ? 1 : 2;
    for (int i = 0; i < inputString.length(); i++) {
      machine.writeByte(textbuffer + i + storeOffset,
              (byte) inputString.charAt(i));
    }
    char terminateChar = inputString.charAt(inputString.length() - 1);
    checkTermination(terminateChar, textbuffer, inputString.length() + 1);
  }

  private boolean isTerminatingCharacter(final char zsciiChar) {
    
    return isFileHeaderTerminator(zsciiChar) 
           || zsciiChar == ZsciiEncoding.NEWLINE
           || zsciiChar == ZsciiEncoding.NULL;
  }
  
  private boolean isFileHeaderTerminator(final char zsciiChar) {
    if (machine.getVersion() >= 5) {  
      final StoryFileHeader fileheader = machine.getFileHeader();
      final int terminatorTable = fileheader.getTerminatorsAddress();
      if (terminatorTable == 0) {
        return false;
      }
    
      // Check the terminator table
      short terminator;
    
      for (int i = 0; ; i++) {      
        terminator = machine.readUnsignedByte(terminatorTable + i);
        if (terminator == 0) {
          break;
        }
        if (terminator == 255) {
          return ZsciiEncoding.isFunctionKey(zsciiChar);
        }
        if (terminator == zsciiChar) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Depending on the terminating character, return the terminator to
   * the caller. We need this since aread stores the terminating character
   * as a result. If a newline was typed as the terminator, a newline
   * will be echoed, in all other cases, the terminator is simply returned.
   * 
   * @param terminateChar the terminating character
   * @return a terminating character that can be stored as a result
   */
  public char handleTerminateChar(final char terminateChar) {
    
    if (terminateChar == ZsciiEncoding.NEWLINE) {
      
      // Echo a newline into the streams
      // must be called with isInput == false since we are not
      // in input mode anymore when we receive NEWLINE
      machine.printZsciiChar(ZsciiEncoding.NEWLINE);      
    }      
    return terminateChar;
  }
  
  // **********************************************************************
  // ****** READ_CHAR
  // *******************************
  /**
   * {@inheritDoc}
   */
  public char readChar() {
    String inputLine = machine.getSelectedInputStream().readLine();
    return inputLine.charAt(0);
  }
  
  /**
   * {@inheritDoc}
   */
  public void tokenize(final int textbuffer, final int parsebuffer,
                       final int dictionaryAddress, final boolean flag) {
    final int version = machine.getVersion();
    final int bufferlen = machine.readUnsignedByte(textbuffer);
    final int textbufferstart = determineTextBufferStart(version);
    final int charsTyped = (version >= 5) ?
                      machine.readUnsignedByte(textbuffer + 1) :
                      0;
    
    // from version 5, text starts at position 2
    final ZsciiString input = bufferToZscii(textbuffer + textbufferstart, bufferlen,
                                            charsTyped);
    final List<ZsciiString> tokens = tokenize(input);
    
    final Map<ZsciiString, Integer> parsedTokens =
      new HashMap<ZsciiString, Integer>();
    
    // Write the number of tokens in byte 1 of the parse buffer
    final int maxwords = machine.readUnsignedByte(parsebuffer);
    
    // Do not go beyond the limit of maxwords
    final int numParsedTokens = Math.min(maxwords, tokens.size());
    
    // Write the number of parsed tokens into byte 1 of the parse buffer
    machine.writeUnsignedByte(parsebuffer + 1, (short) numParsedTokens);
    
    int parseaddr = parsebuffer + 2;
    
    for (int i = 0; i < numParsedTokens; i++) {
      
      final ZsciiString token = tokens.get(i);   
      final int entryAddress = machine.lookupToken(dictionaryAddress, token);
      //System.out.println("token: '" + token + "' entryAddress: " + entryAddress);
      
      int startIndex = 0;
      if (parsedTokens.containsKey(token)) {
          
        final int timesContained = parsedTokens.get(token);
        parsedTokens.put(token, timesContained + 1);
          
        for (int j = 0; j < timesContained; j++) {
          final int found = input.indexOf(token, startIndex);
          startIndex = found + token.length();
        }    
      } else { 
        parsedTokens.put(token, 1);          
      }
      
      int tokenIndex = input.indexOf(token, startIndex);    
      
      tokenIndex++; // adjust by the buffer length byte
      
      if (version >= 5) {
        // if version >= 5, there is also numbers typed byte
        tokenIndex++;
      }
      
      // if the tokenize flag is not set, write out the entry to the
      // parse buffer, if it is set then, only write the token position
      // if the token was recognized
      if (!flag || flag && entryAddress > 0) {
        
        // This is one slot
        machine.writeUnsignedShort(parseaddr, entryAddress);     
        machine.writeUnsignedByte(parseaddr + 2, (short) token.length());
        machine.writeUnsignedByte(parseaddr + 3, (short) tokenIndex);
      }
      parseaddr += 4;
    }
  }  

  /**
   * Turns the buffer into a ZSCII string. This function reads at most
   * |bufferlen| bytes and treats each byte as an ASCII character.
   * The characters will be concatenated to the result string.
   * 
   * @param address the buffer address
   * @param bufferlen the buffer length
   * @param charsTyped from version 5, this is the number of characters
   * to include in the input
   * @return the string contained in the buffer
   */
  private ZsciiString bufferToZscii(final int address, final int bufferlen,
      final int charsTyped) {    
    // If charsTyped is set, use that value as the limit
    final int numChars = (charsTyped > 0) ? charsTyped : bufferlen;
    
    // read input from text buffer
    final ZsciiStringBuilder buffer = new ZsciiStringBuilder();
    for (int i = 0; i < numChars; i++) {
      
      final char charByte = (char) machine.readUnsignedByte(address + i);
      if (charByte == 0) {
        break;
      }
      buffer.append(charByte);
    }    
    return buffer.toZsciiString();
  }
  
  /**
   * Turns the specified input string into tokens. It will take whitespace
   * implicitly and dictionary separators explicitly to tokenize the
   * stream, dictionary specified separators are included in the result list.
   * 
   * @param input the input string
   * @return the tokens
   */
  private List<ZsciiString> tokenize(final ZsciiString input) {
    
    final List<ZsciiString> result = new ArrayList<ZsciiString>();    
    // The tokenizer will also return the delimiters
    final ZsciiString delim = machine.getDictionaryDelimiters();
    final ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    
    while (tok.hasMoreTokens()) {      
      final ZsciiString token = tok.nextToken();
      if (!Character.isWhitespace(token.charAt(0))) {
        
        result.add(token);
      }
    }
    return result;
  }
  
  /**
   * Depending on the version, this returns the offset where text starts in
   * the text buffer. In versions up to 4 this is 1, since we have the
   * buffer size in the first byte, from versions 5, we also have the
   * number of typed characters in the second byte.
   * 
   * @param version the story file version
   * @return 1 if version &lt; 4, 2, otherwise
   */
  private int determineTextBufferStart(final int version) {
    
    return (version < 5) ? 1 : 2;
  }
}
