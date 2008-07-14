/*
 * $Id$
 * 
 * Created on 2008/04/06
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
package org.zmpp.blorb;

import java.io.IOException;
import java.io.InputStream;

/**
 * User interface specific factory to generate NativeImage instance from
 * a block of data.
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface NativeImageFactory {
  /**
   * Creates a NativeImage from an InputStream.
   * @param inputStream the input stream
   * @return the NativeImage
   * @throws IOException if read error occurred
   */
  NativeImage createImage(InputStream inputStream) throws IOException;
}
