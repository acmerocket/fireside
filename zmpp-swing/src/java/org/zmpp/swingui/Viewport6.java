/*
 * $Id$
 * 
 * Created on 2006/02/23
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
package org.zmpp.swingui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.zmpp.io.OutputStream;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.ScreenModel6;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.TextCursor;
import org.zmpp.vm.Window6;
import org.zmpp.vm.StoryFileHeader.Attribute;

public class Viewport6 extends JViewport implements ScreenModel6, Viewport {

  private static final long serialVersionUID  = 1L;
  private static final int NUM_V6_WINDOWS = 8;
  
  private Canvas canvas;
  private BufferedImage imageBuffer;
  private FontFactory fontFactory;
  private Font fixedFont, standardFont;
  private boolean initialized;
  
  private int defaultBackground, defaultForeground;
  
  private Window6Impl[] windows;
  private int currentwindow;
  
  private Machine machine;
  private LineEditor editor;
  private OutputStream outputstream;
  
  public Viewport6(Machine machine, LineEditor editor) {

    this.machine = machine;
    this.editor = editor;
    this.fontFactory = new FontFactory();
    
    windows = new Window6Impl[NUM_V6_WINDOWS];
    outputstream = new ScreenOutputStream(machine, this);
  }
  
  public CursorWindow getCurrentWindow() {

    return windows[currentwindow];
  }
  
  public LineEditor getLineEditor() { return editor; }
  public int getDefaultBackground() { return defaultBackground; }
  public int getDefaultForeground() { return defaultForeground; }
  public Canvas getCanvas() { return canvas; }
    
  // ********************************************************************
  // ***** ScreenModel interface
  // *******************************************
  
  public void eraseWindow(int window) {
    
    if (window == - 1 || window == -2) {
      
      // We need to have special handling for the two different cases
      Color erasecolor =
        ColorTranslator.getInstance().translate(defaultBackground);
      canvas.fillRect(erasecolor, 0, 0 , getWidth(), getHeight());
      
    } else {
      
      windows[window].clear();
    }
  }
  
  public void eraseLine(int value) {
    
    windows[currentwindow].eraseLine(value);
  }
  
  public TextCursor getTextCursor() {
    
    return windows[currentwindow].getCursor();
  }

  public void setTextCursor(int line, int column, int window) {

//    System.out.printf("@set_cursor %d %d %d\n", line, column, window);
    int w = (window == ScreenModel.CURRENT_WINDOW) ? currentwindow : window;    
    windows[w].getCursor().setPosition(line, column);
  }
  
  public void splitWindow(int linesUpperWindow) {
    
    //System.out.println("@split_window: " + linesUpperWindow);
    windows[1].resize(linesUpperWindow);
    int heightWindowTop = windows[1].getHeight();
    windows[0].setVerticalBounds(heightWindowTop + 1,
                                 getHeight() - heightWindowTop);    
  }
  
  public void setWindow(int window) {
    
    //System.out.println("setWindow: ");
    currentwindow = window;
  }
  
  public void setTextStyle(int style) {
   
    //System.out.println("setTextStyle()");
    getOutputStream().flush();
    windows[currentwindow].setTextStyle(style);
  }
  
  public void setBufferMode(boolean flag) {
    
    windows[currentwindow].setBufferMode(flag);
  }
  
  public void setPaging(boolean flag) {

    // TODO
    //System.out.println("setPaging()");
  }
  
  /**
   * Reset the line counters.
   */
  public void resetPagers() {
    
    // TODO
    //System.out.println("resetPagers()");    
  }

  public synchronized void waitInitialized() {
    
    while (!isInitialized()) {
      
      try { wait(); } catch (Exception ex) { }
    }
  }
  
  public synchronized boolean isInitialized() {
    
    return initialized;
  }
  
  public synchronized void setInitialized() {
    
    this.initialized = true;
    notifyAll();
  }
  
  public OutputStream getOutputStream() {
    
    return outputstream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setForegroundColor(int colornum, int window) {

    //System.out.printf("@set_foreground %d %d\n", colornum, window);
    if (colornum > 0) {
      
      getOutputStream().flush();
      int w = (window == CURRENT_WINDOW) ? currentwindow : window;      
      windows[w].setForeground(colornum);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setBackgroundColor(int colornum, int window) {
    
    //System.out.printf("@set_background %d %d\n", colornum, window);
    if (colornum > 0) {
      
      getOutputStream().flush();
      int w = (window == CURRENT_WINDOW) ? currentwindow : window;      
      windows[w].setBackground(colornum);
    }
  }

  public void redraw() {
    
    repaintInUiThread();
  }
  
  public int setFont(int fontnum) {
   
    getOutputStream().flush();
    return windows[currentwindow].setFont(fontnum);
  }

  public synchronized void displayCursor(boolean showCaret) {
    
    // TODO
    //System.out.println("displayCursor()");
  }
  
  public void reset() {
    
    setScreenProperties();
  }
  
  // ********************************************************************
  // ***** ScreenModel V6 interface
  // *******************************************
  
  public void setMouseWindow(int window) {

    // do nothing at the moment
  }
  
  public Window6 getSelectedWindow() {

    return windows[currentwindow];
  }
  
  public Window6 getWindow(int window) {

    return windows[window];
  }
  
  // ********************************************************************
  // ***** Component functions  
  // *******************************************
  
  public void paint(Graphics g) {

    if (imageBuffer == null) {
      
      imageBuffer = new BufferedImage(getWidth(), getHeight(),
          BufferedImage.TYPE_INT_RGB);

      // Default colors
      defaultBackground = ColorTranslator.COLOR_WHITE;
      defaultForeground = ColorTranslator.COLOR_BLACK;
            
      canvas = new CanvasImpl(imageBuffer, this);
      for (int i = 0; i < NUM_V6_WINDOWS; i++) {
      
        windows[i] = new Window6Impl(this, fontFactory, i);
      }
      
      // Set initial window sizes
      windows[0].setSize(getWidth(), getHeight());
      windows[1].setSize(getWidth(), 0);
      
      Graphics g_img = imageBuffer.getGraphics();
      Color bgcolor =
        ColorTranslator.getInstance().translate(defaultBackground);
      g_img.setColor(bgcolor);
      g_img.fillRect(0, 0, getWidth(), getHeight());
      
      setScreenProperties();
      
      // Set the final default settings to each window
      for (int i = 0; i < NUM_V6_WINDOWS; i++) {
        
        windows[i].setFont(1);
        windows[i].setBackground(defaultBackground);
        windows[i].setForeground(defaultForeground);
      }
      
      setInitialized();
    }

    g.drawImage(imageBuffer, 0, 0, this);    
  }

  // ********************************************************************
  // ***** Private functions
  // *******************************************
  
  private void setScreenProperties() {
    
    StoryFileHeader fileheader = machine.getGameData().getStoryFileHeader();
    fileheader.setEnabled(Attribute.SUPPORTS_BOLD, true);
    fileheader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, true);
    fileheader.setEnabled(Attribute.SUPPORTS_ITALIC, true);
    
    fileheader.setEnabled(Attribute.SUPPORTS_COLOURS, true);
    fileheader.setDefaultBackgroundColor(ColorTranslator.COLOR_WHITE);
    fileheader.setDefaultForegroundColor(ColorTranslator.COLOR_BLACK);
    
    determineStandardFont();
    fileheader.setFontWidth(canvas.getCharWidth(fixedFont, '0'));
    fileheader.setFontHeight(canvas.getFontHeight(fixedFont));
    updateDimensionsInHeader();
  }
  
  private void determineStandardFont() {
          
    standardFont = new Font("Monospaced", Font.ROMAN_BASELINE, 12);
    fixedFont = standardFont;
    fontFactory.initialize(standardFont, fixedFont);    
  }
  
  private void updateDimensionsInHeader() {
    
    StoryFileHeader fileheader = machine.getGameData().getStoryFileHeader();
    int numcols = imageBuffer.getWidth() / canvas.getCharWidth(fixedFont, '0');
    int numlines = imageBuffer.getHeight() / canvas.getFontHeight(fixedFont);
    
    fileheader.setScreenWidth(numcols);
    fileheader.setScreenHeight(numlines);
    fileheader.setScreenWidthUnits(imageBuffer.getWidth());
    fileheader.setScreenHeightUnits(imageBuffer.getHeight());
  }
    
  private void repaintInUiThread() {
    
    try {
      
      SwingUtilities.invokeAndWait(new Runnable() {
        
        public void run() {
          
          // replace the expensive repaint() call with a fast copying of
          // the double buffer
          if (imageBuffer != null) {
            
            getGraphics().drawImage(imageBuffer, 0, 0, Viewport6.this);
          }
        }
      });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
  }  
}
