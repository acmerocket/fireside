/*
 * $Id$
 * 
 * Created on 2006/02/14
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

import java.io.Closeable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.StoryFileHeader.Attribute;

public class OutputImpl implements Output, Closeable {

  private Machine machine;
  
  /**
   * This is the array of output streams.
   */
  private OutputStream[] outputStream;
  
  public OutputImpl(final Machine machine) {
    super();
    this.machine = machine;
    outputStream = new OutputStream[3];
  }
  
  /**  
   * Sets the output stream to the specified number.
   * @param streamnumber the stream number
   * @param stream the output stream
   */
  public void setOutputStream(final int streamnumber,
      final OutputStream stream) {
    
    outputStream[streamnumber - 1] = stream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void printZString(final int address) {
    print(machine.decode2Zscii(address, 0));
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(final ZsciiString str) {
    //System.out.println("print: '" + str + "'");
    printZsciiChars(str);
  }
  
  /**
   * {@inheritDoc}
   */
  public void newline() {
    printZsciiChar(ZsciiEncoding.NEWLINE);
  }
  
  private char[] zchars = new char[1];
  
  /**
   * {@inheritDoc}
   */
  public void printZsciiChar(final char zchar) {
    //System.out.println("printZsciiChar: '" + (char) zchar + "'");
    zchars[0] = zchar;
    printZsciiChars(new ZsciiString(zchars));
  }

  /**
   * Prints the specified array of ZSCII characters. This is the only function
   * that communicates with the output streams directly.
   * 
   * @param zsciiString the array of ZSCII characters.
   */
  private void printZsciiChars(final ZsciiString zsciiString) {
    
    checkTranscriptFlag();
    
    if (outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      for (int i = 0, n = zsciiString.length(); i < n; i++) {
        
        outputStream[OUTPUTSTREAM_MEMORY - 1].print(zsciiString.charAt(i));
      }
      
    } else {
    
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          for (int j = 0, n = zsciiString.length(); j < n; j++) {
          
            outputStream[i].print(zsciiString.charAt(j));
          }
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void printNumber(final short number) {
    
    print(new ZsciiString(String.valueOf(number)));
  }
  
  public void flushOutput() {
    
    // At the moment flushing only makes sense for screen
    if (!outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          outputStream[i].flush();
        }
      }
    }
  }

  /**
   * Checks the fileheader if the transcript flag was set by the game
   * bypassing output_stream, e.g. with a storeb to the fileheader flags
   * address. Enable the transcript depending on the status of that flag.
   */
  private void checkTranscriptFlag() {
    if (outputStream[OUTPUTSTREAM_TRANSCRIPT - 1] != null) {
      outputStream[OUTPUTSTREAM_TRANSCRIPT - 1].select(
          machine.getFileHeader().isEnabled(Attribute.TRANSCRIPTING));
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectOutputStream(final int streamnumber, final boolean flag) {
    
    outputStream[streamnumber - 1].select(flag);
    
    // Sets the tranxdQscript flag if the transcipt is specified
    if (streamnumber == OUTPUTSTREAM_TRANSCRIPT) {
      //System.out.println("ENABLE_TRANSCRIPT_STREAM: " + flag);
      machine.getFileHeader().setEnabled(Attribute.TRANSCRIPTING, flag);      
    } else if (streamnumber == OUTPUTSTREAM_MEMORY && flag) {
      
      machine.halt("invalid selection of memory stream");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectOutputStream3(final int tableAddress,
      final int tableWidth) {

    ((MemoryOutputStream) outputStream[OUTPUTSTREAM_MEMORY - 1]).select(
        tableAddress, tableWidth);
  }
  
  /**
   * {@inheritDoc}
   */
  public void close() {
    if (outputStream != null) {
      for (int i = 0; i < outputStream.length; i++) {
        if (outputStream[i] != null) {
          outputStream[i].flush();
          outputStream[i].close();
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void reset() {    
    for (int i = 0; i < outputStream.length; i++) {
      if (outputStream[i] != null) {
        outputStream[i].flush();
      }
    }
  }
}
