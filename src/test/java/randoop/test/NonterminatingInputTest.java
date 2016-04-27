package test.java.randoop.test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import main.java.randoop.DummyVisitor;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.operation.ConstructorCall;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.sequence.Variable;
import main.java.randoop.test.DummyCheckGenerator;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.RandoopTypeException;
import main.java.randoop.util.ReflectionExecutor;
import main.java.randoop.util.TimeoutExceededException;

import junit.framework.TestCase;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NonterminatingInputTest {

  @Test
  public void test() throws SecurityException, NoSuchMethodException {

    Sequence s = new Sequence();
    ConcreteOperation con = null;
    try {
      con = createConstructorCall(Looper.class.getConstructor());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    s = s.extend(con, new ArrayList<Variable>());
    int oldTimeout = ReflectionExecutor.timeout;
    ReflectionExecutor.timeout = 500;
    ExecutableSequence es = new ExecutableSequence(s);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    ReflectionExecutor.timeout = oldTimeout;
    assertTrue(es.throwsException(TimeoutExceededException.class));
  }

  public static class Looper {
    public Looper() {
      while (true) {
        // loop.
      }
    }
  }

  private ConcreteOperation createConstructorCall(Constructor<?> con) throws RandoopTypeException {
    ConstructorCall op = new ConstructorCall(con);
    ConcreteType declaringType = ConcreteType.forClass(con.getDeclaringClass());
    List<ConcreteType> paramTypes = new ArrayList<>();
    for (Class<?> pc : con.getParameterTypes()) {
      paramTypes.add(ConcreteType.forClass(pc));
    }
    return new ConcreteOperation(op, declaringType, new ConcreteTypeTuple(paramTypes), declaringType);
  }
}
