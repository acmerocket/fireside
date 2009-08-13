/*
 * Created on 2008/04/26
 * Copyright 2005-2009 by Wei-ju Wu
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

/**
 * An annotated character for the text grid. This is supposed to save a little
 * memory as opposed to AnnotatedText.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class AnnotatedCharacter {
  private char character;
  private TextAnnotation annotation;

  public AnnotatedCharacter(TextAnnotation annotation, char c) {
    this.annotation = annotation;
    this.character = c;
  }

  public TextAnnotation getAnnotation() { return annotation; }
  public char getCharacter() { return character; }
}
