package main.java.randoop.test;

import java.util.Set;

import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.util.predicate.DefaultPredicate;

public class ExcludeTestPredicate extends DefaultPredicate<ExecutableSequence> {

  private Set<Sequence> excludeSet;

  public ExcludeTestPredicate(Set<Sequence> excludeSet) {
    this.excludeSet = excludeSet;
  }

  @Override
  public boolean test(ExecutableSequence s) {
    return !excludeSet.contains(s.sequence);
  }
}
