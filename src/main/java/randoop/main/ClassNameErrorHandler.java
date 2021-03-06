package main.java.randoop.main;

/**
 * A ClassNameErrorHandler determines the error behavior when a class name error
 * occurs.
 */
public interface ClassNameErrorHandler {

  /**
   * Performs error handling behavior for bad class name.
   *
   * @param className
   *          the name of the class for inclusion in messages.
   */
  void handle(String className);
}
