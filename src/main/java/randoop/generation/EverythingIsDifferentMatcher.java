package main.java.randoop.generation;

public class EverythingIsDifferentMatcher implements StateMatcher {

  int size = 0;

  @Override
  public boolean add(Object object) {
    size++;
    return true;
  }

  @Override
  public int size() {
    return size;
  }
}
