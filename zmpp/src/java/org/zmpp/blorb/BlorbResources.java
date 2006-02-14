/*
 * $Id$
 * 
 * Created on 2006/02/06
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
package org.zmpp.blorb;

import java.awt.image.BufferedImage;

import org.zmpp.iff.FormChunk;
import org.zmpp.media.MediaCollection;
import org.zmpp.media.Resources;
import org.zmpp.media.SoundEffect;

/**
 * This class encapsulates a Blorb file and offers access to the sound
 * and graphics media collections.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class BlorbResources implements Resources {

  /**
   * The file's images.
   */
  private MediaCollection<BufferedImage> images;
  
  /**
   * The file's sounds.
   */
  private MediaCollection<SoundEffect> sounds;
  
  /**
   * Constructor.
   * 
   * @param formchunk a form chunk in Blorb format
   */
  public BlorbResources(FormChunk formchunk) {
  
    images = new BlorbImages(formchunk);
    sounds = new BlorbSounds(formchunk);
  }
  
  /**
   * {@inheritDoc}
   */
  public MediaCollection<BufferedImage> getImages() {
    
    return images;
  }
  
  /**
   * {@inheritDoc}
   */
  public MediaCollection<SoundEffect> getSounds() {
    
    return sounds;
  }
}