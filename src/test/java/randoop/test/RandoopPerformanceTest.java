package test.java.randoop.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import plume.EntryReader;
import main.java.randoop.generation.ForwardGenerator;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.ModelCollections;
import main.java.randoop.reflection.OperationExtractor;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionManager;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.types.ConcreteType;

public class RandoopPerformanceTest extends AbstractPerformanceTest {

  @Override
  void execute() {
    String resourcename = "java.util.classlist.java1.6.txt";

    List<Class<?>> classes = new ArrayList<>();
    try (EntryReader er = new EntryReader(ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename))) {
      for (String entry : er) {
        classes.add(Class.forName(entry));
      }
    } catch (IOException e) {
      fail("exception while reading class names");
    } catch (ClassNotFoundException e) {
      fail("couldn't load class");
    }

    List<ConcreteOperation> model = getConcreteOperations(classes);
    assertFalse("model should not be empty", model.isEmpty());
    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    GenInputsAbstract.debug_checks = false;
    ForwardGenerator explorer =
        new ForwardGenerator(model, new LinkedHashSet<ConcreteOperation>(), Long.MAX_VALUE, 100000, 100000, null, null, null);
    explorer.explore();
  }

  @Override
  int expectedTimeMillis() {
    return 10000;
  }


  private static List<ConcreteOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<ConcreteOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    mgr.add(new OperationExtractor(operationManager, new DefaultReflectionPredicate()));
    for (Class<?> c: classes) {
      mgr.apply(c);
    }
    return model;
  }
}
