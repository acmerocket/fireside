/*
 * $Id$
 * 
 * Created on 12/22/2005
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zmpp.base.MemoryAccess;
import org.zmpp.vmutil.ZCharConverter;
import org.zmpp.vmutil.ZsciiEncoding;
import org.zmpp.vmutil.ZCharConverter.Alphabet;

/**
 * This class contains functions that deal with input.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class InputFunctions {

  private Machine machine;
  private boolean inputMode;
  private List<Short> inputbuffer;

  class InterruptThread extends Thread {
  
    /**
     * The interval in milliseconds.
     */
    private int time;
    
    /**
     * The packed routine address.
     */
    private int routineAddress;
    
    /**
     * After a routine has run, true indicates that it has printed something.
     */
    private boolean routineDidOutput;
    
    public InterruptThread(int time, int routineAddress) {
      
      this.time = time;
      this.routineAddress = routineAddress;
    }
    
    public void run() {
      
      while (inputMode) {
        
        try { Thread.sleep(time); } catch (Exception ex) { }
        short retval = runRoutine();
        if (retval == 1) {
          
          machine.getSelectedInputStream().cancelInput();
          break;
        }
        
        // REDISPLAY INPUT HERE
        // We need to find out if the routine has printed anything to
        // the screen if yes, the input needs to be redisplayed
        if (inputbuffer != null && routineDidOutput) {
          
          for (short zsciiChar : inputbuffer) {
            
            machine.printZsciiChar(zsciiChar, false);
          }
        }
        machine.flushOutput();
      }
    }
    
    private short runRoutine() {
     
      routineDidOutput = false;
      int originalRoutineStackSize = machine.getRoutineContexts().size();
      RoutineContext routineContext = machine.call(routineAddress,
          machine.getProgramCounter(),
          new short[0], (short) RoutineContext.DISCARD_RESULT);
      
      for (;;) {
        
        Instruction instr = machine.nextStep();
        instr.execute();
        // check if something was printed
        if (instr.isOutput()) routineDidOutput = true;
        if (machine.getRoutineContexts().size() == originalRoutineStackSize) {
          
          break;
        }
      }
      return routineContext.getReturnValue();
    }
  }
  
  public InputFunctions(Machine machine) {
    
    this.machine = machine;
  }
  
  /**
   * Reads a string from the selected input stream.
   * 
   * @param address the start address in memory
   * @param bufferlen the length of the buffer
   * @param time the time interval to call routine
   * @param routineAddress the packed routine address
   */
  public short readLine(int address, int bufferlen, int time,
      int routineAddress) {
    
    // Using a Vector for synchronization
    inputbuffer = new Vector<Short>();
    
    short zsciiChar;
    int version = machine.getStoryFileHeader().getVersion();
    boolean isAtLeastV4 = version >= 4;
    boolean isAtLeastV5 = version >= 5;
    MemoryAccess memaccess = machine.getMemoryAccess();
    
    // From V5, the first byte contains the number of characters typed
    int pointerstart = isAtLeastV5 ? 1 : 0;
    int pointer = pointerstart;
    Thread thread = null;
    String initString = "";
    
    if (isAtLeastV5) {
      
      // The clunky feature to include previous input into the current input
      // Simply adjust the pointer, the differencing at the end of the
      // function will then calculate the total
      int numCharactersTyped = memaccess.readByte(address);
      if (numCharactersTyped < 0) numCharactersTyped = 0;
      if (numCharactersTyped > 0) {
        
        StringBuilder leftover = new StringBuilder();
        ZsciiEncoding encoding = ZsciiEncoding.getInstance();
        
        for (int i = 0; i < numCharactersTyped; i++) {
          
 
          short zsciichar = memaccess.readUnsignedByte(
              address + pointerstart + i);
          leftover.append(encoding.getUnicodeChar(zsciichar));
          inputbuffer.add(zsciichar);
        }
        initString = leftover.toString();
        
        // The input stream needs to be preinitialized with
        // an edit string before entering edit mode if there is leftover
        // input.        
        machine.getSelectedInputStream().forceEditMode(initString);
      }
      pointer += numCharactersTyped;
    }
    
    inputMode = true;
    if (isAtLeastV4 && time > 0 && routineAddress != 0) {
    
      double dtime = ((double) time) / 10.0 * 1000.0;
      thread = new InterruptThread((int) dtime, routineAddress);
      thread.start();
    }
    
    do {
      
      zsciiChar = machine.getSelectedInputStream().readZsciiChar();
      
      // Decrement the buffer pointer
      if (zsciiChar == ZsciiEncoding.DELETE
          && pointer > pointerstart // is this check doppelt gemoppelt ?
          && inputbuffer.size() > 0) {
        
        inputbuffer.remove(inputbuffer.size() - 1);
        pointer--;
        
      } else if (zsciiChar != ZsciiEncoding.NEWLINE) {
        
        // Do not include the terminator in the buffer
        memaccess.writeByte(address + pointer, (byte) zsciiChar);
        inputbuffer.add(zsciiChar);
        pointer++;
      }      
      machine.printZsciiChar(zsciiChar, true);
      
    } while (zsciiChar != ZsciiEncoding.NEWLINE
             && zsciiChar != ZsciiEncoding.NULL
             && pointer < bufferlen - 1);

    inputMode = false;
    if (thread != null) {
      
      try {
        
        thread.join();
        
      } catch (Exception ex) { }
    }
    
    if (isAtLeastV5) {
    
      // Check if was cancelled
      byte numCharsTyped = (zsciiChar != ZsciiEncoding.NULL) ?
          (byte) (pointer - 1) : 0;
          
      // Write the number of characters typed in byte 1
      memaccess.writeUnsignedByte(address, numCharsTyped);
      
    } else {
      
      // Terminate with 0 byte in versions < 5
      // Check if input was cancelled
      if (zsciiChar == ZsciiEncoding.NULL) pointer = 0;
      memaccess.writeByte(address + pointer, (byte) 0);
    }
    
    // Clean up
    inputbuffer = null;
    
    if (zsciiChar != ZsciiEncoding.NULL) {
      
      // Echo a newline into the streams
      machine.printZsciiChar(ZsciiEncoding.NEWLINE, true);
      return ZsciiEncoding.NEWLINE;
      
    } else {
      
      return ZsciiEncoding.NULL;
    }
  }
  
  /**
   * Reads a ZSCII char from the selected input stream.
   * 
   * @param time the time interval to call routine (timed input)
   * @param routineAddress the packed routine address to call (timed input)
   * @return the selected ZSCII char
   */
  public short readChar(int time, int routineAddress) {
    
    Thread thread = null;
    inputMode = true;
    
    if (machine.getStoryFileHeader().getVersion() >= 4
        && time > 0 && routineAddress != 0) {
      
      double dtime = ((double) time) / 10.0 * 1000.0;
      thread = new InterruptThread((int) dtime, routineAddress);
      thread.start();
    }
    short result = machine.getSelectedInputStream().getZsciiChar();
    inputMode = false;
    
    if (thread != null) {
      try { thread.join(); } catch (Exception ex) { }
    }
    return result;
  }
  
  public void tokenize(int textbuffer, int parsebuffer) {
    
    MemoryAccess memaccess = machine.getMemoryAccess();
    Dictionary dictionary = machine.getDictionary();
    int version = machine.getStoryFileHeader().getVersion();
    int bufferlen = memaccess.readUnsignedByte(textbuffer);
    int charsTyped = (version >= 5) ?
                      memaccess.readUnsignedByte(textbuffer + 1) :
                      0;
    
    // from version 5, text starts at position 2
    int textoffset = (version < 5) ? 1 : 2; 
    String input = bufferToString(textbuffer + textoffset, bufferlen,
                                  charsTyped);
    List<String> tokens = tokenize(input);
    
    Map<String, Integer> parsedTokens = new HashMap<String, Integer>();
    
    // Write the number of tokens in byte 1 of the parse buffer
    int maxwords = memaccess.readUnsignedByte(parsebuffer);
    
    // Do not go beyond the limit of maxwords
    int numParsedTokens = Math.min(maxwords, tokens.size());
    
    // Write the number of parsed tokens into byte 1 of the parse buffer
    memaccess.writeUnsignedByte(parsebuffer + 1, (short) numParsedTokens);
    
    int parseaddr = parsebuffer + 2;
    
    for (int i = 0; i < numParsedTokens; i++) {
      
      String token = tokens.get(i);      
      int entryAddress = dictionary.lookup(token);
      //System.out.println("token: " + token + " entryAddress: " + entryAddress);
      
      int startIndex = 0;
      if (parsedTokens.containsKey(token)) {
          
        int timesContained = parsedTokens.get(token);
        parsedTokens.put(token, timesContained + 1);
          
        for (int j = 0; j < timesContained; j++) {
          
          int found = input.indexOf(token, startIndex);
          startIndex = found + token.length();
        }
          
      } else {
          
        parsedTokens.put(token, 1);          
      }
      
      int tokenIndex = input.indexOf(token, startIndex);    
      tokenIndex = tokenIndex + 1; // because of the length byte
      
      // write out the entry to the parse buffer
      memaccess.writeUnsignedShort(parseaddr, entryAddress);     
      memaccess.writeUnsignedByte(parseaddr + 2, (short) token.length());
      memaccess.writeUnsignedByte(parseaddr + 3, (short) tokenIndex);
      parseaddr += 4;
    }
  }  

  /**
   * Turns the buffer into a Java string. This function reads at most
   * |bufferlen| bytes and treats each byte as an ASCII character.
   * The characters will be concatenated to the result string.
   * 
   * @param address the buffer address
   * @param bufferlen the buffer length
   * @param charsTyped from version 5, this is the number of characters
   * to include in the input
   * @return the string contained in the buffer
   */
  private String bufferToString(int address, int bufferlen, int charsTyped) {
    
    MemoryAccess memaccess = machine.getMemoryAccess();
    
    // If charsTyped is set, use that value as the limit
    int numChars = (charsTyped > 0) ? charsTyped : bufferlen;
    
    // read input from text buffer
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < numChars; i++) {
      
      short charByte = memaccess.readUnsignedByte(address + i);
      if (charByte == 0) break;
      buffer.append((char) charByte);
    }
    
    return buffer.toString().toLowerCase();
  }
  
  /**
   * Turns the specified input string into tokens. It will take whitespace
   * implicitly and dictionary separators explicitly to tokenize the
   * stream, dictionary specified separators are included in the result list.
   * 
   * @param input the input string
   * @return the tokens
   */
  private List<String> tokenize(String input) {
    
    List<String> result = new ArrayList<String>();
    String whitespace = " \n\t\r\f";
    
    // Retrieve the defined separators
    StringBuilder separators = new StringBuilder();
    Dictionary dictionary = machine.getDictionary();
    for (int i = 0, n = dictionary.getNumberOfSeparators(); i < n; i++) {
      
      byte delim = dictionary.getSeparator(i);
      separators.append(ZCharConverter.decode(Alphabet.A0, delim));
    }
    
    // The tokenizer will also return the delimiters
    String delim = whitespace + separators.toString();
    StringTokenizer tok = new StringTokenizer(input, delim, true);
    
    while (tok.hasMoreTokens()) {
      
      String token = tok.nextToken();
      if (!Character.isWhitespace(token.charAt(0))) {
        
        result.add(token);
      }
    }
    return result;
  }  
}
