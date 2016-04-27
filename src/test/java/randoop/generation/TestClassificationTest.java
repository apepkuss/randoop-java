package test.java.randoop.generation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import main.java.randoop.DummyVisitor;
import main.java.randoop.contract.ObjectContract;
import main.java.randoop.generation.ForwardGenerator;
import main.java.randoop.generation.ComponentManager;
import main.java.randoop.generation.RandoopListenerManager;
import main.java.randoop.generation.SeedSequences;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.main.GenInputsAbstract.BehaviorType;
import main.java.randoop.main.GenTests;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.ModelCollections;
import main.java.randoop.reflection.OperationExtractor;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionManager;
import main.java.randoop.reflection.ReflectionPredicate;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.reflection.VisibilityPredicate;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.test.Check;
import main.java.randoop.test.EmptyExceptionCheck;
import main.java.randoop.test.ExceptionCheck;
import main.java.randoop.test.ExpectedExceptionCheck;
import main.java.randoop.test.NoExceptionCheck;
import main.java.randoop.test.TestCheckGenerator;
import main.java.randoop.test.TestChecks;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.util.MultiMap;
import main.java.randoop.util.predicate.AlwaysTrue;
import main.java.randoop.util.predicate.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the classification of tests based on exception behavior assignments.
 * So, question is where exceptions are placed.
 */
public class TestClassificationTest {

  /**
   * Tests the classification of tests when all exceptions are invalid.
   * Because of class will have no error tests, and regression tests
   * should have no exceptions.
   */
  @Test
  public void allInvalidTest() {
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.INVALID;
    GenInputsAbstract.unchecked_exception = BehaviorType.INVALID;
    GenInputsAbstract.npe_on_null_input = BehaviorType.INVALID;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.INVALID;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);

    for (ExecutableSequence s : rTests) {
      TestChecks cks = s.getChecks();
      assertTrue("if sequence here should have checks", cks.hasChecks());
      assertFalse("these are not error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        String msg = "all exceptions are invalid, regression checks should be null;\n have ";
        fail(msg + eck.getClass().getName() + " with " + eck.getExceptionName());
      }
    }

    assertEquals("when all exceptions invalid, have no error tests", 0, eTests.size());
  }

  /**
   * Tests the classification of tests when all exceptions are errors.
   * All exceptions should show as NoExceptionCheck, and should be no
   * expected exceptions in regression tests.
   */
  @Test
  public void allErrorTest() {
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.ERROR;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.ERROR;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);

    for (ExecutableSequence s : rTests) {
      TestChecks cks = s.getChecks();
      assertFalse("these are not error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        String msg = "all exceptions error, should have no expected;\n have ";
        fail(msg + eck.getClass().getName() + " with " + eck.getExceptionName());
      }
    }

    assertTrue("should have some error tests", eTests.size() > 0);

    for (ExecutableSequence s : eTests) {
      TestChecks cks = s.getChecks();
      assertTrue("if sequence here should have checks", cks.hasChecks());
      assertTrue("these are error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      int exceptionCount = 0;
      for (Check ck : cks.get().keySet()) {
        if (ck instanceof NoExceptionCheck) {
          exceptionCount++;
        }
      }
      assertTrue("exception count should be one, have " + exceptionCount, exceptionCount == 1);
    }
  }

  /**
   * Tests classification of tests when all exceptions are expected.
   * All exceptions should show as expected exception checks, and
   * there should be no error tests.
   */
  @Test
  public void allExpectedTest() {
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.oom_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);

    for (ExecutableSequence s : rTests) {
      TestChecks cks = s.getChecks();
      assertFalse("these are not error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        assertTrue("if there is an exception check, should be checks", cks.hasChecks());
        assertTrue(
            "should be expected exception, was" + eck.getClass().getName(),
            eck instanceof ExpectedExceptionCheck);
      }
    }

    assertEquals("all exceptions expected, should be no error tests", 0, eTests.size());
  }

  /**
   * Tests classification of tests when behavior type defaults are set
   * (checked and unchecked exceptions are expected, and both NPE-on-null
   * and OOM are invalid).
   * Because class throws NPE without input, should see NPE as expected when no
   * null inputs. Otherwise, should not see NPE.
   */
  @Test
  public void defaultsTest() {
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);

    for (ExecutableSequence s : rTests) {
      TestChecks cks = s.getChecks();
      assertFalse("these are not error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        assertTrue("if there is an exception check, should be checks", cks.hasChecks());
        assertTrue(
            "should be expected exception, was" + eck.getClass().getName(),
            eck instanceof ExpectedExceptionCheck);
      }
    }

    assertTrue("should have error tests", eTests.size() > 0);

    for (ExecutableSequence s : eTests) {
      TestChecks cks = s.getChecks();
      assertTrue("if sequence here should have checks", cks.hasChecks());
      assertTrue("these are error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      int exceptionCount = 0;
      for (Check ck : cks.get().keySet()) {
        if (ck instanceof NoExceptionCheck) {
          exceptionCount++;
        }
      }
      assertTrue("exception count should be one, have " + exceptionCount, exceptionCount == 1);
    }
  }

  /**
   * Tests default behaviors with regression assertions turned off.
   * Means that because class throws NPE without input, should see NPE as
   * empty exception when there are no null inputs.
   * Otherwise, should not see NPE, or any other checks.
   */
  @Test
  public void defaultsWithNoRegressionAssertions() {
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_regression_assertions = true;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);

    for (ExecutableSequence s : rTests) {
      TestChecks cks = s.getChecks();
      assertFalse("these are not error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        assertTrue("if there is an exception check, should be checks", cks.hasChecks());
        assertTrue(
            "should be expected exception, was" + eck.getClass().getName(),
            eck instanceof EmptyExceptionCheck);
      } else {
        assertFalse("if there is no exception check, should be no checks", cks.hasChecks());
      }
    }

    assertTrue("should have error tests", eTests.size() > 0);

    for (ExecutableSequence s : eTests) {
      TestChecks cks = s.getChecks();
      assertTrue("if sequence here should have checks", cks.hasChecks());
      assertTrue("these are error checks", cks.hasErrorBehavior());
      assertFalse("these are not invalid checks", cks.hasInvalidBehavior());

      int exceptionCount = 0;
      for (Check ck : cks.get().keySet()) {
        if (ck instanceof NoExceptionCheck) {
          exceptionCount++;
        }
      }
      assertTrue("exception count should be one, have " + exceptionCount, exceptionCount == 1);
    }
  }

  private ForwardGenerator buildGenerator(Class<?> c) {

    Set<String> omitfields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate predicate =
        new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitfields);
    final List<ConcreteOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager manager = new ReflectionManager(visibility);
    manager.add(new OperationExtractor(operationManager, predicate));
    manager.apply(c);
    Collection<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    ComponentManager componentMgr = new ComponentManager(components);
    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator gen =
        new ForwardGenerator(
            model,
            new LinkedHashSet<ConcreteOperation>(),
            GenInputsAbstract.timelimit * 1000,
            GenInputsAbstract.inputlimit,
            GenInputsAbstract.outputlimit,
            componentMgr,
            null,
            listenerMgr);
    Predicate<ExecutableSequence> isOutputTest = new AlwaysTrue<>();
    gen.addTestPredicate(isOutputTest);
    TestCheckGenerator checkGenerator =
        (new GenTests()).createTestCheckGenerator(visibility, new LinkedHashSet<ObjectContract>(), new MultiMap<ConcreteType, ConcreteOperation>(), new LinkedHashSet<ConcreteOperation>());
    gen.addTestCheckGenerator(checkGenerator);
    gen.addExecutionVisitor(new DummyVisitor());
    return gen;
  }
}
