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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;

/**
 * This class implements the Image collection.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class BlorbImages extends ResourceCollection<BufferedImage> {

  /**
   * This map implements the image database.
   */
  private Map<Integer, BufferedImage> images;
  
  /**
   * Constructor.
   * 
   * @param formchunk the form chunk
   */
  public BlorbImages(FormChunk formchunk) {
    
    super(formchunk);
  }
  
  /**
   * {@inheritDoc}
   */
  protected void initDatabase() {
    
    images = new HashMap<Integer, BufferedImage>();    
  }
  
  /**
   * {@inheritDoc}
   */
  protected boolean isHandledResource(byte[] usageId) {
    
    return usageId[0] == 'P' && usageId[1] == 'i' && usageId[2] == 'c'
           && usageId[3] == 't';
  }
  
  /**
   * {@inheritDoc}
   */
  public BufferedImage getResource(int resourcenumber) {

    return images.get(resourcenumber);
  }

  /**
   * {@inheritDoc}
   */
  protected boolean storeResource(Chunk chunk, int resnum) {

    InputStream is = new MemoryAccessInputStream(chunk.getMemoryAccess(),
                                                  Chunk.CHUNK_HEADER_LENGTH);
    try {

      BufferedImage img = ImageIO.read(is);
      images.put(resnum, img);
      return true;

    } catch (IOException ex) {

      ex.printStackTrace();
    }
    return false;
  }
}
