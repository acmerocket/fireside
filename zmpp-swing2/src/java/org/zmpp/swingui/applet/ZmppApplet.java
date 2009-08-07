/*
 * Created on 2008/07/14
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
package org.zmpp.swingui.applet;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.zmpp.blorb.NativeImage;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.io.IOSystem;
import org.zmpp.swingui.view.DisplaySettings;
import org.zmpp.swingui.view.FileSaveGameDataStore;
import org.zmpp.swingui.view.MemorySaveGameDataStore;
import org.zmpp.swingui.view.ScreenModelView;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.windowing.ScreenModel;

/**
 * This is the new applet class version using the new Swing 2 screen model.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZmppApplet extends JApplet implements IOSystem {
  /**
   * The serial version.
   */
  private static final long serialVersionUID = 1L;
  private static final int DEFAULT_BACKGROUND = ScreenModel.COLOR_WHITE;
  private static final int DEFAULT_FOREGROUND = ScreenModel.COLOR_BLACK;
  private static final int DEFAULT_FIXED_FONT_SIZE  = 12;
  private static final int DEFAULT_STD_FONT_SIZE    = 12;
  private static final boolean DEFAULT_ANTIALIAS = true;
  private static final String DEFAULT_STD_FONT   = "Times";
  private static final String DEFAULT_FIXED_FONT = "Courier New";
  
  /**
   * The color map maps parameters to color ids.
   */
  private static final Map<String, Integer> colormap =
    new HashMap<String, Integer>();
  
  static {
    colormap.put("black",   2);
    colormap.put("red",     3);
    colormap.put("green",   4);
    colormap.put("yellow",  5);
    colormap.put("blue",    6);
    colormap.put("magenta", 7);
    colormap.put("cyan",    8);
    colormap.put("white",   9);
    colormap.put("gray",   10);
  }

  private DisplaySettings settings;
  private boolean savetofile;
  private ScreenModelView screenModelView;
  private MachineInitStruct initStruct;

  private Font createStdFont(String name, int size) {
    String fontName = name == null ? DEFAULT_STD_FONT : name;
    return new Font(fontName, Font.PLAIN, size);
  }
  
  private Font createFixedFont(String name, int size) {
    String fontName = name == null ? DEFAULT_FIXED_FONT : name;
    return new Font(fontName, Font.PLAIN, size);
  }
 
  private void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /* {@inheritDoc} */
  @Override
  public void init() {
    setSystemLookAndFeel();
    // unsigned applets do not go well with this option
    //setLogLevels();
    requestFocusInWindow();
    String story = getParameter("story-file");
    String blorb = getParameter("blorb-file");
    String saveto = getParameter("save-to");
    String fixedFontName = getParameter("fixed-font-name");
    String fixedFontSize = getParameter("fixed-font-size");
    String stdFontName = getParameter("std-font-name");
    String stdFontSize = getParameter("std-font-size");
    String defaultBgStr = getParameter("default-background");
    String defaultFgStr = getParameter("default-foreground");
    String antialiasparam = getParameter("antialias");
    
    savetofile = "file".equalsIgnoreCase(saveto);

    int sizeFixedFont = parseInt(fixedFontSize, DEFAULT_FIXED_FONT_SIZE);
    int sizeStdFont = parseInt(stdFontSize, DEFAULT_STD_FONT_SIZE);
    int defaultBackground = parseColor(defaultBgStr, DEFAULT_BACKGROUND);
    int defaultForeground = parseColor(defaultFgStr, DEFAULT_FOREGROUND);
    boolean antialias = parseBoolean(antialiasparam, DEFAULT_ANTIALIAS);
    
    settings = new DisplaySettings(createStdFont(stdFontName, sizeStdFont),
        createFixedFont(fixedFontName, sizeFixedFont),
        defaultBackground, defaultForeground, antialias);
    screenModelView = new ScreenModelView(settings);
    getContentPane().add(screenModelView, BorderLayout.CENTER);
    
    try {
      initStruct = new MachineInitStruct();
      initStruct.blorbURL = (blorb != null) ?
        new URL(getDocumentBase(), blorb) : null;
      initStruct.storyURL = (story != null) ?
        new URL(getDocumentBase(), story) : null;
      // this is a simple dummy image factory since we do not handle
      // pictures yet
      initStruct.nativeImageFactory = new NativeImageFactory() {
        public NativeImage createImage(InputStream inputStream)
                throws IOException {
          return new NativeImage() {
            public int getWidth() { return 0; }
            public int getHeight() { return 0; }        
          };
        }
      };
      initStruct.saveGameDataStore = savetofile ?
        new FileSaveGameDataStore(this) : new MemorySaveGameDataStore();
      initStruct.ioSystem = this;
    } catch (Exception ex) {
      ex.printStackTrace();      
    }
  }

  /**
   * Parses the specified string into an integer and returns it, if str
   * is null or not an integer, the fallback value is returned.
   * 
   * @param str the string to parse
   * @param fallback the fallback value
   * @return the integer result
   */
  private int parseInt(String str, int fallback) {
    int result = fallback;
    if (str != null) {
      try {
        result = Integer.parseInt(str);
      } catch (NumberFormatException ignore) { }
    }
    return result;
  }

  /**
   * Retrieves the color id for the specified string.
   * @param str the color string
   * @param fallback the fallback value
   * @return the color id
   */
  private int parseColor(String str, int fallback) {
    return colormap.get(str) == null ? fallback : colormap.get(str);
  }
  
  /**
   * Retrievs the boolean value for the specified string. Values can
   * be true|false or on|off.
   * @param str the string
   * @param fallback the fallback value
   * @return the boolean value
   */
  private boolean parseBoolean(String str, boolean fallback) {
    if ("false".equals(str) || "off".equals(str)) return false;
    if ("true".equals(str) || "on".equals(str)) return true;
    return fallback;
  }
  
  /** {@inheritDoc} */
  @Override
  public void start() {
    try {
      screenModelView.startGame(initStruct);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // IOSystem does nothing in an applet
  public Writer getTranscriptWriter() { return null; }
  public Reader getInputStreamReader() { return null; }
}
