package test.java.randoop.test;

import main.java.randoop.ExceptionalExecution;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.test.predicate.ExceptionPredicate;

//Dummy failure predicate -- everything fails! --- for tests
public class DummyFailurePredicate implements ExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }
}
