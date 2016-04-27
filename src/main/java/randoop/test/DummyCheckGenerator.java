package main.java.randoop.test;

import main.java.randoop.sequence.ExecutableSequence;

/**
 * Returns an empty TestChecks.
 */
public class DummyCheckGenerator implements TestCheckGenerator {

  @Override
  public TestChecks visit(ExecutableSequence s) {
    return new RegressionChecks();
  }
}
