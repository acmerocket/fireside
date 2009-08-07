/*
 * Created on 11/08/2005
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
package org.zmpp.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.logging.Logger;
import org.zmpp.encoding.IZsciiEncoding;

/**
 * This class defines an output stream for transcript output (Stream 2).
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class TranscriptOutputStream implements OutputStream {

  private static final Logger LOG = Logger.getLogger("org.zmpp");
  private IOSystem iosys;
  private BufferedWriter output;
  private Writer transcriptWriter;
  private boolean enabled;
  private StringBuilder linebuffer;
  private IZsciiEncoding encoding;
  private boolean initialized;

  /**
   * Constructor.
   * 
   * @param iosys the I/O system
   */
  public TranscriptOutputStream(final IOSystem iosys,
      final IZsciiEncoding encoding) {
  
    super();
    this.iosys = iosys;
    this.encoding = encoding;
    linebuffer = new StringBuilder();
  }
  
  /**
   * {@inheritDoc}
   */
  private void initFile() {
    if (!initialized && transcriptWriter == null) {
      transcriptWriter = iosys.getTranscriptWriter();
      if (transcriptWriter != null) {
      	output = new BufferedWriter(transcriptWriter);
      }
      initialized = true;
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(final char zsciiChar) {
    initFile();
    if (output != null) {
      if (zsciiChar == IZsciiEncoding.NEWLINE) { 
        flush();
      } else if (zsciiChar == IZsciiEncoding.DELETE) {
        linebuffer.deleteCharAt(linebuffer.length() - 1);
      } else {
        linebuffer.append(encoding.getUnicodeChar(zsciiChar));
      }
      flush();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void select(final boolean flag) { enabled = flag; }

  /**
   * {@inheritDoc}
   */
  public boolean isSelected() { return enabled; }
  
  /**
   * {@inheritDoc}
   */
  public void flush() {
    try {
      if (output != null) {        
        output.write(linebuffer.toString());
        linebuffer = new StringBuilder();
      }
    } catch (IOException ex) { 
        LOG.throwing("TranscriptOutputStream", "flush", ex);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void close() {
    if (output != null) {
      try {
        output.close();
        output = null;
      } catch (Exception ex) {
        LOG.throwing("TranscriptOutputStream", "close", ex);
      }      
    }
    
    if (transcriptWriter != null) {
      try {
        transcriptWriter.close();
        transcriptWriter = null;
      } catch (Exception ex) {
        LOG.throwing("TranscriptOutputStream", "close", ex);
      }      
    }
    initialized = false;
  }  
}
