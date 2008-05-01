/*
 * $Id$
 * 
 * Created on 2008/04/25
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

import org.zmpp.vm.ExecutionControl;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.awt.font.LineMetrics;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.zmpp.vm.InvalidStoryException;
import org.zmpp.vm.Machine.MachineRunState;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.zscreen.BufferedScreenModel;
import org.zmpp.zscreen.BufferedScreenModel.ScreenModelListener;
import org.zmpp.zscreen.BufferedScreenModel.StatusLineListener;

/**
 * A standard Swing component to act as main user interface to the Z-machine.
 * The new ZMPP Swing screen model employs MVC in a stricter sense than in
 * previous versions. It employs an underlying BufferedScreenModel as its
 * model component and listens to update events from the model in order
 * to update the view.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StdScreenView extends JPanel
  implements ScreenModelListener, StatusLineListener {

  private JPanel mainPanel = new JPanel(new BorderLayout());
  private JTextPane topWindow, bottomWindow;
  private JScrollPane scrollPane;
  private JPanel statusPanel;
  private JLabel objectDescLabel = new JLabel(" "),
                 statusLabel = new JLabel(" ");  
  private BufferedScreenModel screenModel = new BufferedScreenModel();
  private int editStart;
  private boolean isReadLine, isReadChar;
  private ExecutionControl executionControl;
  private JFrame frame;
  private int charWidth, charHeight;
  
  /**
   * Constructor.
   * @param frame the application frame, can be null in case of applet mode
   */
  public StdScreenView(JFrame frame) {
    super(new BorderLayout());
    createTopWindow();
    createBottomWindow();
    add(createStatusPanel(), BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
    mainPanel.setBorder(null);
    mainPanel.add(topWindow, BorderLayout.NORTH);    
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    screenModel.addScreenModelListener(this);
    screenModel.addStatusLineListener(this);
    installBottomWindowHandlers();
  }
  
  /**
   * Constructor.
   */
  public StdScreenView() {
    this(null);
  }

  private void createTopWindow() {
    topWindow = new JTextPane(); //new JEditorPane(EDITOR_TYPE, "");
    topWindow.setBorder(null);
    topWindow.setPreferredSize(new Dimension(640, 0));
    // just for debugging and better visibility
    topWindow.setBackground(Color.BLUE);
    topWindow.setForeground(Color.WHITE);
  }

  private void createBottomWindow() {
    bottomWindow = new JTextPane(); //new JEditorPane(EDITOR_TYPE, "");
    bottomWindow.setBorder(null);
    scrollPane = new JScrollPane(bottomWindow);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(640, 480));
  }
  
  private JPanel createStatusPanel() {
    statusPanel = new JPanel(new GridLayout(1, 2));
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    statusPanel.add(leftPanel);
    statusPanel.add(rightPanel);
    leftPanel.add(objectDescLabel);
    rightPanel.add(statusLabel);
    return statusPanel;
  }

  private void installBottomWindowHandlers() {
    // TODO: Mouse clicks can as well influence typing position
    // Make sure mouse clicks do not change caret position
    bottomWindow.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {        
        //System.out.println("keyTyped(): " + e.getKeyChar() + " code: " + e.getKeyCode());
      }

      public void keyPressed(KeyEvent e) {
        //System.out.println("-------------------------");
        //System.out.println("keyPressed(): " + e.getKeyChar() + " code: " + e.getKeyCode());
        preventKeyActionIfNeeded(e);
      }

      public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased(): " + e.getKeyChar() + " code: " + e.getKeyCode());
        //preventKeyActionIfNeeded(e);
      }
    });
  }
  
  // *********************************************************************
  // ****** MachineInitCallback
  // ***************************************
  
  private void setSizes() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();
    int numCharsPerRow = componentWidth / charWidth;
    int numRows = componentHeight / charHeight;
    screenModel.setNumCharsPerRow(numCharsPerRow);
    
    System.out.println("Char width: " + charWidth + " component width: " +
            componentWidth + " # chars/row: " + numCharsPerRow +
            " char height: " + charHeight + " # rows: " + numRows);
    executionControl.resizeScreen(numRows, numCharsPerRow);
  }

  private void initUI() {
    int version = executionControl.getVersion();
    System.out.println("initUI, story file version: " + version);
    statusPanel.setVisible(version <= 3);
    
    // Just for prototyping
    StyledDocument topDoc = (StyledDocument) topWindow.getDocument();

    // Top window setup
    MutableAttributeSet topAttrs = getTopAttributes();
    StyleConstants.setFontFamily(topAttrs, "Monospaced");
    MutableAttributeSet bottomAttrs = getBottomAttributes();
    StyleConstants.setFontFamily(bottomAttrs, "Serif");
    StyleConstants.setFontSize(bottomAttrs, 15);
    // DEBUG
    StyleConstants.setBackground(topAttrs, Color.BLUE);
    StyleConstants.setForeground(topAttrs, Color.WHITE);


    measureFont(topDoc.getFont(topAttrs));
    setSizes();
  }
  
  private void measureFont(Font font) {
    Graphics2D g2d = (Graphics2D) bottomWindow.getGraphics();
    LineMetrics lineMetrics = font.getLineMetrics("0", g2d.getFontRenderContext());
    FontMetrics fontMetrics = g2d.getFontMetrics(font);
    charHeight = (int) lineMetrics.getHeight();
    charWidth = fontMetrics.charWidth('0');
  }

  // *********************************************************************
  // ****** ScreenModelListener
  // ***************************************
  /**
   * {@inheritDoc}
   */
  public void screenModelUpdated(final BufferedScreenModel screenModel) {
    runInUIThread(new Runnable() {
      public void run() {
        List<AnnotatedText> buffer = screenModel.getBottomWindow().getBuffer();
        for (AnnotatedText str : buffer) {
          appendToBottom(getViewString(str.getText()));
        }
      }
    });
  }

  public void screenSplit(int linesUpperWindow) {
    System.out.println("Screen Split, # lines upper: " + linesUpperWindow);
    topWindow.setPreferredSize(new Dimension(getWidth(), charHeight * linesUpperWindow));
    screenModel.getTopWindow().resize(linesUpperWindow, screenModel.getTopWindow().getNumColumns());
    doLayout();
    windowErased(0);
  }

  public void windowErased(int window) {
    topWindow.setCaretPosition(0);
    int rows = screenModel.getTopWindow().getNumRows();
    int cols = screenModel.getTopWindow().getNumColumns();
    System.out.println("Window erased, rows: " + rows + " cols: " + cols);
    int offset = 0;
    
    clearTopWindow();
    MutableAttributeSet attrs = new SimpleAttributeSet();
    StyleConstants.setForeground(attrs, Color.WHITE);
    StyleConstants.setBackground(attrs, Color.BLUE);
    StyleConstants.setFontFamily(attrs, "Monospaced");
    
    StyledDocument doc = topWindow.getStyledDocument();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        setTopCharacter(doc, offset++, 'D', attrs);
      }
      setTopCharacter(doc, offset++, '\n', attrs);
    }
  }
  
  private void clearTopWindow() {
    StyledDocument doc = topWindow.getStyledDocument();
    try {
      doc.remove(0, doc.getLength());
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }

  private void setTopCharacter(StyledDocument doc, int offset, char c,
          AttributeSet attrs) {
    try {
      doc.insertString(offset, String.valueOf(c), attrs);
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }

  // *********************************************************************
  // ****** StatusLineListener
  // ***************************************
  /**
   * {@inheritDoc}
   */
  public void statusLineUpdated(String objectDescription, String status) {
    objectDescLabel.setText(objectDescription);
    statusLabel.setText(status);
  }
  
  // *********************************************************************
  // ****** ScreenView
  // ***************************************
  
  /**
   * {@inheritDoc}
   */
  public void startGame(File storyFile)
      throws IOException, InvalidStoryException {
    MachineInitStruct initStruct = new MachineInitStruct();
    initStruct.storyFile = storyFile;
    initStruct.screenModel = screenModel;
    initStruct.statusLine = screenModel;
    
    if (this.isVisible()) {
      executionControl = new ExecutionControl(initStruct);
      initUI();
      MachineRunState runState = executionControl.run();
      System.out.println("PAUSING WITH STATE: " + runState);
      switchModeOnRunState(runState);
    }
  }
  
  private void switchModeOnRunState(MachineRunState runState) {
    if (runState == MachineRunState.READ_CHAR) this.setReadChar(true);
    else if (runState == MachineRunState.READ_LINE) this.setReadLine(true);
  }
  
  private void setReadLine(boolean flag) {
    isReadLine = flag;
    viewCursor(flag);
  }
  
  private void setReadChar(boolean flag) {
    System.out.println("setReadChar(): " + flag);
    isReadChar = flag;
    viewCursor(flag);
  }
  
  private void consumeKeyEvent(KeyEvent e) {
    if (e != null) e.consume();
  }
  
  private void preventKeyActionIfNeeded(KeyEvent e) {
    if (isReadChar) {
      System.out.println("HAHAHAHA");
      switchModeOnRunState(executionControl.resumeWithInput("\n"));
      consumeKeyEvent(e);
    }
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      // Handle up key
      consumeKeyEvent(e);
    }
    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      // Handle down key
      consumeKeyEvent(e);
    }
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      handleEnterKey(e);
    }
    if (bottomWindow.getCaretPosition() <= editStart &&
        keyCodeLeadsToPreviousPosition(e.getKeyCode())) {
      consumeKeyEvent(e);
    }
  }
  
  private void handleEnterKey(KeyEvent e) {
    if (isReadLine) {
      Document doc = bottomWindow.getDocument();
      try {
        String input = doc.getText(editStart, doc.getLength() - editStart);
        System.out.println("ENTER PRESSED, input: " + input);
        consumeKeyEvent(e);
        doc.insertString(doc.getLength(), "\n", null);
        switchModeOnRunState(executionControl.resumeWithInput(input));
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  private boolean keyCodeLeadsToPreviousPosition(int keyCode) {
    return keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_LEFT;            
  }
  
  private void viewCursor(final boolean flag) {
    runInUIThread(new Runnable() {
      public void run() {
        if (flag) {
          editStart = bottomWindow.getDocument().getLength();
          bottomWindow.setCaretPosition(editStart);
          bottomWindow.requestFocusInWindow();
        } else {
          // might set caret to invisible
        }
      }
    });
  }
  
  private MutableAttributeSet getTopAttributes() {
    return topWindow.getInputAttributes();
  }
  
  private MutableAttributeSet getBottomAttributes() {
    return bottomWindow.getInputAttributes();
  }
  
  private void appendToBottom(String str) {
    try {
      StyledDocument doc = (StyledDocument) bottomWindow.getDocument();
      doc.insertString(doc.getLength(), str, getBottomAttributes());
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * Convert ZSCII string to a string in Unicode.
   * @param zsciiString the ZSCII string
   * @return the Unicode representation
   */
  private String getViewString(String zsciiString) {
    String result = zsciiString.replaceAll("\r", "\n");
    return result;
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
