package test.zmpp.instructions;

import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.PrintLiteralStaticInfo;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.instructions.Short1StaticInfo;
import org.zmpp.instructions.VariableStaticInfo;

import junit.framework.TestCase;

public class StaticInstructionTest extends TestCase {

  // *******************************************************************
  // ********* GET_OP_NAME
  // *************************
  
  public void testGetLongOpName() {

    LongStaticInfo info = LongStaticInfo.getInstance();
    for (int i = LongStaticInfo.OP_JE; i <= LongStaticInfo.OP_CALL_2S; i++) {
      
      assertNotNull(info.getOpName(i, 3));      
    }    
    assertNotNull(info.getOpName(1234, 3));      
  }
  
  public void testGetOpNamePrintInstruction() {

    PrintLiteralStaticInfo info = PrintLiteralStaticInfo.getInstance();
    for (int i = PrintLiteralStaticInfo.OP_PRINT; i <= PrintLiteralStaticInfo.OP_PRINT_RET; i++) {
      
      assertNotNull(info.getOpName(i, 3));
    }
    assertNotNull(info.getOpName(1234, 3));
  }
  
  public void testStaticInformationPrintLiteral() {
    
    PrintLiteralStaticInfo info = PrintLiteralStaticInfo.getInstance();
    assertFalse(info.storesResult(PrintLiteralStaticInfo.OP_PRINT, 3));
    assertFalse(info.storesResult(PrintLiteralStaticInfo.OP_PRINT_RET, 3));
    
    assertFalse(info.isBranch(PrintLiteralStaticInfo.OP_PRINT, 3));
    assertFalse(info.isBranch(PrintLiteralStaticInfo.OP_PRINT_RET, 3));

    assertTrue(info.isOutput(PrintLiteralStaticInfo.OP_PRINT, 3));
    assertTrue(info.isOutput(PrintLiteralStaticInfo.OP_PRINT_RET, 3));
    
    int[] validVersions1 = info.getValidVersions(PrintLiteralStaticInfo.OP_PRINT);
    int[] validVersions2 = info.getValidVersions(PrintLiteralStaticInfo.OP_PRINT_RET);
    assertEquals(8, validVersions1.length);
    assertEquals(8, validVersions2.length);
  }  
  
  public void testGetOpNameShort0() {

    Short0StaticInfo info = Short0StaticInfo.getInstance();
    
    for (int i = Short0StaticInfo.OP_RTRUE; i <= Short0StaticInfo.OP_VERIFY; i++) {
      
      assertNotNull(info.getOpName(i, 3));
    }
    assertNotNull(info.getOpName(1234, 3));
  }
  
  public void testGetOpNameShort1() {

    Short1StaticInfo info = Short1StaticInfo.getInstance();
    
    for (int i = Short1StaticInfo.OP_JZ; i <= Short1StaticInfo.OP_NOT; i++) {
      
      assertNotNull(info.getOpName(i, 3));
    }
    assertNotNull(info.getOpName(12345, 3));
  }
  
  public void testGetOpNameShort1V5() {

    Short1StaticInfo info = Short1StaticInfo.getInstance();
    assertEquals("CALL_1N", info.getOpName(Short1StaticInfo.OP_CALL_1N, 5));
  }

  public void testGetOpNameVariable() {

    VariableStaticInfo info = VariableStaticInfo.getInstance();
    
    for (int i = VariableStaticInfo.OP_CALL; i <= VariableStaticInfo.OP_TOKENISE; i++) {
      
      assertNotNull(info.getOpName(i, 3));
    }    
    assertNotNull(info.getOpName(1234, 3));
  }
  
  public void testGetOpNameVariableV5() {

    VariableStaticInfo info = VariableStaticInfo.getInstance();
    assertEquals("AREAD", info.getOpName(VariableStaticInfo.OP_AREAD, 5));
  }
  
  
}
