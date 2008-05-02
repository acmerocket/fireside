/*
 * $Id$
 * 
 * Created on 2008/05/01
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
package org.zmpp.swingui2;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * A super class capturing the common features of virtual views. It tries to
 * emulate Swing/AWT in terms of validation and layout method naming.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class VirtualView {

  private static final boolean DEBUG = true;
  private JComponent hostingComponent;
  private boolean valid;
  private FontSelector fontSelector;
  
  // Bounds relative to hosting component
  private int left, top, width, height;

  // Layout information
  private int marginLeft = 8, marginRight = 8, marginBottom = 5,
              marginTop = 5;

  /**
   * Constructor.
   * @param hostingComponent the hosting parent component
   * @param fontSelector the FontSelector object
   */
  public VirtualView(JComponent hostingComponent, FontSelector fontSelector) {
    this.hostingComponent = hostingComponent;
    this.fontSelector = fontSelector;
  }
  
  /**
   * Returns the hosting Swing component for this view.
   * @return the hosting component
   */
  protected JComponent getHostingComponent() { return hostingComponent; }
  
  /**
   * Returns the view's FontSelector.
   * @return the FontSelector
   */
  protected FontSelector getFontSelector() { return fontSelector; }
  
  /**
   * Returns the validation status.
   * @return the validation status
   */
  protected boolean isValid() { return valid; }
  
  /**
   * Invalidates the view and marks it for relayouting its contents.
   */
  protected void invalidate() { valid = false; }
  
  /**
   * Marks the view as valid.
   */
  protected void validate() { valid = true; }
  
  /**
   * Tells the view to layout its contents.
   * @param g2d the Graphics2D object
   */
  protected void doLayout(Graphics2D g2d) {
    validate();
  }

  /**
   * Sets the bounds of this window.
   * @param left x-start of the view within the component
   * @param top y-start of the view within the component
   * @param width width of the view
   * @param height height of the view
   */
  public void setBounds(int left, int top, int width, int height) {
    this.left = left;
    this.top = top;
    this.width = width;
    this.height = height;
  }

  /**
   * Paint method. This will be typically called within the paintComponent()
   * method of the hosting component. The base implementation simply sets the
   * clip to the usable area.
   * @param g2d the Graphics2D object
   */
  public void paint(Graphics2D g2d) {
    drawDebugBorderLines(g2d);
    // Restrict drawing operations to the usable area
    g2d.setClip(getUsableLeft(), getUsableTop(), getUsableWidth(),
            getUsableHeight());
  }
  
  /**
   * Draws the border lines to visually indicate the area this view uses.
   * Can be deactivated by setting DEBUG to false.
   * @param g2d the Graphics2D object
   */
  private void drawDebugBorderLines(Graphics2D g2d) {
    if (DEBUG) {
      g2d.setClip(left, top, width + 1, height + 1);
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.drawRect(left, top, width, height);
      g2d.setColor(Color.BLACK);
      g2d.drawRect(getUsableLeft() - 1, getUsableTop() - 1,
                   getUsableWidth() + 2, getUsableHeight() + 2);
    }
  }

  protected void clearArea(Graphics2D g2d) {
    g2d.setColor(getHostingComponent().getBackground());
    g2d.fillRect(getUsableLeft(), getUsableTop(), getUsableWidth(), getUsableHeight());
  }

  // ***********************************************************************
  // **** Helper functionality to determine the usable area
  // ***********************************************************************

  protected int getUsableLeft() { return left + marginLeft; }
  protected int getUsableTop() { return top + marginTop; }
  protected int getUsableWidth() { return width - marginRight - marginLeft; }
  protected int getUsableHeight() { return height - marginBottom - marginTop; }
}
