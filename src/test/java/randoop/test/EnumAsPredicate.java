package test.java.randoop.test;

import main.java.randoop.util.predicate.Predicate;

/**
 * Created by bjkeller on 4/14/16.
 *
 * inspired by Google javascript code in Defects4J Closure 123f
 */
public enum EnumAsPredicate implements Predicate<Integer> {
  ONE {
    @Override
    public boolean test(Integer integer) {
      return false;
    }

  },
  TWO {
    @Override
    public boolean test(Integer integer) {
      return false;
    }
  };

  @Override
  public Predicate<Integer> or(Predicate<Integer> p) {
    return this;
  }

  @Override
  public Predicate<Integer> and(Predicate<Integer> q) {
    return this;
  }
};
