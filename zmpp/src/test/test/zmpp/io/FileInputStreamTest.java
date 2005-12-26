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
package test.zmpp.io;

import java.io.Reader;
import java.io.StringReader;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.io.FileInputStream;
import org.zmpp.io.IOSystem;

public class FileInputStreamTest extends MockObjectTestCase {

  private FileInputStream instream;
  private Mock mockIo;
  private IOSystem iosys;
  private Reader reader;
  
  protected void setUp() throws Exception {

    reader = new StringReader("Hello");
    mockIo = mock(IOSystem.class);
    iosys = (IOSystem) mockIo.proxy();
    instream = new FileInputStream(iosys);
  }

  public void testReadZsciiChar() {
    
    mockIo.expects(once()).method("getInputStreamReader").will(returnValue(reader));
    instream.readZsciiChar();
  }
  
  protected void tearDown() throws Exception {
    
    instream.close();
  }
}
