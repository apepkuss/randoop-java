package main.java.randoop.test.predicate;

import main.java.randoop.ExceptionalExecution;
import main.java.randoop.sequence.ExecutableSequence;

/**
 * An {@code ExceptionPredicate} that always returns false. Used to indicate
 * that no exceptions belong to a behavior type.
 */
public class AlwaysFalseExceptionPredicate implements ExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }
}
