/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zmpp.swingui2;

import java.io.File;
import java.io.IOException;

/**
 * An interface that represents the view component of the Z-machine
 * interpreter. Called By ExecutionControl.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface ScreenView {
  void startGame(File storyFile) throws IOException;
  void setReadChar(boolean flag);
  void setReadLine(boolean flag);
}
