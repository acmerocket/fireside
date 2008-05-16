/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zmpp.swingui2;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import org.zmpp.vm.ScreenModel;
import org.zmpp.windowing.AnnotatedCharacter;
import org.zmpp.windowing.TextAnnotation;

/**
 *
 * @author weiju
 */
public class TextPaneGridView extends JTextPane {

  private FontSelector fontSelector;
  private ColorTranslator colorTranslator = ColorTranslator.getInstance();
  private int numRows, numColumns;
  public TextPaneGridView() {
    this.setOpaque(false);
    //this.setEditable(false);
    // Here is a demonstration of focus transfer:
    // if this component gets it, it gets rid of it
    this.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        transferFocus();
      }      
    });
  }
  
  @Override
   // to preserve the full width of the text
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  public void setFontSelector(FontSelector selector) {
    this.fontSelector = selector;
    MutableAttributeSet attributes = getInputAttributes();
    StyleConstants.setFontFamily(attributes, fontSelector.getFixedFont().getFamily());
    StyleConstants.setFontSize(attributes, fontSelector.getFixedFont().getSize());
  }
  
  public void setGridSize(int numrows, int numcols) {
    numRows = numrows;
    numColumns = numcols;
  }
  
  public void clear() {
    try {
      getDocument().remove(0, getDocument().getLength());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    StringBuilder screenContent = new StringBuilder();
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numColumns; col++) {
        screenContent.append(" ");
      }
      screenContent.append("\n");
    }
    try {
      MutableAttributeSet attributes = getInputAttributes();
      StyleConstants.setBackground(attributes, Color.GREEN);
      //attributes.removeAttribute(StyleConstants.Background);
      getDocument().insertString(0, screenContent.toString(),
                                 attributes);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void setCharacter(int line, int column, AnnotatedCharacter c) {
    // Guarding writing out of bounds, some games do this
    if (outOfBounds(line, column)) return;
    int offset = getOffset(line, column);
    try {
      TextAnnotation annotation = c.getAnnotation();
      Color foreground = colorTranslator.translate(
        annotation.getForeground(), ScreenModel.COLOR_BLACK);
      Color background = colorTranslator.translate(
        annotation.getBackground(), ScreenModel.COLOR_WHITE);
      MutableAttributeSet attributes = getInputAttributes();
      StyleConstants.setForeground(attributes, foreground);
      StyleConstants.setBackground(attributes, background);
      
      getDocument().remove(offset, 1);
      getDocument().insertString(offset, String.valueOf(c.getCharacter()), attributes);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private boolean outOfBounds(int line, int column) {
    return (line - 1) >= numRows || (column - 1) >= numColumns;
  }
  
  private int getOffset(int line, int column) {
    return (line - 1) * numColumns + (column - 1) + line;
  }
}
