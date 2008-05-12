/*
 * $Id$
 * 
 * Created on 2008/05/10
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.zmpp.vm.ScreenModel;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.zscreen.BufferedScreenModel;
import org.zmpp.zscreen.BufferedScreenModel.ScreenModelListener;

/**
 * The MainView class is the main view component. It contains the upper and
 * the lower windows.
 * While the lower window is layed out so that its boundaries start
 * at the split position, the upper window component always uses up the
 * whole available space. The upper window in in fact an overlay over the
 * lower window, which is controlled by implementing the MainView as a
 * JLayeredPane.
 */
public class ScreenModelSplitView extends JLayeredPane
implements ScreenModelListener {

  //private static final Font STD_FONT = new Font("Baskerville", Font.PLAIN, 20);
  private static final Font STD_FONT = new Font("American Typewriter", Font.PLAIN, 16);
  private static final Font FIXED_FONT = new Font("Monaco", Font.PLAIN, 16);

  public interface MainViewListener {
    /**
     * The view's dimensions or position have changed.
     * @param viewHeight the current view height
     * @param viewportHeight the current viewport height
     * @param currentViewPos the current view position
     */
    void viewDimensionsChanged(int viewHeight, int viewportHeight,
                               int currentViewPos);
  }

  private JTextPane lower = new JTextPane() {

    /**
     * Ties the background color of the MainView to the background of the
     * TextPane. That's because the upper window is treated as transparent
     * at the beginning.
     * @param color the new background color
     */
    @Override
    public void setBackground(Color color) {
      super.setBackground(color);
      ScreenModelSplitView.this.setBackground(color);
    }
  };
  private JViewport lowerViewport;
  private TextGridView upper = new TextGridView();
  private MainViewListener listener;
  private ScreenModelLayout layout = new ScreenModelLayout();
  private FontSelector fontSelector = new FontSelector();

  public ScreenModelSplitView() {
    setOpaque(true);
    fontSelector.setFixedFont(FIXED_FONT);
    fontSelector.setStandardFont(STD_FONT);
    upper.setFontSelector(fontSelector);
    layout.setFontSelector(fontSelector);
    setLayout(layout);
    setPreferredSize(new Dimension(640, 480));

    Border lowerBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));
    lower.setEditable(true);
    lower.setEnabled(true);
    lower.setBackground(Color.WHITE);
    lower.setForeground(Color.BLACK);
    lowerViewport = new JViewport();
    lowerViewport.setView(lower);
    lowerViewport.addChangeListener(new ChangeListener() {

      /**
       * Called when view size changes.
       * @param e the change event
       */
      public void stateChanged(ChangeEvent e) {
        viewSizeChanged();
      }
    });
    lower.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentMoved(ComponentEvent e) {
        viewSizeChanged();
      }
    });
    lower.setBorder(lowerBorder);

    Border upperBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    upper.setBorder(upperBorder);
    add(upper, JLayeredPane.PALETTE_LAYER);
    add(lowerViewport, JLayeredPane.DEFAULT_LAYER);
    
    // Debugging
    //addStringsToLower();
    split(0);
  }

  private void addStringsToLower() {
    try {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < 40; i++) {
        builder.append("This is line: " + i + "\n");
      }
      lower.getDocument().insertString(0, builder.toString(), null);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public void addMainViewListener(MainViewListener l) {
    listener = l;
  }
  
  @Override
  public void addMouseWheelListener(MouseWheelListener l) {
    lower.addMouseWheelListener(l);
  }

  public void scroll(int value) {
    lower.setLocation(0, value);
    validate();
    repaint();
  }
  
  private void split(int numRowsUpper) {
    layout.setNumRowsUpper(numRowsUpper);
  }

  private void viewSizeChanged() {
    listener.viewDimensionsChanged(lower.getHeight(), lowerViewport.getHeight(),
                                lower.getY());
  }
  
  public int getFixedFontWidth() {
    return upper.getGraphics().getFontMetrics(
      getRomanFixedFont()).charWidth('0');
  }
  
  public int getFixedFontHeight() {
    return upper.getGraphics().getFontMetrics(
      getRomanFixedFont()).getHeight();
  }
  
  private Font getRomanFixedFont() {
    return fontSelector.getFont(ScreenModel.FONT_FIXED,
                                ScreenModel.TEXTSTYLE_ROMAN);
  }

  // *************************************************************************
  // ****** ScreenModelListener
  // ***************************************
  public void screenModelUpdated(BufferedScreenModel screenModel) {
    List<AnnotatedText> text = screenModel.getBottomWindow().getBuffer();
    Document doc = lower.getDocument();
    for (AnnotatedText segment : text) {
      try {
        doc.insertString(doc.getLength(), zsciiToUnicode(segment.getText()),
                null);
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  private String zsciiToUnicode(String str) {
    return str.replace("\r", "\n");
  }

  public void screenSplit(int linesUpperWindow) {
    split(linesUpperWindow);
  }

  public void windowErased(int window) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
  
