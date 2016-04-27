package main.java.randoop.test;

import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.util.predicate.DefaultPredicate;

public class ErrorTestPredicate extends DefaultPredicate<ExecutableSequence> {

  @Override
  public boolean test(ExecutableSequence s) {
    return s.hasFailure();
  }
}
