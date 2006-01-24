package org.zmpp.swingui;

public interface LineEditor {

  void setInputMode(boolean flag);
  void cancelInput();
  short nextZsciiChar();
  boolean isInputMode();
}
