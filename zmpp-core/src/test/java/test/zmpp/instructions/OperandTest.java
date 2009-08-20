/*
 * Created on 09/24/2005
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