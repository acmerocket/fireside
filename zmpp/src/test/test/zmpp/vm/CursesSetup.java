package test.zmpp.vm;

import java.io.File;
import java.io.FileInputStream;

import org.junit.BeforeClass;
import org.zmpp.base.Memory;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharDecoder;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.instructions.DefaultInstructionDecoder;
import org.zmpp.vm.Abbreviations;
import org.zmpp.vm.GameData;
import org.zmpp.vm.GameDataImpl;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vmutil.FileUtils;

/**
 * Set up the test with a Curses game.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class CursesSetup {

  protected Memory curses;
  protected GameData config;
  protected ZCharDecoder converter;
  protected StoryFileHeader fileheader;
  protected Abbreviations abbreviations;
  protected Machine machineState;
  private static byte[] originalData;

  @BeforeClass
  public static void setupClass() throws Exception {
  	File cursesFile = new File("testfiles/curses.z5");
  	FileInputStream fileInput = new FileInputStream(cursesFile);
  	originalData = FileUtils.readFileBytes(fileInput);
  	fileInput.close();
  }
  
  protected void setUp() throws Exception {
  	byte[] data = new byte[originalData.length];
  	System.arraycopy(originalData, 0, data, 0, originalData.length);
  	config = new GameDataImpl(data, null);
  	curses = config.getMemory();
  	fileheader = config.getStoryFileHeader();

  	abbreviations = new Abbreviations(curses,
  			fileheader.getAbbreviationsAddress());
  	ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
  	AlphabetTable alphabetTable = new DefaultAlphabetTable();
  	ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
  	converter = new DefaultZCharDecoder(encoding, translator, abbreviations);

  	machineState = new MachineImpl();
  	machineState.initialize(config, new DefaultInstructionDecoder());
  }
}
