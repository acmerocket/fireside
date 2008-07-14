/*
 * $Id$
 * 
 * Created on 2006/02/13
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
package org.zmpp.vmutil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.media.Resources;

/**
 * This utility class was introduced to avoid a code smell in data
 * initialization.
 * It offers methods to read data from streams and files.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class FileUtils {

  /**
   * This class only contains static methods.
   */
  private FileUtils() { }
  
  /**
   * Creates a resources object from a Blorb file.
   * @param imageFactory the NativeImageFactory
   * @param blorbfile the file
   * @return the resources object or null (on failure)
   */
  public static Resources createResources(NativeImageFactory imageFactory,
          final File blorbfile) {
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(blorbfile, "r");
      final byte[] data = new byte[(int) raf.length()];
      raf.readFully(data);
      final Memory memory = new DefaultMemory(data);
      final FormChunk formchunk = new DefaultFormChunk(memory);
      return new BlorbResources(imageFactory, formchunk);
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (raf != null) {
        try { raf.close(); } catch (Exception ex) {
          ex.printStackTrace(System.err);
        }
      }        
    }
    return null;
  }
  
  /**
   * Reads an array of bytes from the given input stream.
   * @param inputstream the input stream
   * @return the bytes or null if the inputstream is null
   */
  public static byte[] readFileBytes(final InputStream inputstream) {
    if (inputstream == null) return null;

    // Start with a buffer size between 1K and 1M based on available memory.
    final int minBufferSize = (int)Math.max(1024, Math.min(Runtime.getRuntime().freeMemory()/10, 1024 * 1024));

    List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
    int totalBytesRead = 0;

    // Fill buffer lists
    try {
      final ReadableByteChannel rbc = Channels.newChannel(inputstream);
      ByteBuffer bb = ByteBuffer.allocate(minBufferSize);
      buffers.add(bb);

      int bytesRead;
      while ((bytesRead = rbc.read(bb)) != -1) {
        totalBytesRead += bytesRead;
        // if this buffer is mostly full, create another one and avoid small read iterations
        if (bb.remaining() < 16) {
          bb.flip();
          bb = ByteBuffer.allocate(Math.max(minBufferSize, totalBytesRead/2));
          buffers.add(bb);
        }
      }
      bb.flip();

    } catch (IOException e) {
      throw new RuntimeException("Unable to read file bytes", e);
    }

    // merge the buffers so we can convert to a byte array.
    final ByteBuffer data =  ByteBuffer.allocate(totalBytesRead);
    for (final ByteBuffer buf : buffers) {
      data.put(buf);
    }
    data.flip();

    // allow intermeditate buffers to be collected before we create a possibly big array
    buffers = null;

    // is the data buffer backed by a byte array?
    if (data.hasArray()) {
      // just use the buffer's backing array since it's no longer needed and avoid the copy.
      return data.array();
    } else {
      // we need to copy the bytes into a byte array.
      final byte[] bytes = new byte[data.remaining()];
      data.get(bytes);
      return bytes;
    }
  }

  /**
   * Reads the bytes from the given file if it is a file and it exists.
   * @param file the file object
   * @return a byte array
   */
  public static byte[] readFileBytes(final File file) {
    byte[] data = null;
    if (file != null && file.exists() && file.isFile()) {
      RandomAccessFile raf = null;
      try {
        raf = new RandomAccessFile(file, "r");
        data = new byte[(int) raf.length()];
        raf.readFully(data);
      } catch (IOException ex) {
        ex.printStackTrace();
      } finally {
        if (raf != null) {
          try { raf.close(); } catch (Exception ex) {
            ex.printStackTrace(System.err);
          } 
        } 
      }
    }
    return data;
  }
}
