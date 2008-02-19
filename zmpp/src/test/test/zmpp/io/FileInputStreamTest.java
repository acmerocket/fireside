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

import java.io.Reader;
import java.io.StringReader;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
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
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    
    iosys = (IOSystem) mockIo.proxy();
    instream = new FileInputStream(iosys, encoding);
  }

  public void testReadZsciiChar() {
    
    mockIo.expects(once()).method("getInputStreamReader").will(returnValue(reader));
    instream.getZsciiChar(true);
  }
  
  protected void tearDown() throws Exception {
    
    instream.close();
  }
}
