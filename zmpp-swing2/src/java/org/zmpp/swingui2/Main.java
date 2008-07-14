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
package org.zmpp.swingui2;

import java.io.File;
import java.util.PropertyResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

  private static PropertyResourceBundle MESSAGE_BUNDLE =
		(PropertyResourceBundle) PropertyResourceBundle.getBundle("zmpp_messages");
  public static final boolean DEBUG = true;
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
  
  private static void runStoryFile(final File storyfile) {
    JFrame frame = new JFrame(APP_NAME);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ScreenModelView view = createView();
    frame.getContentPane().add(view);
    frame.pack();
    frame.setVisible(true);
    try {
      view.startGame(storyfile);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private static ScreenModelView createView() {
    return new ScreenModelView();
  }

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
