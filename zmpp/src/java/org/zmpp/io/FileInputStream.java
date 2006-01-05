/*
 * $Id$
 * 
 * Created on 08.11.2005
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
package org.zmpp.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.zmpp.vmutil.ZsciiEncoding;

/**
 * This class implements a Z-machine input stream that takes its input from
 * a file. It queries a screen model to provide the input file.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class FileInputStream implements InputStream {

  private IOSystem iosys;
  private Reader filereader;
  private BufferedReader input;
  
  /**
   * Constructor.
   * 
   * @param iosys an IOSystem object
   */
  public FileInputStream(IOSystem iosys) {

    this.iosys = iosys;
  }

  public void cancelInput() {
    
  }
  
  /**
   * {@inheritDoc}
   */
  public short getZsciiChar() {
    
    checkForReader();
    if (input != null) {
      
      // Read from file
      try {
        
        if (input.ready()) {
          
          ZsciiEncoding encoding = ZsciiEncoding.getInstance();
          char c = (char) input.read();
          if (encoding.isConvertableToZscii(c)) {
            
            return encoding.getZsciiChar((char) c);
          }
        }
        
      } catch (IOException ex) {
        
        ex.printStackTrace();
      }
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public void close() {

    if (input != null) {
      try {
        
        input.close();
        input = null;
        
      } catch (IOException ex) { }
    }
    
    if (filereader != null) {
      
      try {
        
        filereader.close();
        filereader = null;
        
      } catch (IOException ex) { }
    }      
  }
  
  private void checkForReader() {
    
    if (filereader == null) {
      
      filereader = iosys.getInputStreamReader();
      input = new BufferedReader(filereader);
    }
  }  
}
