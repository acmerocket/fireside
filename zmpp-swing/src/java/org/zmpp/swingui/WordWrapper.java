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

public class WordWrapper {

  private int lineLength;
  private FontMetrics fontMetrics;
  
  public WordWrapper(int lineLength, FontMetrics fontMetrics) {
    
    this.lineLength = lineLength;
    this.fontMetrics = fontMetrics;
  }
  
  public String[] wrap(int currentX, String input) {
    
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
    return result.toArray(new String[0]);
  }
}
