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
package org.zmpp.zscreen;

import org.zmpp.vm.StatusLine;

/**
 * This status line model implementation takes advantage of the fact that
 * the standard screen model consists of two windows and therefore wraps
 * a screen model, updates go to the upper window.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StatusLineModel implements StatusLine {

  private BufferedScreenModel screenModel;
  
  public StatusLineModel(BufferedScreenModel screenModel) {
    this.screenModel = screenModel;
  }

  // StatusLine 
  public void updateStatusScore(String objectName, int score, int steps) {
    System.out.println("(Status) Object: " + objectName + " " + score + "/" +
            steps);
  }

  public void updateStatusTime(String objectName, int hours, int minutes) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
