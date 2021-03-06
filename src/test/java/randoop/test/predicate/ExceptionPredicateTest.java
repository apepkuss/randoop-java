package test.java.randoop.test.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import main.java.randoop.DummyVisitor;
import main.java.randoop.ExceptionalExecution;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.main.GenInputsAbstract.BehaviorType;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.operation.ConstructorCall;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.sequence.Variable;
import main.java.randoop.test.DummyCheckGenerator;
import main.java.randoop.test.predicate.AlwaysFalseExceptionPredicate;
import main.java.randoop.test.predicate.ExceptionBehaviorPredicate;
import main.java.randoop.test.predicate.ExceptionPredicate;
import main.java.randoop.types.ConcreteSimpleType;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.ConcreteTypes;

/**
 * Tests to check whether exception predicates are acting as expected.
 *
 */
public class ExceptionPredicateTest {

  /*
   * Make sure that command-line arguments are set in expected way.
   */
  @BeforeClass
  public static void setupBeforeClass() {
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
  }

  /*
   * Predicates for behavior types
   */
  private ExceptionPredicate isInvalid;
  private ExceptionPredicate isError;
  private ExceptionPredicate isExpected;
  private ExceptionPredicate alwaysFalse;

  @Before
  public void setupBefore() {
    this.isError = new ExceptionBehaviorPredicate(BehaviorType.ERROR);
    this.isInvalid = new ExceptionBehaviorPredicate(BehaviorType.INVALID);
    this.isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
    this.alwaysFalse = new AlwaysFalseExceptionPredicate();
  }

  /*
   * Faked occurrence of NullPointerException should not satisfy the
   * NPEContractPredicate, which is looking for an NPE when null is input.
   */
  @Test
  public void testNoNullNPE() {
    ExceptionalExecution exec = new ExceptionalExecution(new NullPointerException(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertTrue("non-null input NPE is error", isError.test(exec, s));
    assertFalse("non-null input NPE is not invalid", isInvalid.test(exec, s));
    assertFalse("non-null input NPE is expected", isExpected.test(exec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(exec, s));
  }

  @Test
  public void testNullNPE() {
    ExceptionalExecution exec = new ExceptionalExecution(new NullPointerException(), 0);
    Class<?> c = CUTForExceptionPredicate.class;
    ConcreteType classType = new ConcreteSimpleType(c);
    Constructor<?> con = null;
    try {
      con = c.getDeclaredConstructor(Object.class);
    } catch (Exception e) {
      fail("test not setup correctly: " + e);
    }
    List<ConcreteType> paramTypes = new ArrayList<>();
    paramTypes.add(ConcreteTypes.OBJECT_TYPE);
    ConcreteOperation conOp = new ConcreteOperation(new ConstructorCall(con), classType, new ConcreteTypeTuple(paramTypes), classType);
    Sequence seq = new Sequence().extend(ConcreteOperation.createNullOrZeroInitializationForType(ConcreteTypes.OBJECT_TYPE));
    List<Variable> inputVariables = new ArrayList<>();
    inputVariables.add(new Variable(seq, 0));

    seq = seq.extend(conOp, inputVariables);
    ExecutableSequence s = new ExecutableSequence(seq);
    s.execute(new DummyVisitor(), new DummyCheckGenerator());

    assertFalse("null input NPE is not error", isError.test(exec, s));
    assertFalse("null input NPE is not invalid", isInvalid.test(exec, s));
    assertTrue("null input NPE is expected", isExpected.test(exec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(exec, s));
  }

  @Test
  public void testOOM() {
    ExceptionalExecution exec = new ExceptionalExecution(new OutOfMemoryError(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertFalse("OOM is not error", isError.test(exec, s));
    assertTrue("OOM is invalid", isInvalid.test(exec, s));
    assertFalse("OOM is not expected", isExpected.test(exec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(exec, s));
  }

  @Test
  public void testFailures() {
    ExceptionalExecution assertionExec = new ExceptionalExecution(new AssertionError(), 0);
    ExceptionalExecution overflowExec = new ExceptionalExecution(new StackOverflowError(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertTrue("AE is error", isError.test(assertionExec, s));
    assertFalse("AE is not invalid", isInvalid.test(assertionExec, s));
    assertFalse("AE is not expected", isExpected.test(assertionExec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(assertionExec, s));

    assertTrue("SOE is error", isError.test(overflowExec, s));
    assertFalse("SOE is not invalid", isInvalid.test(overflowExec, s));
    assertFalse("SOE is not expected", isExpected.test(overflowExec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(overflowExec, s));
  }
}
