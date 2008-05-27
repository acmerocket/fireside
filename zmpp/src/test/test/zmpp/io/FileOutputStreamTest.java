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
   
    outstream.print('a');
    outstream.print(ZsciiEncoding.NEWLINE);
    outstream.print('b');
    outstream.print(ZsciiEncoding.NEWLINE);
  }
}
