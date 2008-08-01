/*
 * $Id$
 * 
 * Created on 2008/07/14
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
package org.zmpp.swingui.applet;

import org.zmpp.swingui.view.DisplaySettings;
import org.zmpp.swingui.view.ScreenModelView;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import org.zmpp.blorb.NativeImage;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.windowing.ScreenModel;

/**
 * This is the new applet class version using the new Swing 2 screen model.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZmppApplet extends JApplet {
  /**
   * The serial version.
   */
  private static final long serialVersionUID = 1L;
  
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

  /** Setting the log levels directly */
  private void setLogLevels() {    
    Logger.getLogger("org.zmpp").setLevel(Level.SEVERE);
    Logger.getLogger("org.zmpp.screen").setLevel(Level.SEVERE);
    Logger.getLogger("org.zmpp.ui").setLevel(Level.INFO);
    Logger.getLogger("org.zmpp.control").setLevel(Level.SEVERE);
  }
  
  private Font createStdFont(int size) {
    return new Font("Times", Font.PLAIN, size);
  }
  
  private Font createFixedFont(int size) {
    return new Font("Monospaced", Font.PLAIN, size);
  }

  /* {@inheritDoc} */
  @Override
  public void init() {
    setLogLevels();
    requestFocusInWindow();
    String story = getParameter("storyfile");
    String blorb = getParameter("blorbfile");
    String saveto = getParameter("saveto");
    String fixedfontsize = getParameter("fixedfontsize");
    String stdfontsize = getParameter("stdfontsize");
    String defbg = getParameter("defaultbg");
    String deffg = getParameter("defaultfg");
    String antialiasparam = getParameter("antialias");
    
    int sizeStdFont = 12;
    int sizeFixedFont = 12;
    int defaultBackground = ScreenModel.COLOR_WHITE;
    int defaultForeground = ScreenModel.COLOR_BLACK;
    boolean antialias = true;
    
    savetofile = "file".equalsIgnoreCase(saveto);

    sizeFixedFont = parseInt(fixedfontsize, sizeFixedFont);
    sizeStdFont = parseInt(stdfontsize, sizeStdFont);
    defaultBackground = parseColor(defbg, defaultBackground);
    defaultForeground = parseColor(deffg, defaultForeground);
    antialias = parseBoolean(antialiasparam, antialias);
    
    settings = new DisplaySettings(createStdFont(sizeStdFont),
        createFixedFont(sizeFixedFont),
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
}
