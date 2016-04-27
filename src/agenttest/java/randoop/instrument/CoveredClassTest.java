package test.java.randoop.instrument;

import main.java.randoop.instrument.ExercisedClassVisitor;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import main.java.randoop.contract.ObjectContract;
import main.java.randoop.generation.ComponentManager;
import main.java.randoop.generation.ForwardGenerator;
import main.java.randoop.generation.RandoopListenerManager;
import main.java.randoop.generation.SeedSequences;
import main.java.randoop.main.ClassNameErrorHandler;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.main.GenTests;
import main.java.randoop.main.ThrowClassNameError;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.operation.OperationParseException;
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
import main.java.randoop.util.predicate.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static main.java.randoop.main.GenInputsAbstract.include_if_classname_appears;
import static main.java.randoop.main.GenInputsAbstract.methodlist;

/**
 *
 */
public class CoveredClassTest {

  @Test
  public void testNoFilter() {
    System.out.println("no filter");
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised = null;
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", rTests.size() > 0);
    assertFalse("don't expect error tests", eTests.size() > 0);

    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("randoop.instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    for (ExecutableSequence e : rTests) {
      assertFalse("should not cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  @Test
  public void testNameFilter() {
    System.out.println("name filter");
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    include_if_classname_appears =
        Pattern.compile("randoop\\.instrument\\.testcase\\.A"); //null;
    GenInputsAbstract.include_if_class_exercised =
        null; //"tests/randoop/instrument/testcase/coveredclasses.txt";
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should be no regression tests", rTests.size() == 0);
    assertFalse("should be no error tests", eTests.size() > 0);

    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("randoop.instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    for (ExecutableSequence e : rTests) {
      assertFalse("should not cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  @Test
  public void testCoverageFilter() {
    System.out.println("coverage filter");
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised =
        new File("randoop/instrument/testcase/coveredclasses.txt");
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", rTests.size() > 0);
    assertFalse("don't expect error tests", eTests.size() > 0);

    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("randoop.instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    for (ExecutableSequence e : rTests) {
      assertTrue("should cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  private ForwardGenerator getGenerator() {
    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames = GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.include_if_class_exercised,"unable to read coverage class names");
    Set<String> omitFields =
            GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.omit_field_list, "Error reading field file");
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate =
        new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitFields);
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    Set<String> methodSignatures =
            GenInputsAbstract.getStringSetFromFile(methodlist, "Error while reading method list file");

    OperationModel operationModel = null;
    try {
      operationModel=
              OperationModel.createModel(visibility, reflectionPredicate,
                      classnames,
                      coveredClassnames,
                      methodSignatures,
                      classNameErrorHandler,
                      GenInputsAbstract.literals_file);
    } catch (OperationParseException e) {
      fail("operation parse exception thrown: " + e);
    } catch (NoSuchMethodException e) {
      fail("Method not found: " + e);
    } catch (RandoopTypeException e) {
      fail("Type error: " + e);
    }
    assert operationModel != null;

    List<ConcreteOperation> model = operationModel.getConcreteOperations();
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
            componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    Set<String> observerSignatures = GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.observers,"Unable to read observer file", "//.*", null);

    MultiMap<ConcreteType,ConcreteOperation> observerMap = null;
    try {
      observerMap = operationModel.getObservers(observerSignatures);
    } catch (OperationParseException e) {
      System.out.printf("Error: parse exception thrown while reading observers: %s%n", e);
      System.exit(1);
    }
    assert observerMap != null;
    Set<ConcreteOperation> observers = new LinkedHashSet<>();
    for (ConcreteType keyType : observerMap.keySet()) {
      observers.addAll(observerMap.getValues(keyType));
    }

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
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
      fail("type error: " + e.getMessage());
    }
    assert objectConstructor != null : "object constructor is null";

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet, operationModel.getExercisedClasses(), include_if_classname_appears);
    testGenerator.addTestPredicate(isOutputTest);

    Set<ObjectContract> contracts = operationModel.getContracts();
    Set<ConcreteOperation> excludeAsObservers = new LinkedHashSet<>();
    TestCheckGenerator checkGenerator = genTests.createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(new ExercisedClassVisitor(operationModel.getExercisedClasses()));
    return testGenerator;
  }
}
