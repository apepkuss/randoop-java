package main.java.randoop.instrument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import main.java.randoop.ExecutionVisitor;
import main.java.randoop.sequence.ExecutableSequence;

/**
 * A {@link ExecutionVisitor} that polls a set of coverage instrumented classes
 * and adds each covered class to an {@link ExecutableSequence} after it is
 * executed.
 */
public class ExercisedClassVisitor implements ExecutionVisitor {

  /** The classes to be polled. */
  private Set<Class<?>> classes;

  /**
   * Creates a visitor to poll the given classes for coverage by sequence
   * executions.
   *
   * @param classes
   *          the set of classes to poll for coverage by a sequence
   */
  public ExercisedClassVisitor(Set<Class<?>> classes) {
    this.classes = classes;
  }

  /**
   * {@inheritDoc} Registers each class covered with the sequence execution
   * results.
   */
  @Override
  public void visitAfterSequence(ExecutableSequence executableSequence) {
    for (Class<?> c : classes) {
      if (checkAndReset(c)) {
        executableSequence.addCoveredClass(c);
      }
    }
  }

  /**
   * Calls the coverage instrumentation method.
   *
   * @param c
   *          the class for which method is to be called
   * @return true if the instrumentation method is true, false otherwise
   */
  private boolean checkAndReset(Class<?> c) {
    try {
      Method m = c.getMethod("randoop_checkAndReset", new Class<?>[0]);
      m.setAccessible(true);
      return (boolean) m.invoke(null, new Object[0]);
    } catch (NoSuchMethodException e) {
      throw new Error("Cannot find instrumentation method: " + e);
    } catch (SecurityException e) {
      throw new Error("Security error when accessing instrumentation method: " + e);
    } catch (IllegalAccessException e) {
      throw new Error("Cannot access instrumentation method: " + e);
    } catch (IllegalArgumentException e) {
      throw new Error("Bad argument to instrumentation method: " + e);
    } catch (InvocationTargetException e) {
      throw new Error("Bad invocation of instrumentation method: " + e);
    }
  }

  // unimplemented visitor methods
  @Override
  public void visitBeforeStatement(ExecutableSequence sequence, int i) {
    // Not doing anything before
  }

  @Override
  public void visitAfterStatement(ExecutableSequence sequence, int i) {
    // Not doing anything after
  }

  @Override
  public void initialize(ExecutableSequence executableSequence) {
    // No initialization
  }
}
