/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
