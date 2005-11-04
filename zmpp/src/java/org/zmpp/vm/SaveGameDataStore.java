package org.zmpp.vm;

import org.zmpp.iff.WritableFormChunk;

public interface SaveGameDataStore {

  boolean saveFormChunk(WritableFormChunk formchunk);
}
