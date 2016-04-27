package main.java.randoop.main;

import java.io.IOException;

import main.java.randoop.util.Files;

/**
 * Counts lines in multiple files. Prints the sum.
 */
public final class CountLines {
  private CountLines() {
    throw new IllegalStateException("no instances");
  }

  public static void main(String[] args) throws IOException {
    int acc = 0;
    for (String arg : args) {
      acc += Files.readWhole(arg).size();
    }
    System.out.println(acc);
  }
}
