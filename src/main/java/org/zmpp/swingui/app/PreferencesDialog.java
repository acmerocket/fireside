/*
 * Created on 2006/03/27
 * Copyright 2005-2010 by Wei-ju Wu
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.border.Border;
import org.zmpp.swingui.view.DisplaySettings;
import org.zmpp.windowing.ScreenModel;

/**
 * A dialog to set the display preferences for the user interface.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class PreferencesDialog extends JDialog implements ActionListener {

  /**
   * An item for the color list.
   */
  static class ColorItem {
    int color;
    String name;

    /**
     * Constructor.
     * @param colornum the color number
     * @param name the name of the color
     */
    public ColorItem(int colornum, String name) {
      this.color = colornum;
      this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { return name; }
  }

  private static final ColorItem[] COLORS = {
    new ColorItem(ScreenModel.UNDEFINED, ""),
    new ColorItem(ScreenModel.COLOR_BLACK, getMessage("caption.black")),
    new ColorItem(ScreenModel.COLOR_RED, getMessage("caption.red")),
    new ColorItem(ScreenModel.COLOR_GREEN, getMessage("caption.green")),
    new ColorItem(ScreenModel.COLOR_YELLOW, getMessage("caption.yellow")),
    new ColorItem(ScreenModel.COLOR_BLUE, getMessage("caption.blue")),
    new ColorItem(ScreenModel.COLOR_MAGENTA, getMessage("caption.magenta")),
    new ColorItem(ScreenModel.COLOR_CYAN, getMessage("caption.cyan")),
    new ColorItem(ScreenModel.COLOR_WHITE, getMessage("caption.white")),
    new ColorItem(ScreenModel.COLOR_MS_DOS_DARKISH_GREY,
        getMessage("caption.darkgray")),
  };

  /**
   * Serial version uid.
   */
  private static final long serialVersionUID = 1L;

  private JSpinner stdfontSpinner;
  private JSpinner fixedfontSpinner;
  private JComboBox foregroundCB;
  private JComboBox backgroundCB;
  private JCheckBox antialiasCB;
  private Preferences preferences;
  private DisplaySettings settings;
  private String stdFontName;
  private String fixedFontName;

  /**
   * Constructor.
   * @param parent the parent frame
   * @param preferences the preferences object
   * @param settings the display settings
   */
  public PreferencesDialog(JFrame parent, Preferences preferences,
                           DisplaySettings settings) {
    super(parent, getMessage("dialog.prefs.title"), true);
    this.preferences = preferences;
    this.settings = settings;

    // Control panel
    GridLayout grid = new GridLayout(5, 2);
    grid.setVgap(3);
    grid.setHgap(3);

    JPanel mainpanel = new JPanel(grid);

    JLabel stdfontLabel = new JLabel(getMessage("caption.stdfont.size"));
    mainpanel.add(stdfontLabel);
    stdfontSpinner = new JSpinner();
    stdfontSpinner.setValue(settings.getStdFont().getSize());
    mainpanel.add(stdfontSpinner);

    JLabel fixedfontLabel = new JLabel(getMessage("caption.fixedfont.size"));
    mainpanel.add(fixedfontLabel);
    fixedfontSpinner = new JSpinner();
    fixedfontSpinner.setValue(settings.getFixedFont().getSize());
    mainpanel.add(fixedfontSpinner);

    JLabel backgroundLabel = new JLabel(getMessage(
        "caption.default.background"));
    mainpanel.add(backgroundLabel);
    backgroundCB = new JComboBox(COLORS);
    mainpanel.add(backgroundCB);
    preselect(backgroundCB, settings.getDefaultBackground());

    JLabel foregroundLabel = new JLabel(getMessage(
        "caption.default.foreground"));
    mainpanel.add(foregroundLabel);
    foregroundCB = new JComboBox(COLORS);
    mainpanel.add(foregroundCB);
    preselect(foregroundCB, settings.getDefaultForeground());

    JLabel antialiasLabel = new JLabel(getMessage("caption.antialias"));
    mainpanel.add(antialiasLabel);
    antialiasCB = new JCheckBox();
    antialiasCB.setSelected(settings.getAntialias());
    mainpanel.add(antialiasCB);

    // Button panel
    Box lowpanel = new Box(BoxLayout.Y_AXIS);
    lowpanel.add(new JSeparator());
    JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    lowpanel.add(buttonpanel);

    JButton okbutton = new JButton(getMessage("caption.ok"));
    JButton cancelbutton = new JButton(getMessage("caption.cancel"));
    getRootPane().setDefaultButton(okbutton);
    buttonpanel.add(okbutton);
    buttonpanel.add(cancelbutton);
    okbutton.addActionListener(this);
    cancelbutton.addActionListener(this);

    getContentPane().add(mainpanel, BorderLayout.NORTH);
    getContentPane().add(
        new JLabel(getMessage("caption.restarttochange")),
                   BorderLayout.CENTER);
    getContentPane().add(lowpanel, BorderLayout.SOUTH);

    Border border = BorderFactory.createEmptyBorder(5, 5, 5, 3);
    ((JPanel) getContentPane()).setBorder(border);
    ((BorderLayout) getContentPane().getLayout()).setVgap(5);

    pack();

    // For now, just remember the fixed and std font names
    stdFontName = settings.getStdFont().getFontName();
    fixedFontName = settings.getFixedFont().getFontName();
  }

  /**
   * {@inheritDoc}
   */
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals(getMessage("caption.ok"))) {

      // Transfer the settings to the user settings only, they will
      // only take effect on the next restart
      int stdfontsize = Integer.valueOf(stdfontSpinner.getValue().toString());
      int fixedfontsize =
        Integer.valueOf(fixedfontSpinner.getValue().toString());
      int bgcolor = ((ColorItem) backgroundCB.getSelectedItem()).color;
      int fgcolor = ((ColorItem) foregroundCB.getSelectedItem()).color;
      boolean antialias = antialiasCB.isSelected();

      preferences.put("stdfontsize", String.valueOf(stdfontsize));
      preferences.put("fixedfontsize", String.valueOf(fixedfontsize));
      preferences.put("defaultbackground", String.valueOf(bgcolor));
      preferences.put("defaultforeground", String.valueOf(fgcolor));
      preferences.put("antialias", antialias ? "true" : "false");
      preferences.put("stdfontname", stdFontName);
      preferences.put("fixedfontname", fixedFontName);
      Font stdFont = new Font(ZmppFrame.STD_FONT_NAME, Font.PLAIN, stdfontsize);

      Font fixedFont = new Font(ZmppFrame.FIXED_FONT_NAME, Font.PLAIN,
                                fixedfontsize);
      settings.setSettings(stdFont, fixedFont, bgcolor, fgcolor, antialias);
      try {
        preferences.flush();
      } catch (BackingStoreException ex) {
        ex.printStackTrace();
      }
    }
    dispose();
  }

  /**
   * Preselect the specified item in the combobox.
   * @param value the value to select
   */
  private void preselect(JComboBox combobox, int value) {
    for (int i = 0; i < COLORS.length; i++) {
      if (COLORS[i].color == value) {
        combobox.setSelectedItem(COLORS[i]);
        break;
      }
    }
  }

  private static String getMessage(String key) {
    return Main.getMessage(key);
  }
}
