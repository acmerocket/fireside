/*
 * Created on 09/24/2005
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
package test.zmpp.instructions;


import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.Operand.OperandType;

/**
 * Test class for Operand.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class OperandTest {

  @Test
  public void testCreateOperand() {
    Operand operand1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    Operand operand2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 6);
    Operand operand3 = new Operand(Operand.TYPENUM_VARIABLE, (char) 11);
    Operand operand4 = new Operand(Operand.TYPENUM_OMITTED, (char) 13);
    
    assertEquals(5, operand1.getValue());
    assertEquals(6, operand2.getValue());
    assertEquals(11, operand3.getValue());
    assertEquals(13, operand4.getValue());
    
    assertEquals(operand1.getType(), OperandType.SMALL_CONSTANT);
    assertEquals(operand2.getType(), OperandType.LARGE_CONSTANT);
    assertEquals(operand3.getType(), OperandType.VARIABLE);
    assertEquals(operand4.getType(), OperandType.OMITTED);
  }
}
