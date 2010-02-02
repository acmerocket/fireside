/*
 * Created on 12/06/2005
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
