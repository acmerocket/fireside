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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import org.zmpp.vm.ExecutionControl;
import org.zmpp.vm.Machine.MachineRunState;
import org.zmpp.vm.ScreenModel;
import org.zmpp.windowing.AnnotatedCharacter;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.windowing.TextAnnotation;
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

  //private static final Font STD_FONT = new Font("Baskerville", Font.PLAIN, 16);
  private static final Font STD_FONT = new Font("American Typewriter", Font.PLAIN, 14);
  private static final Font FIXED_FONT = new Font("Monaco", Font.PLAIN, 14);
  private static final int DEFAULT_FOREGROUND = ScreenModel.COLOR_WHITE;
  private static final int DEFAULT_BACKGROUND = ScreenModel.COLOR_BLUE;
  private int editStart;
  private ExecutionControl executionControl;
  private BufferedScreenModel screenModel;
  private boolean isReadLine, isReadChar;

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

  /**
   * Constructor.
   */
  public ScreenModelSplitView() {
    initLayout();
    createUpperView();
    createLowerView();
    split(0);
  }
  
  // ************************************************************************
  // **** User interface setup
  // ************************************
  
  private void initLayout() {
    setOpaque(true);
    setPreferredSize(new Dimension(640, 480));
    fontSelector.setFixedFont(FIXED_FONT);
    fontSelector.setStandardFont(STD_FONT);    
    layout.setFontSelector(fontSelector);
    setLayout(layout);    
  }

  private void createUpperView() {
    upper.setFontSelector(fontSelector);
    Border upperBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    upper.setBorder(upperBorder);
    add(upper, JLayeredPane.PALETTE_LAYER);
  }
  
  private void createLowerView() {

    Border lowerBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));
    lower.setEditable(true);
    lower.setEnabled(true);
    lower.setBackground(getColor(DEFAULT_BACKGROUND));
    lower.setForeground(getColor(DEFAULT_FOREGROUND));
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
    add(lowerViewport, JLayeredPane.DEFAULT_LAYER);
    installLowerHandlers();
  }

  private void installLowerHandlers() {
    // TODO: Mouse clicks can as well influence typing position
    // Make sure mouse clicks do not change caret position
    ScreenModelViewInputHandler inputHandler =
      new ScreenModelViewInputHandler(this);
    lower.addKeyListener(inputHandler);

    // In order to influence the caret position on a mouse click, we need to
    // override caret behaviour
    lower.getCaret().addChangeListener(inputHandler);
  }
  
  private Color getColor(int screenModelColor) {
    return ColorTranslator.getInstance().translate(screenModelColor);
  }
  
  // ***********************************************************************
  // **** Protected interface
  // *********************************

  JTextPane getLower() { return lower; }
  int getEditStart() { return editStart; }
  boolean isReadChar() { return isReadChar; }
  boolean isReadLine() { return isReadLine; }
  ExecutionControl getExecutionControl() { return executionControl; }
  
  // ***********************************************************************
  // **** Public interface
  // *********************************
  
  public void switchModeOnRunState(MachineRunState runState) {
    if (runState == MachineRunState.READ_CHAR) enterReadCharMode();
    else if (runState == MachineRunState.READ_LINE) enterReadLineMode();
  }
  
  public void initUI(BufferedScreenModel screenModel,
                     ExecutionControl control) {
    executionControl = control;
    executionControl.setDefaultColors(DEFAULT_BACKGROUND,
                                      DEFAULT_FOREGROUND);
    this.screenModel = screenModel;
    screenModel.addScreenModelListener(this);
    setSizes();
    setLowerFontStyles();
  }
  
  private void setSizes() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();
    int charWidth = getFixedFontWidth();
    int charHeight = getFixedFontHeight();
    int numCharsPerRow = componentWidth / charWidth;
    int numRows = componentHeight / charHeight;
    
    System.out.println("Char width: " + charWidth + " component width: " +
            componentWidth + " # chars/row: " + numCharsPerRow +
            " char height: " + charHeight + " # rows: " + numRows);
    upper.setGridSize(numRows, numCharsPerRow);
    executionControl.resizeScreen(numRows, numCharsPerRow);
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
  
  private Font getRomanStdFont() {
    return fontSelector.getFont(ScreenModel.FONT_NORMAL,
                                ScreenModel.TEXTSTYLE_ROMAN);
  }

  // *************************************************************************
  // ****** ScreenModelListener
  // ***************************************
  public void screenModelUpdated(BufferedScreenModel screenModel) {
    List<AnnotatedText> text = screenModel.getLowerBuffer();
    for (AnnotatedText segment : text) {
      appendToLower(segment);
    }
  }
  
  public void topWindowUpdated(int cursorx, int cursory, AnnotatedCharacter c) {
    upper.setCharacter(cursory, cursorx, c);
    repaint();
  }

  private void appendToLower(AnnotatedText segment) {
    Document doc = lower.getDocument();
    try {
      doc.insertString(doc.getLength(), zsciiToUnicode(segment.getText()),
                       getStyleAttributes(segment.getAnnotation()));
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }
  
  private MutableAttributeSet getStyleAttributes(TextAnnotation annotation) {
     MutableAttributeSet attributes = getLowerAttributes();
     Font font = fontSelector.getFont(annotation);
     StyleConstants.setFontFamily(attributes, font.getFamily());
     StyleConstants.setFontSize(attributes, font.getSize());
     StyleConstants.setBold(attributes, font.isBold());
     StyleConstants.setItalic(attributes, font.isItalic());
     ColorTranslator colorTranslator = ColorTranslator.getInstance();
     Color background = colorTranslator.translate(annotation.getBackground(),
       DEFAULT_BACKGROUND);
     Color foreground = colorTranslator.translate(annotation.getForeground(),
       DEFAULT_FOREGROUND);
     if (annotation.isReverseVideo()) {
       StyleConstants.setBackground(attributes, foreground);
       StyleConstants.setForeground(attributes, background);
     } else {
       StyleConstants.setBackground(attributes, background);
       StyleConstants.setForeground(attributes, foreground);
     }
     return attributes;
  }
  
  private void setLowerFontStyles() {
    MutableAttributeSet bottomAttrs = getLowerAttributes();
    StyleConstants.setFontFamily(bottomAttrs, getRomanStdFont().getFamily());
    StyleConstants.setFontSize(bottomAttrs, getRomanStdFont().getSize());
  }
  
  private MutableAttributeSet getLowerAttributes() {
    return lower.getInputAttributes();
  }
  
  private String zsciiToUnicode(String str) {
    return str.replace("\r", "\n");
  }

  public void screenSplit(int linesUpperWindow) {
    split(linesUpperWindow);
  }

  public void windowErased(int window) {
    if (window == -1) {
      clearAll();
    } else {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
  
  private void clearAll() {
    try {
      ColorTranslator translator = ColorTranslator.getInstance();
      lower.setBackground(translator.translate(
        screenModel.getLowerBackground(), DEFAULT_BACKGROUND));
      lower.setForeground(translator.translate(
        screenModel.getLowerForeground(), DEFAULT_FOREGROUND));
      StringBuilder formFeed = new StringBuilder();
      for (int i = 0; i < upper.getNumRows(); i++) {
        formFeed.append("\n");
      }
      TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_NORMAL,
        ScreenModel.TEXTSTYLE_ROMAN,
        screenModel.getLowerBackground(),
        screenModel.getLowerForeground());
      appendToLower(new AnnotatedText(annotation, formFeed.toString()));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    upper.clear();
  }
  
  // *************************************************************************
  // ****** Game control
  // ***************************************
  private void enterReadCharMode() {
    this.isReadLine = false;
    this.isReadChar = true;
    viewCursor(true);
  }
  
  private void enterReadLineMode() {
    this.isReadChar = false;
    this.isReadLine = true;
    viewCursor(true);
  }

  private void viewCursor(final boolean flag) {
    runInUIThread(new Runnable() {
      public void run() {
        if (flag) {
          editStart = lower.getDocument().getLength();
          lower.setCaretPosition(editStart);
          lower.requestFocusInWindow();
        } else {
          // might set caret to invisible
        }
      }
    });
  }

  /**
   * A little more readable to execute Runnable within UI thread.
   * @param runnable the Runnable
   */
  private void runInUIThread(Runnable runnable) {
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

