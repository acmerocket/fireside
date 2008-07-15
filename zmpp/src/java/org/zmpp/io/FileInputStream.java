/*
 * $Id$
 * 
 * Created on 11/08/2005
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
package org.zmpp.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.util.logging.Logger;
import org.zmpp.encoding.IZsciiEncoding;

/**
 * This class implements a Z-machine input stream that takes its input from
 * a file. It queries a screen model to provide the input file.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class FileInputStream implements InputStream {
  private static final Logger LOG = Logger.getLogger("FileInputStream");
  private IOSystem iosys;
  private IZsciiEncoding encoding;
  private Reader filereader;
  private BufferedReader input;
  
  /**
   * Constructor.
   * 
   * @param iosys an IOSystem object
   * @param encoding a ZSCII encoding object
   */
  public FileInputStream(IOSystem iosys, IZsciiEncoding encoding) {
    this.iosys = iosys;
    this.encoding = encoding;
  }

  /**
   * {@inheritDoc}
   */
  public String readLine() {
    checkForReader();
    if (input != null) {
      // Read from file
      try {
        if (input.ready()) {
          String line = input.readLine();
          /*
          if (encoding.isConvertableToZscii(c)) {
            return encoding.getZsciiChar(c);
          }*/
          return new String(encoding.convertToZscii(line));
        }
      } catch (IOException ex) {
        LOG.throwing("FileInputStream", "readLine", ex);
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    if (input != null) {
      try {
        input.close();
        input = null;
      } catch (IOException ex) {
        LOG.throwing("FileInputStream", "close", ex);
      }
    }
    
    if (filereader != null) {
      try {
        filereader.close();
        filereader = null;
      } catch (IOException ex) {
        LOG.throwing("FileInputStream", "readLine", ex);
      }
    }      
  }
  
  private void checkForReader() {
    if (filereader == null) {
      filereader = iosys.getInputStreamReader();
      input = new BufferedReader(filereader);
    }
  }  
}
