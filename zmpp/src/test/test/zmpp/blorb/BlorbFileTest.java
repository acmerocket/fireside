/**
 * $Id$
 */
package test.zmpp.blorb;

import java.io.File;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer.Info;

import junit.framework.TestCase;

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.base.MemoryAccess;
import org.zmpp.base.MemoryReadAccess;
import org.zmpp.blorb.AiffInputStream;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;

/**
 * This testing class explores the structure of a BLORB file.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class BlorbFileTest extends TestCase {

  public void testLurkingHorrorBlorb() throws Exception {

    File file = new File("testfiles/Lurking.blb");
    RandomAccessFile rndfile = null;
    SourceDataLine line = null;
    
    try {
      
      rndfile = new RandomAccessFile(file, "r");
      byte[] data = new byte[(int) rndfile.length()];
      rndfile.readFully(data);
      MemoryAccess memaccess = new DefaultMemoryAccess(data);
      FormChunk formchunk = new DefaultFormChunk(memaccess);
      
      // Chunk type
      assertEquals("IFRS", new String(formchunk.getSubId()));
      
      // Ridx chunk
      Chunk ridxChunk = formchunk.getSubChunk("RIdx".getBytes());
      assertNotNull(ridxChunk);
      MemoryReadAccess chunkmem = ridxChunk.getMemoryAccess();
      int offset = 8;
      int numresources = (int) chunkmem.readUnsigned32(offset);
      assertEquals(14, numresources);
      
      // Usage
      byte[] usage = new byte[4];
      for (int i = 0; i < 4; i++) usage[i] = chunkmem.readByte(offset + 4 + i);
      assertEquals("Snd ", new String(usage));

      // Number of resource
      int resnum = (int) chunkmem.readUnsigned32(offset + 8);
      assertEquals(3, resnum);

      // Start of resource
      int resstart = (int) chunkmem.readUnsigned32(offset + 12);
      assertEquals(308, resstart);
      Chunk sound1 = formchunk.getSubChunk(308);
      assertNotNull(sound1);
      FormChunk soundchunk1 = new DefaultFormChunk(sound1.getMemoryAccess());
      assertEquals("AIFF", new String(soundchunk1.getSubId()));
      
      Chunk commChunk = soundchunk1.getSubChunk("COMM".getBytes());
      assertNotNull(commChunk);
      MemoryReadAccess sound1mem = commChunk.getMemoryAccess();
      
      // sample rate
      int ratebits = (int) sound1mem.readUnsigned32(16);
      float rate = Float.intBitsToFloat(ratebits);
      assertEquals(2.1967278f, rate);
      
      AiffInputStream aiffStream = new AiffInputStream(soundchunk1);
      AudioFileFormat aiffFormat1 = AudioSystem.getAudioFileFormat(aiffStream);
      AudioFormat audioFormat1 = aiffFormat1.getFormat();
      assertEquals(1, audioFormat1.getChannels());
      assertEquals(8, audioFormat1.getSampleSizeInBits());
      assertEquals(9676.193f, audioFormat1.getSampleRate());
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat1);
      assertTrue(AudioSystem.isLineSupported(info));
      AudioInputStream stream = new AudioInputStream(aiffStream,
                                                     aiffFormat1.getFormat(),
                                                     (long) soundchunk1.getSize());
      assertEquals(soundchunk1.getSize(), stream.available());
      assertEquals(49802, stream.available());
      
      byte[] sdata = new byte[stream.available()];
      stream.read(sdata);
      line = (SourceDataLine) AudioSystem.getLine(info);
      Info[] infos = AudioSystem.getMixerInfo();
      for (int i = 0; i < infos.length; i++) {
        
        System.out.println(infos[i].toString());
        
      }
      line.open(audioFormat1);
      //FloatControl volCtrl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
      //assertEquals(0, volCtrl.getValue());
      line.start();
      line.write(sdata, 0, sdata.length);
      line.drain();
/*
      Iterator<Chunk> iter = soundchunk1.getSubChunks();      
      while (iter.hasNext()) {
        
        System.out.println("Chunk: " + new String(iter.next().getId()));
      }*/
      /*
      Iterator<Chunk> iter = formchunk.getSubChunks();      
      while (iter.hasNext()) {
        
        System.out.println("Chunk: " + new String(iter.next().getId()));
      }*/
      
    } finally {
      
      if (line != null) {
        line.stop();
        line.close();
      }
      if (rndfile != null) rndfile.close();
    }
  }
/*
  public void testJourneyBlorb() throws Exception {

    System.out.println("Reading Journey blorb");
    File file = new File("testfiles/Journey.blb");
    RandomAccessFile rndfile = null;
    
    try {
      
      rndfile = new RandomAccessFile(file, "r");
      byte[] data = new byte[(int) rndfile.length()];
      rndfile.readFully(data);
      MemoryAccess memaccess = new DefaultMemoryAccess(data);
      FormChunk formchunk = new DefaultFormChunk(memaccess);
      assertEquals("IFRS", new String(formchunk.getSubId()));
      Iterator<Chunk> iter = formchunk.getSubChunks();
      while (iter.hasNext()) {
        
        System.out.println("Chunk: " + new String(iter.next().getId()));
      }
      
    } finally {
      
      if (rndfile != null) rndfile.close();
    }
  }*/
}
