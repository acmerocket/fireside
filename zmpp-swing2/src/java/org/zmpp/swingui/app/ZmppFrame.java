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
package org.zmpp.swingui.app;

import org.zmpp.swingui.view.ScreenModelView;
import apple.dts.osxadapter.OSXAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.zmpp.swingui.view.DisplaySettings;

/**
 * A new version of the ZmppFrame class.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZmppFrame extends JFrame {
  
  private ScreenModelView screenModelView;
  private DisplaySettings displaySettings;
  private Preferences preferences;

  /**
   * Constructor.
   */
  public ZmppFrame(DisplaySettings displaySettings) {
    super(Main.APP_NAME);
    this.displaySettings = displaySettings;
    preferences = Preferences.userNodeForPackage(ZmppFrame.class);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setupUI();
    pack();
  }
  
  /**
   * Determine if we are running under Mac OS X. 
   * @return true if this is a Mac
   */
  private static boolean isMacOsX() { return Main.isMacOsX(); }
  
  /**
   * Returns a localized message from the resource bundle.
   * @param property the message key
   * @return the message content
   */
  private static String getMessage(String property) {
    return Main.getMessage(property);
  }
  
  /**
   * Returns the Z-machine view
   * @return the Z-machine view
   */
  public ScreenModelView getScreenModelView() { return screenModelView; }

  /**
   * Sets up the user interface
   */
  private void setupUI() {
    screenModelView = new ScreenModelView(displaySettings);
    getContentPane().add(screenModelView);
    if (isMacOsX()) {
      setupMacOsAppMenu();
    } else {
      setupNonMacOsMenuBar();
    }
  }
  
  private void setupNonMacOsMenuBar() {
    JMenuBar menubar = new JMenuBar();
    setJMenuBar(menubar);
    JMenu helpMenu = null;
    JMenu fileMenu = new JMenu(getMessage("menu.file.name"));
    fileMenu.setMnemonic(getMessage("menu.file.mnemonic").charAt(0));
    menubar.add(fileMenu);

    // Quit is already in the application menu
    JMenuItem exitItem = new JMenuItem(getMessage("menu.file.quit.name"));
    exitItem.setMnemonic(getMessage("menu.file.quit.mnemonic").charAt(0));
    fileMenu.add(exitItem);
    exitItem.addActionListener(new ActionListener() {
      /** {@inheritDoc} */
      public void actionPerformed(ActionEvent e) { quit(); }
    });
    JMenu editMenu = new JMenu(getMessage("menu.edit.name"));
    menubar.add(editMenu);
    editMenu.setMnemonic(getMessage("menu.edit.mnemonic").charAt(0));
    JMenuItem preferencesItem =
            new JMenuItem(getMessage("menu.edit.prefs.name"));
    preferencesItem.setMnemonic(getMessage("menu.edit.prefs.mnemonic").charAt(0));
    editMenu.add(preferencesItem);
    preferencesItem.addActionListener(new ActionListener() {
      /** {@inheritDoc} */
      public void actionPerformed(ActionEvent e) { editPreferences(); }
    });

    helpMenu = new JMenu(getMessage("menu.help.name"));
    menubar.add(helpMenu);
    helpMenu.setMnemonic(getMessage("menu.help.mnemonic").charAt(0));

    JMenuItem aboutItem = new JMenuItem(getMessage("menu.help.about.name"));
    aboutItem.setMnemonic(getMessage("menu.help.about.mnemonic").charAt(0));
    helpMenu.add(aboutItem);
    aboutItem.addActionListener(new ActionListener() {
      /** {@inheritDoc} */
      public void actionPerformed(ActionEvent e) { about(); }
    });
  }
  
  /**
   * Sets up the Mac OS X application menu. This makes the application look
   * more like a native Mac application.
   */
  private void setupMacOsAppMenu() {
    try {
      OSXAdapter.setAboutHandler(this,
              ZmppFrame.class.getDeclaredMethod("about"));
      OSXAdapter.setQuitHandler(this,
              ZmppFrame.class.getDeclaredMethod("quit"));
      OSXAdapter.setPreferencesHandler(this,
              ZmppFrame.class.getDeclaredMethod("editPreferences"));
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
  } 

  /**
   * Displays the about dialog.
   */
  public void about() {
    GameInfoDialog dialog = new GameInfoDialog(this,
      screenModelView.getMachine().getResources());
    dialog.setVisible(true);
  }
  
  /**
   * Quits the application.
   */
  public void quit() { System.exit(0); }

  /**
   * Open the preferences dialog.
   */
  public void editPreferences() {
    PreferencesDialog dialog = new PreferencesDialog(this, preferences,
                                                     displaySettings);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }
}
