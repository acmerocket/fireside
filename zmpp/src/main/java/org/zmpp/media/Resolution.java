/*
 * Created on 2008/07/28
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
package org.zmpp.media;

/**
 * An object similar to Dimension, but can be used in environments outside of
 * J2SE.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class Resolution {

  private int width;
  private int height;

  /**
   * Constructor.
   * @param width the width
   * @param height the height
   */
  public Resolution(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Returns the width attribute.
   * @return the width attribute
   */
  public int getWidth() { return width; }

  /**
   * Returns the height attribute.
   * @return the height attribute
   */
  public int getHeight() { return height; }

  /** {@inheritDoc} */
  @Override
  public String toString() { return width + "x" + height; }
}
