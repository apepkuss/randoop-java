package test.java.randoop.test;

/**
 * Created by bjkeller on 4/15/16.
 * Inspired by a bug report from Martin Schaef
 */
public class ClassWithInnerClass {

  public class A {
    public String s;
    public int i;
  }

  public void foo(A a) {
    A b = a;
    a.i = 7;
    bar(b);
  }

  private void bar(A a) {
    a.toString();
  }

}
