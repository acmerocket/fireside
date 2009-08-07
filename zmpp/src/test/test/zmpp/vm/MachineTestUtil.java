/*
 * Created on 10/03/2005
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
package test.zmpp.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vmutil.FileUtils;

/**
 * Test utility class for virtual machine package.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MachineTestUtil {

  protected static MachineImpl createMachine(File file)
    throws IOException {
    MachineImpl machine = new MachineImpl();
    machine.initialize(readData(file), null);
    return machine;
  }

  protected static byte[] readData(File file) throws IOException {
    FileInputStream fileInput = null;
    try {
      fileInput = new FileInputStream(file);
      return FileUtils.readFileBytes(fileInput);
    } finally {
      fileInput.close();
    }
  }  
}
