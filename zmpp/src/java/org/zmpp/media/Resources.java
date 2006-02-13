/*
 * $Id$
 * 
 * Created on 2006/02/13
 * Copyright 2005-2006 by Wei-ju Wu
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
package org.zmpp.media;

import java.awt.image.BufferedImage;

/**
 * This interface defines access to the Z-machine's media resources.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface Resources {

  /**
   * Returns the images of this file.
   * 
   * @return the images
   */
  MediaCollection<BufferedImage> getImages();

  /**
   * Returns the sounds of this file.
   * 
   * @return the sounds
   */
  MediaCollection<SoundEffect> getSounds();
}
