package main.java.randoop.test;

import main.java.randoop.test.ExceptionCheck;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.util.TimeoutExceededException;
import main.java.randoop.util.predicate.DefaultPredicate;

/**
 * {@code RegressionTestPredicate} determines whether to keep an
 * {@code ExecutableSequence} as a regression test.
 */
public class RegressionTestPredicate extends DefaultPredicate<ExecutableSequence> {

  /**
   * Determines whether an executable sequence is a valid regression test. In
   * particular, shouldn't have failures (an error-revealing test), and
   * shouldn't have {@link randoop.util.TimeoutExceededException
   * TimeoutExceededException}.
   *
   * @return true if has no failures and does not involve a timeout exception,
   *         false otherwise.
   */
  @Override
  public boolean test(ExecutableSequence s) {
    // don't want error revealing test
    if (s.hasFailure()) {
      return false;
    }

    TestChecks testChecks = s.getChecks();

    // if have exception
    ExceptionCheck ec = testChecks.getExceptionCheck();
    if (ec != null) {
      // Remove any sequences that throw randoop.util.TimeoutExceededException.
      // It would be nicer for Randoop to output a test suite that detects
      // long-running tests and generates a TimeoutExceededException, as
      // documented in Issue 11:
      // https://github.com/randoop/randoop/issues/11 .
      if (ec.getException() instanceof TimeoutExceededException) {
        return false;
      }
    }

    return true;
  }
}
