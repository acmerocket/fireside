package test.zmpp.swingui;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.zmpp.swingui.WordWrapper;

public class WordWrapperTest extends TestCase {

  private WordWrapper bufferedWordWrapper;
  private WordWrapper unbufferedWordWrapper;
  private FontMetrics fontMetrics;
  
  protected void setUp() throws Exception {
 
    JFrame frame = new JFrame();
    fontMetrics =
      frame.getFontMetrics(new Font("Courier", Font.ROMAN_BASELINE, 8));
    bufferedWordWrapper = new WordWrapper(100, fontMetrics, true);
    unbufferedWordWrapper = new WordWrapper(85, fontMetrics, false);
  }

  public void testWrapBuffered() {

    String line = "A line that is to be wrapped";
    assertEquals(140, fontMetrics.stringWidth(line));
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(2, lines.length);
    assertEquals("A line that is to be", lines[0]);
    assertEquals(" wrapped", lines[1]);
  }
  
  public void testWrapBufferedWithNewLine() {
    
    String line =  "A line that\nis to\nbe wrapped";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(3, lines.length);
    assertEquals("A line that", lines[0]);
    assertEquals("is to", lines[1]);
    assertEquals("be wrapped", lines[2]);
  }

  public void testWrapBufferedWithNewLineEndsWithNewLine() {
    
    String line =  "A line that\nis to\nbe wrapped\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(4, lines.length);
    assertEquals("A line that", lines[0]);
    assertEquals("is to", lines[1]);
    assertEquals("be wrapped", lines[2]);
    assertEquals("", lines[3]);
  }
  
  public void testWrapBufferedNewLine() {
    
    String line =  "\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(1, lines.length);
    assertEquals("", lines[0]);
  }
  
  public void testWrapUnbuffered() {

    String line = "A line that is going to be wrapped";
    assertEquals(170, fontMetrics.stringWidth(line));
    String[] lines = unbufferedWordWrapper.wrap(0, line);
    assertEquals(2, lines.length);
    assertEquals("A line that is go", lines[0]);
    assertEquals("ing to be wrapped", lines[1]);
  }
}
