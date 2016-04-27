package test.java.randoop.operation;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;


import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.ModelCollections;
import main.java.randoop.reflection.OperationExtractor;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionManager;
import main.java.randoop.reflection.ReflectionPredicate;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.reflection.VisibilityPredicate;
import test.java.randoop.test.AnIntegerPredicate;
import test.java.randoop.test.ClassWithInnerClass;
import main.java.randoop.types.ConcreteType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by bjkeller on 4/14/16.
 */
public class ClassReflectionTest {

  @Test
  public void implementsParameterizedTypeTest() {
    Class<?> c = AnIntegerPredicate.class;
    Set<ConcreteOperation> actual = getConcreteOperations(c);

    // TODO be sure the types of the inherited method has the proper type arguments
    assertEquals("number of operations", 4, actual.size());
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    final Set<ConcreteOperation> operations = new LinkedHashSet<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        operations.add(operation);
      }
    });
    OperationExtractor extractor = new OperationExtractor(operationManager, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }

  @Test
  public void innerClassTest() {
    Class<?> outer = ClassWithInnerClass.class;
    Class<?> inner = null;
    try {
      inner = Class.forName("randoop.test.ClassWithInnerClass$A");
    } catch (ClassNotFoundException e) {
      fail("could not load inner class" + e.getMessage());
    }

    Set<ConcreteOperation> innerActual = getConcreteOperations(inner);

    for (ConcreteOperation op : innerActual) {
      System.out.println(op);
    }
    assertEquals("number of inner class operations", 5, innerActual.size());

    Set<ConcreteOperation> outerActual = getConcreteOperations(outer);
    for(ConcreteOperation op : outerActual) {
      System.out.println(op);
    }
    assertEquals("number of outer operations", 2, outerActual.size());

    // TODO be more sophisticated in checking operations
  }
}
