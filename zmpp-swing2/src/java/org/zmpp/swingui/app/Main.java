/*
 * $Id$
 * 
 * Created on 2008/04/23
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
package org.zmpp.swingui.app;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.zmpp.blorb.NativeImage;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.swingui.view.DisplaySettings;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.windowing.ScreenModel;

/**
 * New application class using the Swing 2 model.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class Main {

  /**
   * Localized message bundle.
   */
  private static PropertyResourceBundle MESSAGE_BUNDLE =
		(PropertyResourceBundle) PropertyResourceBundle.getBundle("zmpp_messages");
  
  /**
   * Debug flag.
   */
  public static final boolean DEBUG = true;
  private static final Font STD_FONT = new Font("American Typewriter", Font.PLAIN, 12);
  private static final Font FIXED_FONT = new Font("Monaco", Font.PLAIN, 12);
  private static final int DEFAULT_FOREGROUND = ScreenModel.COLOR_WHITE;
  private static final int DEFAULT_BACKGROUND = ScreenModel.COLOR_BLUE;
  //private static final Font STD_FONT = new Font("Baskerville", Font.PLAIN, 16);
  private static final DisplaySettings displaySettings = new DisplaySettings(STD_FONT, FIXED_FONT,
      DEFAULT_BACKGROUND, DEFAULT_FOREGROUND, true);
  
  /**
   * Application name.
   */
  public static final String APP_NAME = getMessage("app.name");

  /**
   * Global function to return the message string.
   * @param property the property name
   * @return the message
   */
  public static String getMessage(String property) {
    return MESSAGE_BUNDLE.getString(property);
  }
  
  public static void main(String[] args) {
    setMacOsXProperties();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      LogManager.getLogManager().readConfiguration();
      Logger.getLogger("org.zmpp").setLevel(Level.INFO);
      Logger.getLogger("org.zmpp.screen").setLevel(Level.SEVERE);
      Logger.getLogger("org.zmpp.ui").setLevel(Level.INFO);
      Logger.getLogger("org.zmpp.control").setLevel(Level.SEVERE);
      //ExecutionControl.DEBUG = true;
      //ExecutionControl.DEBUG_INTERRUPT = true;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    try {
    	SwingUtilities.invokeAndWait(new Runnable() {
    		public void run() {
    			JFileChooser fileChooser =
    					new JFileChooser(System.getProperty("user.dir"));
    			fileChooser.setDialogTitle(getMessage("dialog.open.msg"));
    			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {        
    				final File storyfile = fileChooser.getSelectedFile();
    		    SwingUtilities.invokeLater(new Runnable() {  
    		    	public void run() {
    		        runStoryFile(storyfile);
    		    	}
    		    });
    			}
    		}
    	});
    } catch (Exception ignore) {}
  }
  
  private static void runStoryFile(final File storyFile) {
    ZmppFrame frame = new ZmppFrame(displaySettings);
    frame.setVisible(true);
    try {
      MachineInitStruct initStruct = new MachineInitStruct();
      if (storyFile.getName().endsWith("zblorb")) {
        initStruct.blorbFile = storyFile;
      } else {
        initStruct.storyFile = storyFile;
      }
      // just for debugging
      initStruct.nativeImageFactory = new NativeImageFactory() {
        public NativeImage createImage(InputStream inputStream) throws IOException {
          return new NativeImage() {
            public int getWidth() { return 0; }
            public int getHeight() { return 0; }        
          };
        }
      };
      frame.getScreenModelView().startGame(initStruct);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * Starts the specified game.
   * @param storyFile the story file
   * @throws java.io.IOException if I/O error occurred
   * @throws org.zmpp.vm.InvalidStoryException if story is in invalid format
   */
  /*
  public void startGame(File storyFile)
      throws IOException, InvalidStoryException {
    startGame(initStruct);
  }
  */

  public static boolean isMacOsX() {
  	return System.getProperty("mrj.version") != null;
  }
  
  private static void setMacOsXProperties() {
    if (isMacOsX()) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode",
          "false");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          "ZMPP");
    }
  }
}
