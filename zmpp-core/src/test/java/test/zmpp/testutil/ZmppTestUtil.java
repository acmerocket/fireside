/*
 * Created on 2008/07/17
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
package test.zmpp.testutil;

import java.io.File;

/**
 * Usefule utility methods for testing ZMPP.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZmppTestUtil {

  /**
   * Helper function to create a file local to the project given the
   * local path.
   * @param localPath the local path
   * @return the File
   */
  public static File createLocalFile(String localPath) {
    return new File(System.getProperty("user.dir") + File.separatorChar +
                    localPath);
  }
}