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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.zmpp.media.Resources;
import org.zmpp.media.StoryMetadata;
import org.zmpp.swingui.view.DisplaySettings;
import org.zmpp.swingui.view.GameLifeCycleListener;
import org.zmpp.windowing.ScreenModel;

/**
 * A new version of the ZmppFrame class.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZmppFrame extends JFrame implements GameLifeCycleListener {
  private static final String STD_FONT_NAME = "American Typewriter";
  private static final int STD_FONT_SIZE = 12;
  private static final String FIXED_FONT_NAME = "Monaco";
  private static final int FIXED_FONT_SIZE = 12;
  private static final int DEFAULT_FOREGROUND = ScreenModel.COLOR_BLACK;
  private static final int DEFAULT_BACKGROUND = ScreenModel.COLOR_WHITE;
  //private static final Font STD_FONT = new Font("Baskerville", Font.PLAIN, 16);
  //private static final DisplaySettings displaySettings = new DisplaySettings(STD_FONT, FIXED_FONT,
  //    DEFAULT_BACKGROUND, DEFAULT_FOREGROUND, true);
  
  private JMenuBar menubar = new JMenuBar();
  private JMenu helpMenu;
  private JMenuItem aboutGameItem;
  private ScreenModelView screenModelView;
  private DisplaySettings displaySettings;
  private Preferences preferences;

  /**
   * Constructor.
   */
  public ZmppFrame() {
    super(Main.APP_NAME);
    preferences = Preferences.userNodeForPackage(ZmppFrame.class);
    this.displaySettings = createDisplaySettings(preferences);
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
    screenModelView.addGameLoadedListener(this);
    getContentPane().add(screenModelView);
    setJMenuBar(menubar);
    if (isMacOsX()) {
      setupMacOsAppMenu();
    } else {
      setupNonMacOsMenuBar();
    }
    addAboutGameMenuItem();
  }
  
  public void gameInitialized() {
    StoryMetadata storyinfo = getStoryInfo();
    if (storyinfo != null) {
      setTitle(getMessage("app.name") + " - " + storyinfo.getTitle()
          + " (" + storyinfo.getAuthor() + ")");
    }
    aboutGameItem.setEnabled(storyinfo != null);
  }
  
  private void addAboutGameMenuItem() {
    aboutGameItem = new JMenuItem(getMessage("menu.help.aboutgame.name"));
    helpMenu.add(aboutGameItem);
    aboutGameItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        aboutGame();
      }
    });
    aboutGameItem.setEnabled(false);
  }
  
  private void setupNonMacOsMenuBar() {
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
      helpMenu = new JMenu(getMessage("menu.help.name"));
      menubar.add(helpMenu);
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
  }

  private StoryMetadata getStoryInfo() {
    Resources resources = screenModelView.getMachine().getResources();
    if (resources != null && resources.getMetadata() != null) {     
      return resources.getMetadata().getStoryInfo();
    }
    return null;
  }

  public void about() {
    JOptionPane.showMessageDialog(this,
        getMessage("app.name") + getMessage("dialog.about.msg"),
        getMessage("dialog.about.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }
  
  /**
   * Displays the about dialog.
   */
  public void aboutGame() {
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


  private DisplaySettings createDisplaySettings(Preferences preferences) {
    String stdFontName = preferences.get("stdfontname", STD_FONT_NAME);
    int stdFontSize = preferences.getInt("stdfontsize", STD_FONT_SIZE);
    String fixedFontName = preferences.get("fixedfontname",
        FIXED_FONT_NAME);
    int fixedFontSize = preferences.getInt("fixedfontsize",
        FIXED_FONT_SIZE);
    int defaultforeground = preferences.getInt("defaultforeground",
        DEFAULT_FOREGROUND);
    int defaultbackground = preferences.getInt("defaultbackground",
        DEFAULT_BACKGROUND);
    boolean antialias = preferences.getBoolean("antialias", true);
    
    return new DisplaySettings(new Font(stdFontName, Font.PLAIN, stdFontSize),
      new Font(fixedFontName, Font.PLAIN, fixedFontSize), defaultbackground,
                               defaultforeground, antialias);    
  }
}
