/*
 * Created on 2006/02/22
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
 * PictureManager implementation.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class PictureManagerImpl implements PictureManager {

  private int release;
  private MediaCollection<? extends ZmppImage> pictures;
  private DrawingArea drawingArea;
  
  public PictureManagerImpl(int release, DrawingArea drawingArea,
                            MediaCollection<? extends ZmppImage> pictures) {
    
    this.release = release;
    this.pictures = pictures;
    this.drawingArea = drawingArea;
  }
  
  /**
   * {@inheritDoc}
   */
  public Resolution getPictureSize(final int picturenum) {
    final ZmppImage img = pictures.getResource(picturenum);
    if (img != null) {
      Resolution reso = drawingArea.getResolution();
      return img.getSize(reso.getWidth(), reso.getHeight());
    }
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  public ZmppImage getPicture(final int picturenum) {    
    return pictures.getResource(picturenum);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumPictures() {
//    
    return pictures.getNumResources();
  }

  /**
   * {@inheritDoc}
   */
  public void preload(final int[] picnumbers) {
    
    // no preloading at the moment
  }

  /**
   * {@inheritDoc}
   */
  public int getRelease() {
    
    return release;
  }

  /**
   * {@inheritDoc}
   */
  public void reset() {
    
    // no resetting supported
  }
}
