package test.zmpp.encoding;


import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.base.Memory;
import org.zmpp.encoding.CustomAccentTable;

/**
 * Test class for CustomAccentTable.
 * @author Wei-ju Wu
 * @version 1.0
 */
@RunWith(JMock.class)
public class CustomAccentTableTest {
  private static final int ADDRESS = 4711;
  private Mockery context = new JUnit4Mockery();
  private Memory memory = context.mock(Memory.class);
  private CustomAccentTable accentTable, noAccentTable;

  @Before
  public void setUp() throws Exception {
    accentTable = new CustomAccentTable(memory, ADDRESS);
    noAccentTable = new CustomAccentTable(memory, 0);
  }

  @Test
  public void testGetLengthNoTable() {
    assertEquals(0, noAccentTable.getLength());
  }

  @Test
  public void testGetLength() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(ADDRESS); will(returnValue((char) 3));
    }});
    assertEquals(3, accentTable.getLength());
  }

  @Test
  public void testGetAccentNoTable() {
    assertEquals('?', noAccentTable.getAccent(42));
  }

  @Test
  public void testGetAccent() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned16(ADDRESS + 7); will(returnValue('^'));
    }});
    assertEquals('^', accentTable.getAccent(3));
  }

  @Test
  public void testGetIndexOfLowerCase() {
    context.checking(new Expectations() {{
      // length
      atLeast(1) .of(memory).readUnsigned8(ADDRESS);
      will(returnValue((char) 80));
      // reference character
      one (memory).readUnsigned16(ADDRESS + 2 * 6 + 1); will(returnValue('B'));
      
      one (memory).readUnsigned16(ADDRESS + 1); will(returnValue('a'));
      one (memory).readUnsigned16(ADDRESS + 2 + 1); will(returnValue('b'));
    }});
    assertEquals(1, accentTable.getIndexOfLowerCase(6));
  }
}
