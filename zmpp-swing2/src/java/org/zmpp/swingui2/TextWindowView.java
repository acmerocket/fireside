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
  
  public TextWindowView(ScreenModelSplitView parent) {
    this.parent = parent;
  }
  
  /**
   * Ties the background color of the MainView to the background of the
   * TextPane. That's because the upper window is treated as transparent
   * at the beginning.
   * @param color the new background color
   */
  @Override
  public void setBackground(Color color) {
    super.setBackground(color);
    if (parent != null) { parent.setBackground(color); }
  }

  public void append(AnnotatedText segment) {
    Document doc = getDocument();
    try {
      doc.insertString(doc.getLength(), zsciiToUnicode(segment.getText()),
                       getStyleAttributes(segment.getAnnotation()));
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }
  private String zsciiToUnicode(String str) {
    return str.replace("\r", "\n");
  }

  /**
   * TODO: Can be done with setCurrentStyle.
   * @param background
   * @param foreground
   */
  public void clear(int background, int foreground) {
    try {
      ColorTranslator translator = ColorTranslator.getInstance();
      setBackground(translator.translate(
        background, ScreenModelSplitView.DEFAULT_BACKGROUND));
      setForeground(translator.translate(
        foreground, ScreenModelSplitView.DEFAULT_FOREGROUND));
      StringBuilder formFeed = new StringBuilder();
      for (int i = 0; i < parent.getNumUpperRows(); i++) {
        formFeed.append("\n");
      }
      TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_NORMAL,
        ScreenModel.TEXTSTYLE_ROMAN, background, foreground);
      append(new AnnotatedText(annotation, formFeed.toString()));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * Sets the current style for user input in this component.
   * @param annotation the current annotation holding the style
   */
  public void setCurrentStyle(TextAnnotation annotation) {
    getStyleAttributes(annotation);
  }
  
  private MutableAttributeSet getStyleAttributes(TextAnnotation annotation) {
    MutableAttributeSet attributes = getInputAttributes();
    Font font = parent.getFont(annotation);
    StyleConstants.setFontFamily(attributes, font.getFamily());
    StyleConstants.setFontSize(attributes, font.getSize());
    StyleConstants.setBold(attributes, annotation.isBold());
    StyleConstants.setItalic(attributes, annotation.isItalic());
    ColorTranslator colorTranslator = ColorTranslator.getInstance();
    Color background = colorTranslator.translate(annotation.getBackground(),
            ScreenModelSplitView.DEFAULT_BACKGROUND);
    Color foreground = colorTranslator.translate(annotation.getForeground(),
            ScreenModelSplitView.DEFAULT_FOREGROUND);
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
