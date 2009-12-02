/*
 * Created on 12/06/2005
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
package test.zmpp.vm;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MemoryOutputStream;

/**
 * Test class for MemoryOutputStream.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class MemoryOutputStreamTest {
  Mockery context = new JUnit4Mockery();
  private Machine machine;
  private MemoryOutputStream output;

  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    output = new MemoryOutputStream(machine);
  }
  
  @Test
  public void testPrintVersion5() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(5));
      one (machine).writeUnsigned8(4713, (char) 65);
      one (machine).writeUnsigned16(4711, (char) 1);
    }});
    // Selection has to be performed prior to printing - ALWAYS !!!
    output.select(4711, 0);    
    output.print((char) 65);
    output.select(false);
  }
  
  @Test
  public void testIsSelected() {
    output.select(4711, 0);
    assertTrue(output.isSelected());
  }
  
  @Test
  public void testUnusedMethods() {
    output.flush();
    output.close();
  }
  
  @Test
  public void testSelectMaxNesting() {
    context.checking(new Expectations() {{
      one (machine).halt("maximum nesting depth (16) for stream 3 exceeded");
    }});
    for (int i = 0; i < 17; i++) {
      output.select(4710 + 10 * i, 0);
    }
  }  
}
