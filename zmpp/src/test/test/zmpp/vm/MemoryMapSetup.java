/*
 * $Id$
 * 
 * Created on 24.09.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package test.zmpp.vm;

import java.io.File;
import java.io.RandomAccessFile;

import org.zmpp.base.MemoryAccess;

import junit.framework.TestCase;

/**
 * This class acts as a base test class and sets up testing memory maps.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public abstract class MemoryMapSetup extends TestCase {

  protected MemoryAccess minizorkmap;
  
  protected void setUp() throws Exception {
    
    File zork1 = new File("testfiles/minizork.z3");
    RandomAccessFile file = new RandomAccessFile(zork1, "r");
    int fileSize = (int) file.length();
    byte[] zork1data = new byte[fileSize];    
    file.read(zork1data);
    file.close();
    minizorkmap = new MemoryAccess(zork1data);
  }
}
