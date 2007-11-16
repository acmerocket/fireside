package test.zmpp.vm;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.vm.ModernObjectTree;
import org.zmpp.vm.ObjectTree;

public class CursesObjectTreeTest extends CursesSetup {

	private static final int ADDR_7_20 	= 7734;
	private static final int ADDR_7_1 	= 7741;
	
  private ObjectTree objectTree;

  @Before
  public void setUp() throws Exception {
	  super.setUp();
	  objectTree = new ModernObjectTree(curses,
			fileheader.getObjectTableAddress());
  }
  
  @Test
  public void testGetPropertiesDescriptionAddress() {
	  assertEquals(0x2d40, objectTree.getPropertiesDescriptionAddress(123));
  }
  
  @Test
  public void testGetPropertyAddress() {
  	assertEquals(ADDR_7_20, objectTree.getPropertyAddress(7, 20));
  	assertEquals(ADDR_7_1, objectTree.getPropertyAddress(7, 1));
  }

  @Test
  public void testGetProperty() {
	  assertEquals(0, objectTree.getProperty(3, 22));
	  assertEquals(0x0006, objectTree.getProperty(3, 8));
	  assertEquals(0xb685, objectTree.getProperty(2, 20));
  }
  
  @Test
  public void testSetGetProperty() {
	  objectTree.setProperty(122, 34, 0xdefe);
	  assertEquals(0xdefe, objectTree.getProperty(122, 34));
  }
  
  @Test
  public void testGetNextProperty() {
  	assertEquals(24, objectTree.getNextProperty(7, 0));
  	assertEquals(20, objectTree.getNextProperty(7, 24));
  	assertEquals(8, objectTree.getNextProperty(7, 20));
  	assertEquals(1, objectTree.getNextProperty(7, 8));
  	assertEquals(0, objectTree.getNextProperty(7, 1));
  }

  @Test
  public void testGetPropertyLength() {
	  assertEquals(2, objectTree.getPropertyLength(ADDR_7_20));
	  assertEquals(6, objectTree.getPropertyLength(ADDR_7_1));
  }
  
  @Test
  public void testGetPropertyLengthAddress0() {
	  assertEquals(0, objectTree.getPropertyLength(0));
  }
}
