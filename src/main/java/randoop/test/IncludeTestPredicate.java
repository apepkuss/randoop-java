package main.java.randoop.test;

import java.util.regex.Pattern;

import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.util.predicate.DefaultPredicate;

/**
 * A test predicate that checks for the occurrence of variables that match the
 * given regular expression.
 */
public class IncludeTestPredicate extends DefaultPredicate<ExecutableSequence> {

  private Pattern testClasses;

  public IncludeTestPredicate(Pattern testClasses) {
    this.testClasses = testClasses;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the sequence uses a member of a class that matches the
   *         regular expression, false otherwise
   */
  @Override
  public boolean test(ExecutableSequence s) {
    return s.sequence.hasUseOfMatchingClass(testClasses);
  }
}
