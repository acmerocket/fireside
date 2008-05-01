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
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
  
  public static void main(String[] args) {

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          JFrame frame = new JFrame("Z-machine Preservation Project 1.5");
          StdScreenView view = new StdScreenView(frame);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.getContentPane().add(view);
          frame.pack();
          frame.setVisible(true);
          try {
            view.startGame(new File("testfiles/minizork.z3"));
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
