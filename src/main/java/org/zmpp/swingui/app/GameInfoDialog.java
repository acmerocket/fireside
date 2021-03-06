/*
 * Created on 2006/03/10
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.zmpp.blorb.BlorbImage;
import org.zmpp.media.InformMetadata;
import org.zmpp.media.Resources;
import org.zmpp.media.StoryMetadata;
import org.zmpp.swingui.app.Main.AwtImage;

/**
 * This dialog displays information about a story given its meta information.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class GameInfoDialog extends JDialog {

  private static final int STD_WIDTH = 400;
  private static final long serialVersionUID = 1L;

  public GameInfoDialog(JFrame owner, Resources resources) {
    super(owner, Main.getMessage("caption.about") + " " +
          resources.getMetadata().getStoryInfo().getTitle());
    getContentPane().add(createInfoPanel(resources));
    getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
    pack();
    setLocationRelativeTo(owner);
  }

  private JPanel createPicturePanel(Resources resources, int coverartnum) {
    JPanel picpanel = new JPanel();
    if (coverartnum > 0) {
      BlorbImage blorbImage = (BlorbImage)
        resources.getImages().getResource(coverartnum);
      AwtImage awtImage = (AwtImage) blorbImage.getImage();
      JLabel label = new PictureLabel(awtImage.getImage());
      label.setPreferredSize(new Dimension(STD_WIDTH, 400));
      picpanel.add(label);
    }
    return picpanel;
  }

  private JComponent createInfoPanel(Resources resources) {
    StoryMetadata storyinfo = resources.getMetadata().getStoryInfo();
    Box infopanel = Box.createVerticalBox();
    infopanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
    JComponent panel = infopanel;

    // in case cover art is available, stack it into the info panel
    int coverartnum = getCoverartNum(resources);
    if (coverartnum > 0 && resources.getImages().getNumResources() > 0) {
      Box wholepanel = Box.createHorizontalBox();
      wholepanel.add(createPicturePanel(resources, coverartnum));
      wholepanel.add(infopanel);
      panel = wholepanel;
    }

    infopanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    infopanel.setPreferredSize(new Dimension(STD_WIDTH, 400));

    List<JLabel> labels = new ArrayList<JLabel>();
    labels.add(new JLabel(storyinfo.getTitle()));

    if (storyinfo.getHeadline() != null) {
      labels.add(new JLabel(storyinfo.getHeadline()));
    }

    labels.add(new JLabel(storyinfo.getAuthor() + " (" +
                          storyinfo.getYear() + ")"));

    for (JLabel label : labels) {
      infopanel.add(label);
      label.setAlignmentX(Component.LEFT_ALIGNMENT);

      // Ensure that the label fonts are all bold
      label.setFont(label.getFont().deriveFont(Font.BOLD));
    }

    infopanel.add(Box.createVerticalStrut(6));

    JTextArea descarea = new JTextArea(storyinfo.getDescription());
    descarea.setLineWrap(true);
    descarea.setWrapStyleWord(true);
    descarea.setEditable(false);
    Insets margins = new Insets(3, 3, 3, 3);
    descarea.setMargin(margins);
    descarea.setFont(labels.get(0).getFont().deriveFont(Font.PLAIN));

    JScrollPane spane = new JScrollPane(descarea);
    spane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    spane.setPreferredSize(new Dimension(STD_WIDTH, 200));
    spane.setAlignmentX(Component.LEFT_ALIGNMENT);
    infopanel.add(spane);
    return panel;
  }

  private JPanel createButtonPanel() {
    // Set up the other controls
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton(Main.getMessage("caption.ok"));
    okButton.addActionListener(new ActionListener() {
      /** {@inheritDoc} */
      public void actionPerformed(ActionEvent e) { setVisible(false); }
    });
    getRootPane().setDefaultButton(okButton);
    buttonPanel.add(okButton);
    return buttonPanel;
  }

  private int getCoverartNum(Resources resources) {
    int coverartnum = resources.getCoverArtNum();
    InformMetadata metadata = resources.getMetadata();

    // If the picture number is not in the Frontispiece chunk, retrieve it
    // from the metadata
    if (coverartnum <= 0) {
      coverartnum = metadata.getStoryInfo().getCoverPicture();
    }
    return coverartnum;
  }
}
