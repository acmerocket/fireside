/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.zmpp.vm;

import java.io.FileInputStream;
import java.io.IOException;
import org.jmock.MockObjectTestCase;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vmutil.FileUtils;

/**
 *
 * @author weiju
 */
public class MachineTestUtil extends MockObjectTestCase {

  protected static MachineImpl createMachine(String filePath)
    throws IOException {
    MachineImpl machine = new MachineImpl();
    machine.initialize(readData(filePath), null);
    return machine;
  }
  
  protected static byte[] readData(String filePath) throws IOException {
    FileInputStream fileInput = null;
    try {
      fileInput = new FileInputStream(filePath);
      return FileUtils.readFileBytes(fileInput);
    } finally {
      fileInput.close();
    }
  }  
}
