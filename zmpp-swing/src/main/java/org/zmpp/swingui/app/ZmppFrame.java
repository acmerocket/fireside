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
package org.zmpp.swingui.app;

import org.zmpp.swingui.view.ScreenModelView;
import apple.dts.osxadapter.OSXAdapter;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.zmpp.io.IOSystem;
import org.zmpp.media.Resources;
import org.zmpp.media.StoryMetadata;
import org.zmpp.swingui.app.Main.AwtImageFactory;
import org.zmpp.swingui.view.DisplaySettings;
import org.zmpp.swingui.view.FileSaveGameDataStore;
import org.zmpp.swingui.view.GameLifeCycleListener;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.windowing.ScreenModel;

/**
 * A new version of the ZmppFrame class.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZmppFrame extends JFrame
  implements GameLifeCycleListener, IOSystem {
  private static final long serialVersionUID = -776528959779547763L;
  //private static final Font STD_FONT =new Font("Baskerville", Font.PLAIN, 16);
  //private static final String STD_FONT_NAME = "American Typewriter";
  //private static final String FIXED_FONT_NAME = "Monaco";
  public static final String STD_FONT_NAME = "Times";
  public static final String FIXED_FONT_NAME = "Courier New";
  public static final int STD_FONT_SIZE = 14;
  public static final int FIXED_FONT_SIZE = 14;
  public static final int DEFAULT_FOREGROUND = ScreenModel.COLOR_BLACK;
  public static final int DEFAULT_BACKGROUND = ScreenModel.COLOR_WHITE;

  private JMenuBar menubar = new JMenuBar();
  private JMenu fileMenu;
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
    this.displaySettings = createDisplaySettings();
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
    createFileMenu();
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
      setTitle(getMessage("app.name") + " - " + storyinfo.getTitle() +
               " (" + storyinfo.getAuthor() + ")");
    }
    aboutGameItem.setEnabled(storyinfo != null);
  }

  // ***********************************************************************
  // **** Menu initialization
  // **********************************

  private void createFileMenu() {
    fileMenu = new JMenu(getMessage("menu.file.name"));
    fileMenu.setMnemonic(getMessage("menu.file.mnemonic").charAt(0));
    menubar.add(fileMenu);
    JMenuItem openItem = new JMenuItem(getMessage("menu.file.open.name"));
    fileMenu.add(openItem);
    openItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openStoryFile(ZmppFrame.this, null);
      }
    });
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
    preferencesItem.setMnemonic(getMessage("menu.edit.prefs.mnemonic")
        .charAt(0));
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

  // ***********************************************************************
  // **** IOSys interface
  // **********************************
  public Writer getTranscriptWriter() {
    File currentdir = new File(System.getProperty("user.dir"));
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle(getMessage("dialog.settranscript.title"));
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        return new FileWriter(fileChooser.getSelectedFile());
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return null;
  }

  public Reader getInputStreamReader() {
    File currentdir = new File(System.getProperty("user.dir"));
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle(getMessage("dialog.setinput.title"));
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        return new FileReader(fileChooser.getSelectedFile());
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return null;
  }
  // ***********************************************************************
  // **** Menu actions
  // **********************************
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

  private DisplaySettings createDisplaySettings() {
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

  public static void openStoryUrl(URL url) {
    runStoryUrl(url);
  }
  public static void openStoryFile() { openStoryFile(null); }
  public static void openStoryFile(File storyfile) {
      openStoryFile(null, storyfile);
  }
  private static void openStoryFile(ZmppFrame frame, final File file) {
    if (frame != null) { frame.dispose(); }
    try {
      runInEventDispatchThread(new Runnable() {
        public void run() {
          if (file == null || !file.exists()) {
            JFileChooser fileChooser =
                new JFileChooser(System.getProperty("user.home"));
            fileChooser.setDialogTitle(getMessage("dialog.open.msg"));
            if (fileChooser.showOpenDialog(null) ==
                JFileChooser.APPROVE_OPTION) {
              final File storyfile = fileChooser.getSelectedFile();
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  runStoryFile(storyfile);
                }
              });
            }
          } else {
              runStoryFile(file);
          }
        }
      });
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
  }

  private static void runStoryFile(final File storyFile) {
    ZmppFrame zmppFrame = new ZmppFrame();
    zmppFrame.setVisible(true);
    FileInputStream storyFileStream = null;
    try {
      storyFileStream = new FileInputStream(storyFile);
      MachineInitStruct initStruct = new MachineInitStruct();
      if (storyFile.getName().endsWith("zblorb")) {
        initStruct.blorbFile = storyFileStream;
      } else {
        initStruct.storyFile = storyFileStream;
      }
      initStruct.nativeImageFactory = new AwtImageFactory();
      initStruct.saveGameDataStore = new FileSaveGameDataStore(zmppFrame);
      initStruct.ioSystem = zmppFrame;
      zmppFrame.getScreenModelView().startGame(initStruct);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (storyFileStream != null) {
        try { storyFileStream.close(); } catch (Exception ioex) {
          ioex.printStackTrace();
        }
      }
    }
  }

  private static void runStoryUrl(final URL storyUrl) {
    ZmppFrame zmppFrame = new ZmppFrame();
    zmppFrame.setVisible(true);
    try {
      MachineInitStruct initStruct = new MachineInitStruct();
      if (storyUrl.getPath().endsWith("zblorb") ||
          storyUrl.getPath().endsWith("zblb")) {
        initStruct.blorbURL = storyUrl;
      } else {
        initStruct.storyURL = storyUrl;
      }
      initStruct.nativeImageFactory = new AwtImageFactory();
      initStruct.saveGameDataStore = new FileSaveGameDataStore(zmppFrame);
      initStruct.ioSystem = zmppFrame;
      zmppFrame.getScreenModelView().startGame(initStruct);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void runInEventDispatchThread(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
