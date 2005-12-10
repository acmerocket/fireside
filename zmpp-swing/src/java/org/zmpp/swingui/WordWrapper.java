/*
 * $Id$
 * 
 * Created on 20.10.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.swingui;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A WordWrapper object lines out a given string using the specified algorithm.
 * If buffered, whole words will be wrapped to the next line if too long,
 * otherwise the strings will be written until the end of line is reachend and
 * continued on the next one.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class WordWrapper {

  private int lineLength;
  private FontMetrics fontMetrics;
  private boolean buffered;
  
  public WordWrapper(int lineLength, FontMetrics fontMetrics,
                     boolean isBuffered) {
    
    this.lineLength = lineLength;
    this.fontMetrics = fontMetrics;
    this.buffered = isBuffered;
  }
  
  public String[] wrap(int currentX, String input) {
   
    // Do this if not empty
    if (buffered) {
      
      return wrapBuffered(currentX, input);
      
    } else {
      
      return wrapUnbuffered(currentX, input);
    }
  }
  
  private String[] wrapBuffered(int currentX, String input) {
    
    List<String> result =  new ArrayList<String>();    
    StringTokenizer tok = new StringTokenizer(input, " \t\n\r", true);
    String[] words = new String[tok.countTokens()];    
    int w = 0;
    while (tok.hasMoreTokens()) {
      
      words[w++] = tok.nextToken();
    }
    
    StringBuilder lineBuffer;
    
    int currentWidth = currentX;
    int wordWidth = 0;
          
    int i = 0;
    
    while (i < words.length) {
      
      lineBuffer = new StringBuilder();
     
      for (; i < words.length; i++) {
      
        wordWidth = fontMetrics.stringWidth(words[i]);
        
        if (words[i].charAt(0) == '\n') {
          
          i++;
          break;
        }
        if (currentWidth + wordWidth <= lineLength) {
        
          lineBuffer.append(words[i]);
          currentWidth += wordWidth;
          
        } else {
        
          break;
        }
      }
      result.add(lineBuffer.toString());
      currentWidth = 0;
    }
    if (words.length > 1 && words[words.length - 1].equals("\n")) {
      
      result.add("");
    }
    return result.toArray(new String[0]);
  }
  
  private String[] wrapUnbuffered(int currentX, String input) {

    List<String> result =  new ArrayList<String>();
    StringBuilder linebuffer = new StringBuilder();
    int currentWidth = currentX;
    
    for (int i = 0; i < input.length(); i++) {
      
      char c = input.charAt(i);
      int charWidth = fontMetrics.charWidth(c);
      
      // new line
      if ((currentWidth + charWidth > lineLength) || c == '\n') {
        
        if (c == '\n') linebuffer.append('\n');
        result.add(linebuffer.toString());
        linebuffer = new StringBuilder();
        currentWidth = 0;        
      }
      if (c != '\n') {
        
        linebuffer.append(c);
        currentWidth += charWidth;

        if (i == input.length() - 1) {
          
          // last character in input, flush the buffer
          result.add(linebuffer.toString());
        }
      }      
    }
    //System.out.println("# lines: " + result.size());
    return result.toArray(new String[0]);
  }
}
