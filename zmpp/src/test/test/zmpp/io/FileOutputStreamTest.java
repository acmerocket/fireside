/*
 * $Id$
 * 
 * Created on 11/08/2005
 * Copyright 2005-2006 by Wei-ju Wu
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
package test.zmpp.io;

import java.io.FileWriter;
import java.io.Writer;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.io.IOSystem;
import org.zmpp.io.TranscriptOutputStream;

public class FileOutputStreamTest extends MockObjectTestCase {

  private Mock mockIo;
  private IOSystem iosys;
  private TranscriptOutputStream outstream;
  private Writer outputwriter;
  
  protected void setUp() throws Exception {
    
    mockIo = mock(IOSystem.class);
    iosys = (IOSystem) mockIo.proxy();
    outstream = new TranscriptOutputStream(iosys, new ZsciiEncoding(new DefaultAccentTable()));
    outputwriter = new FileWriter("testfiles/streamoutput.txt");
  }

  protected void tearDown() throws Exception {
    
    outstream.close();
  }

  public void testPrintFirstTime() {

    mockIo.expects(once()).method("getTranscriptWriter").will(returnValue(outputwriter));
   
    outstream.print((short) 'a', false);
    outstream.print(ZsciiEncoding.NEWLINE, false);
    outstream.print((short) 'b', false);
    outstream.print(ZsciiEncoding.NEWLINE, false);
  }
}
