/*
 * $Id$
 * 
 * Created on 2008/07/28
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
package org.zmpp.swingui2;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.windowing.ScreenModel;
import org.zmpp.windowing.TextAnnotation;

/**
 * The Swing component to represent the lower window.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class TextWindowView extends JTextPane {
  
  private ScreenModelSplitView parent;
  
  /**
   * Constructor.
   * @param parent the parent view
   */
  public TextWindowView(ScreenModelSplitView parent) {
    this.parent = parent;
  }
  
  /**
   * Ties the background color of the MainView to the background of the
   * TextPane. That's because the upper window is treated as a transparent
   * overlay that overlaps with the bottom window.
   * @param color the new background color
   */
  @Override
  public void setBackground(Color color) {
    super.setBackground(color);
    if (parent != null) { parent.setBackground(color); }
  }

  /**
   * Appends the specified annotated segment to the end of the displayed
   * displayed document.
   * @param segment the annotated text to append
   */
  public void append(AnnotatedText segment) {
    Document doc = getDocument();
    try {
      doc.insertString(doc.getLength(), zsciiToUnicode(segment.getText()),
                       setStyleAttributes(segment.getAnnotation()));
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * Convert a ZSCII string to a Unicode string
   * @param zsciiString the ZSCII string
   * @return the unicode representation
   */
  private String zsciiToUnicode(String zsciiString) {
    return zsciiString.replace("\r", "\n");
  }

  /**
   * Clears the text by printing a form feed.
   * @param background the background color in ScreenModel constants
   * @param foreground the foreground color in ScreenModel constants
   */
  public void clear(int background, int foreground) {
    setComponentColors(background, foreground);
    try {
      TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_NORMAL,
        ScreenModel.TEXTSTYLE_ROMAN, background, foreground);
      append(new AnnotatedText(annotation, getFormFeed()));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * Builds a form feed string by creating as many newlines as the component
   * contains lines.
   * @return the form feed string
   */
  private String getFormFeed() {
    StringBuilder formFeed = new StringBuilder();
    for (int i = 0; i < parent.getNumUpperRows(); i++) {
      formFeed.append("\n");
    }
    return formFeed.toString();
  }

  /**
   * Sets this components colors with the colors specified in ScreenModel
   * color constants
   * @param background the background color
   * @param foreground the foreground color
   */
  private void setComponentColors(int background, int foreground) {
    ColorTranslator translator = ColorTranslator.getInstance();
    setBackground(translator.translate(background,
      parent.getDefaultBackground()));
    setForeground(translator.translate(foreground,
      parent.getDefaultForeground()));
  }
  
  /**
   * Sets the current style for user input in this component.
   * @param annotation the current annotation holding the style
   */
  public void setCurrentStyle(TextAnnotation annotation) {
    setStyleAttributes(annotation);
  }
  
  /**
   * Sets the current style attributes and returns them.
   * @param annotation the annotation that specifies the new style
   * @return the changed attribute set
   */
  private MutableAttributeSet setStyleAttributes(TextAnnotation annotation) {
    MutableAttributeSet attributes = getInputAttributes();
    Font font = parent.getFont(annotation);
    StyleConstants.setFontFamily(attributes, font.getFamily());
    StyleConstants.setFontSize(attributes, font.getSize());
    StyleConstants.setBold(attributes, annotation.isBold());
    StyleConstants.setItalic(attributes, annotation.isItalic());
    ColorTranslator colorTranslator = ColorTranslator.getInstance();
    Color background = colorTranslator.translate(annotation.getBackground(),
            parent.getDefaultBackground());
    Color foreground = colorTranslator.translate(annotation.getForeground(),
            parent.getDefaultForeground());
    if (annotation.isReverseVideo()) {
      StyleConstants.setBackground(attributes, foreground);
      StyleConstants.setForeground(attributes, background.brighter());
    } else {
      StyleConstants.setBackground(attributes, background);
      StyleConstants.setForeground(attributes, foreground.brighter());
    }
    return attributes;
  }  
}
