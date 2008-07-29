/*
 * $Id$
 * 
 * Created on 2008/04/23
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
package org.zmpp.windowing;

import java.util.ArrayList;
import java.util.List;

/**
 * BufferedTextWindow is part of the BufferedScreenModel, it represents a
 * buffer for continuously flowing text.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class BufferedTextWindow {

  private List<AnnotatedText> textBuffer = new ArrayList<AnnotatedText>();
  private TextAnnotation currentAnnotation = new TextAnnotation(
          TextAnnotation.FONT_NORMAL, TextAnnotation.TEXTSTYLE_ROMAN);
  private StringBuilder currentRun = new StringBuilder();
  private boolean isBuffered = true;
  
  public TextAnnotation getCurrentAnnotation() { return currentAnnotation; }
  public boolean isBuffered() { return isBuffered; }
  public void setBuffered(boolean flag) { isBuffered = flag; }
  public char setCurrentFont(char font) {
    char previousFont = currentAnnotation.getFont();
    startNewAnnotatedRun(currentAnnotation.deriveFont(font));
    return previousFont;
  }
  public void setCurrentTextStyle(int style) {
    startNewAnnotatedRun(currentAnnotation.deriveStyle(style));
  }
  
  public void setBackground(int background) {
    startNewAnnotatedRun(currentAnnotation.deriveBackground(background));
  }
  
  public void setForeground(int foreground) {
    startNewAnnotatedRun(currentAnnotation.deriveForeground(foreground));
  }
  
  public int getBackground() { return currentAnnotation.getBackground(); }
  public int getForeground() { return currentAnnotation.getForeground(); }

  private void startNewAnnotatedRun(TextAnnotation annotation) {
    textBuffer.add(new AnnotatedText(currentAnnotation, currentRun.toString()));
    currentRun = new StringBuilder();
    currentAnnotation = annotation;    
  }
  
  public void printChar(char zchar) {
    currentRun.append(zchar);
    if (!isBuffered) flush();
  }
  
  public List<AnnotatedText> getBuffer() {
    flush();
    List<AnnotatedText> result = textBuffer;
    textBuffer = new ArrayList<AnnotatedText>();
    return result;
  }
  
  /**
   * Flushes pending output into 
   */
  private void flush() {
    if (currentRun.length() > 0) {
      textBuffer.add(new AnnotatedText(currentAnnotation,
                                       currentRun.toString()));
      currentRun = new StringBuilder();
    }
  }
  
  /**
   * Override toString().
   * @return the string representation
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (AnnotatedText str : textBuffer) {
      String line = str.getText().replace('\r', '\n');
      builder.append(line);
    }
    return builder.toString();
  }
}
