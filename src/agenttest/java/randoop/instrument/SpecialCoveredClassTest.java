package agenttest.java.randoop.instrument;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import main.java.randoop.contract.ObjectContract;
import main.java.randoop.generation.ComponentManager;
import main.java.randoop.generation.ForwardGenerator;
import main.java.randoop.generation.RandoopListenerManager;
import main.java.randoop.generation.SeedSequences;
import main.java.randoop.instrument.ExercisedClassVisitor;
import main.java.randoop.main.ClassNameErrorHandler;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.main.GenTests;
import main.java.randoop.main.ThrowClassNameError;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.OperationModel;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionPredicate;
import main.java.randoop.reflection.VisibilityPredicate;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.test.TestCheckGenerator;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.RandoopTypeException;
import main.java.randoop.types.TypeNames;
import main.java.randoop.util.MultiMap;
import main.java.randoop.util.ReflectionExecutor;
import main.java.randoop.util.predicate.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static main.java.randoop.main.GenInputsAbstract.include_if_class_exercised;
import static main.java.randoop.main.GenInputsAbstract.include_if_classname_appears;
import static main.java.randoop.main.GenInputsAbstract.methodlist;
import static main.java.randoop.main.GenInputsAbstract.omitmethods;

/**
 * Test special cases of "covered" (or exercised) class filtering.
 * Want to ensure behaves well when given abstract class and interface.
 */
public class SpecialCoveredClassTest {

  @Test
  public void abstractClassTest() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/special-allclasses.txt");
    include_if_class_exercised =
        new File("randoop/instrument/testcase/special-coveredclasses.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;

    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames =
            GenInputsAbstract.getStringSetFromFile(
                    include_if_class_exercised, "Unable to read coverage class names");
    Set<String> omitFields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate =
            new DefaultReflectionPredicate(omitmethods, omitFields);
    Set<String> methodSignatures =
            GenInputsAbstract.getStringSetFromFile(methodlist, "Error while reading method list file");
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    OperationModel operationModel = null;
    try {
      operationModel=
              OperationModel.createModel(
                      visibility,
                      reflectionPredicate,
                      classnames,
                      coveredClassnames,
                      methodSignatures,
                      classNameErrorHandler,
                      GenInputsAbstract.literals_file);
    } catch (Throwable e) {
     fail("Error: " + e);
    }
    assert operationModel != null;

    Set<Class<?>> coveredClasses = operationModel.getExercisedClasses();
    Set<ConcreteType> classes = operationModel.getClasses();
    //
    assertTrue("should be one covered classes", coveredClasses.size() == 1);
    for (Class<?> c : coveredClasses) {
      assertEquals(
          "name should be AbstractTarget",
          "randoop.instrument.testcase.AbstractTarget",
          c.getName());
    }

    // 2 classes plus Object
    assertEquals("should have classes", 3, classes.size());
    for (ConcreteType c : classes) {
      assertTrue("should not be interface: " + c.getName(), !c.isInterface());
    }
    //
    List<ConcreteOperation> model = operationModel.getConcreteOperations();
    //
    assertEquals("model operations", 6, model.size());
    //
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
            componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    Set<ConcreteOperation> observers = new LinkedHashSet<>();
    ForwardGenerator testGenerator =
            new ForwardGenerator(
                    model,
                    observers,
                    GenInputsAbstract.timelimit * 1000,
                    GenInputsAbstract.inputlimit,
                    GenInputsAbstract.outputlimit,
                    componentMgr,
                    listenerMgr);
    GenTests genTests = new GenTests();

    ConcreteOperation objectConstructor = null;
    try {
      objectConstructor = operationModel.getConcreteOperation(Object.class.getConstructor());
    } catch (NoSuchMethodException e) {
      assert false : "failed to get Object constructor: " + e;
    } catch (RandoopTypeException e) {
      fail("Type error " + e.getMessage());
    }
    assert objectConstructor != null;

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    Predicate<ExecutableSequence> isOutputTest =
            genTests.createTestOutputPredicate(
                    excludeSet, operationModel.getExercisedClasses(), include_if_classname_appears);
    testGenerator.addTestPredicate(isOutputTest);
    Set<ObjectContract> contracts = operationModel.getContracts();
    Set<ConcreteOperation> excludeAsObservers = new LinkedHashSet<>();
    MultiMap<ConcreteType,ConcreteOperation> observerMap = new MultiMap<>();
    TestCheckGenerator checkGenerator = genTests.createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(new ExercisedClassVisitor(coveredClasses));
    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();
    //
    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", rTests.size() > 0);
    assertFalse("don't expect error tests", eTests.size() > 0);
    //
    Class<?> at = null;
    try {
      at = TypeNames.getTypeForName("randoop.instrument.testcase.AbstractTarget");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Set<ConcreteOperation> opSet = new LinkedHashSet<>();
    for (ExecutableSequence e : rTests) {
      assertTrue("should cover the class: " + at.getName(), e.coversClass(at));
      for (int i = 0; i < e.sequence.size(); i++) {
        ConcreteOperation op = e.sequence.getStatement(i).getOperation();
        if (model.contains(op)) {
          opSet.add(op);
        }
      }
    }

    ConcreteType it = null;
    try {
      it = (ConcreteType)ConcreteType.forName("randoop.instrument.testcase.ImplementorOfTarget");
    } catch (ClassNotFoundException e) {
      fail("cannot find implementor class " + e);
    } catch (RandoopTypeException e) {
      fail("Type error " + e.getMessage());
    }
    for (ConcreteOperation op : model) {
      assertTrue(
          "all model operations should be used or from wrong implementor",
          opSet.contains(op) || op.getDeclaringType().equals(it));
    }
  }
}
