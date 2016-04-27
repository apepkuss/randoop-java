package test.java.randoop.test;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import main.java.randoop.generation.ComponentManager;
import main.java.randoop.generation.ForwardGenerator;
import main.java.randoop.generation.IStopper;
import main.java.randoop.generation.SeedSequences;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.ModelCollections;
import main.java.randoop.reflection.OperationExtractor;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionManager;
import main.java.randoop.reflection.TypedOperationManager;
import test.java.randoop.test.issta2006.BinTree;
import test.java.randoop.test.issta2006.BinomialHeap;
import test.java.randoop.test.issta2006.FibHeap;
import test.java.randoop.test.issta2006.TreeMap;
import main.java.randoop.test.DummyCheckGenerator;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.util.ReflectionExecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test ensures that Randoop achieves a certain level of coverage
 * across 4 data structures. The coverage level that we check of is the
 * one published in the ICSE 2007 paper "Feedback-directed Random Test
 * Generation" (Section 3.1).
 *
 * For each data structure, we expect Randoop to achieve the published
 * coverage in no longer than 2 minutes.
 *
 * Note that this test does not constitute the experiment published in
 * the paper; it only checks that the achievable coverage number can be in
 * fact achieved by Randoop.
 *
 * IMPORTANT: this test DOES NOT work if GenInputsAbstract.repeat_heuristic is
 * disabled. If the heuristic in {@link main.java.randoop.generation.ForwardGenerator ForwardGenerator}
 * is not used, the branch count targets are not met.
 */
public class ICSE07ContainersTest {

  private static void runRandoop(
          String name,
          List<Class<?>> classList,
          Pattern omitMethodPattern,
          IStopper stopper,
          Set<String> excludeNames) {

    System.out.println("ICSE 2006 container: " + name);

    final List<ConcreteOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    mgr.add(new OperationExtractor(operationManager, new DefaultReflectionPredicate(omitMethodPattern, excludeNames)));
    for (Class<?> c : classList) {
      mgr.apply(c);
    }
    assertTrue("model should not be empty", !model.isEmpty());
    System.out.println("Number of operations: " + model.size());

    ComponentManager componentMgr = new ComponentManager(SeedSequences.defaultSeeds());
    assertEquals(
        "Number of seed sequences should be same as default seeds",
        SeedSequences.defaultSeeds().size(),
        componentMgr.numGeneratedSequences());
    ForwardGenerator explorer =
        new ForwardGenerator(
            model,
                new LinkedHashSet<ConcreteOperation>(),
            120000 /* two minutes */,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            componentMgr,
            stopper,
            null);
    explorer.addTestCheckGenerator(new DummyCheckGenerator());
    GenInputsAbstract.maxsize = 10000; // Integer.MAX_VALUE;
    GenInputsAbstract.repeat_heuristic = true;
    ReflectionExecutor.usethreads = false;
    main.java.randoop.main.GenInputsAbstract.debug_checks = false;
    explorer.explore();
  }

  @Test
  public void testFibHeap() throws IOException {
    List<Class<?>> classList = new ArrayList<>();
    classList.add(FibHeap.class);
    FibHeap.rand.setSeed(0);
    main.java.randoop.util.Randomness.reset(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean stop() {
            return FibHeap.tests.size() >= 96;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    runRandoop(
        "FibHeap",
        classList,
        Pattern.compile(
            "decreaseKey|delete\\(randoop.test.issta2006.Node\\)|empty\\(\\)|insert\\(randoop.test.issta2006.Node\\)|min\\(\\)|size\\(\\)|union"),
        stopper,
        excludeNames);
    assertEquals(96, FibHeap.tests.size());
  }

  @Test
  public void testBinTree() {
    List<Class<?>> classList = new ArrayList<>();
    classList.add(BinTree.class);
    main.java.randoop.util.Randomness.reset(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean stop() {
            return BinTree.tests.size() >= 54;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    runRandoop(
        "BinTree", classList, Pattern.compile("find\\(int\\)|gen_native"), stopper, excludeNames);
    assertEquals(54, BinTree.tests.size());
  }

  @Test
  public void testTreeMap() {
    List<Class<?>> classList = new ArrayList<>();
    classList.add(TreeMap.class);
    main.java.randoop.util.Randomness.reset(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean stop() {
            return TreeMap.tests.size() >= 106;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    runRandoop(
        "TreeMap",
        classList,
        Pattern.compile(
            "toString\\(\\)|size\\(\\)|containsKey\\(int\\)|print\\(\\)|concreteString\\(int\\)"),
        stopper,
        excludeNames);
    assertEquals(106, TreeMap.tests.size());
  }

  @Test
  public void testBinomialHeap() {
    List<Class<?>> classList = new ArrayList<>();
    classList.add(BinomialHeap.class);
    main.java.randoop.util.Randomness.reset(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean stop() {
            return BinomialHeap.tests.size() >= 101;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    runRandoop("BinomialHeap", classList, Pattern.compile("findMinimum\\(\\)"), stopper, excludeNames);
    assertEquals(101, test.java.randoop.test.issta2006.BinomialHeap.tests.size());
  }
}
