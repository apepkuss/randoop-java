package test.java.randoop.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import main.java.randoop.BugInRandoopException;
import main.java.randoop.contract.ObjectContract;
import main.java.randoop.generation.ComponentManager;
import main.java.randoop.generation.ForwardGenerator;
import main.java.randoop.generation.SeedSequences;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.main.GenTests;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.operation.ConstructorCall;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.ModelCollections;
import main.java.randoop.reflection.OperationExtractor;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionManager;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.sequence.Variable;
import test.java.randoop.test.bh.BH;
import test.java.randoop.test.bh.Body;
import test.java.randoop.test.bh.Cell;
import test.java.randoop.test.bh.MathVector;
import test.java.randoop.test.bh.Node;
import test.java.randoop.test.bh.Tree;
import main.java.randoop.test.TestCheckGenerator;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.ConcreteTypes;
import main.java.randoop.util.MultiMap;
import main.java.randoop.util.ReflectionExecutor;
import main.java.randoop.util.predicate.Predicate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static main.java.randoop.main.GenInputsAbstract.include_if_classname_appears;

public class ForwardExplorerTests {

  @Test
  public void test1() {
    List<Class<?>> classes = new ArrayList<>();
    classes.add(Long.class);

    final List<ConcreteOperation> model = getConcreteOperations(classes);

    assertTrue("model not empty", model.size() != 0);
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator explorer =
        new ForwardGenerator(model, new LinkedHashSet<ConcreteOperation>(), Long.MAX_VALUE, 5000, 5000, mgr, null, null);
    explorer.addTestCheckGenerator(createChecker(new LinkedHashSet<ObjectContract>()));
    explorer.addTestPredicate(createOutputTest());
    explorer.explore();
    GenInputsAbstract.dontexecute = false;
    assertTrue(explorer.numGeneratedSequences() != 0);
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

  @Test
  public void test2() throws Throwable {
    boolean bisort = false;
    boolean bimerge = false;
    boolean inorder = false;
    boolean swapleft = false;
    boolean swapright = false;
    boolean random = false;

    List<Class<?>> classes = new ArrayList<>();
    classes.add(test.java.randoop.test.BiSortVal.class);
    classes.add(BiSort.class);
    //GenFailures.noprogressdisplay = true;
    //Log.log = new FileWriter("templog.txt");
    int oldTimeout = ReflectionExecutor.timeout;
    ReflectionExecutor.timeout = 200;
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    final List<ConcreteOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    GenInputsAbstract.ignore_flaky_tests = true;
    ForwardGenerator exp = new ForwardGenerator(model, new LinkedHashSet<ConcreteOperation>(), Long.MAX_VALUE, 200, 200, mgr, null, null);
    exp.addTestCheckGenerator(createChecker(new LinkedHashSet<ObjectContract>()));
    exp.addTestPredicate(createOutputTest());
    try {
      exp.explore();
    } catch (Throwable t) {
      fail("Exception during generation: " + t);
    }
    ReflectionExecutor.timeout = oldTimeout;
    for (Sequence s : exp.getAllSequences()) {
      String str = s.toCodeString();
      if (str.contains("bisort")) bisort = true;
      if (str.contains("bimerge")) bimerge = true;
      if (str.contains("inOrder")) inorder = true;
      if (str.contains("swapValLeft")) swapleft = true;
      if (str.contains("swapValRight")) swapright = true;
      if (str.contains("random")) random = true;
    }

    assertTrue(bisort);
    assertTrue(bimerge);
    assertTrue(inorder);
    assertTrue(swapleft);
    assertTrue(swapright);
    assertTrue(random);
  }

  @Test
  public void test4() throws Exception {

    boolean bh = false;
    boolean body = false;
    boolean cell = false;
    boolean mathvector = false;
    boolean node = false;
    boolean tree = false;

    List<Class<?>> classes = new ArrayList<>();
    classes.add(BH.class);
    classes.add(Body.class);
    classes.add(Cell.class);
    classes.add(MathVector.class);
    classes.add(Node.class);
    classes.add(Tree.class);

    System.out.println(classes);
    GenInputsAbstract.ignore_flaky_tests = true;
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    final List<ConcreteOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator exp = new ForwardGenerator(model, new LinkedHashSet<ConcreteOperation>(), Long.MAX_VALUE, 200, 200, mgr, null, null);
    GenInputsAbstract.forbid_null = false;
    exp.addTestCheckGenerator(createChecker(new LinkedHashSet<ObjectContract>()));
    exp.addTestPredicate(createOutputTest());
    try {
      exp.explore();
    } catch (Throwable t) {
      fail("Exception during generation: " + t);
    }
    for (Sequence s : exp.getAllSequences()) {
      String str = s.toCodeString();
      if (str.contains("BH")) bh = true;
      if (str.contains("Body")) body = true;
      if (str.contains("Cell")) cell = true;
      if (str.contains("MathVector")) mathvector = true;
      if (str.contains("Node")) node = true;
      if (str.contains("Tree")) tree = true;
    }
    assertTrue(bh);
    assertTrue(body);
    assertTrue(cell);
    assertTrue(mathvector);
    assertTrue(node);
    assertTrue(tree);
  }

  private static TestCheckGenerator createChecker(Set<ObjectContract> contracts) {
    return (new GenTests()).createTestCheckGenerator(new PublicVisibilityPredicate(), contracts, new MultiMap<ConcreteType, ConcreteOperation>(), new LinkedHashSet<ConcreteOperation>());
  }

  private static Predicate<ExecutableSequence> createOutputTest() {
    Set<Sequence> sequences = new LinkedHashSet<>();
    ConstructorCall objectConstructor;
    try {
      objectConstructor = new ConstructorCall(Object.class.getConstructor());
    } catch (Exception e) {
      throw new BugInRandoopException(e); // Should never reach here!
    }
    ConcreteOperation op = new ConcreteOperation(objectConstructor, ConcreteTypes.OBJECT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.OBJECT_TYPE);
    sequences.add((new Sequence().extend(op, new ArrayList<Variable>())));
    return (new GenTests())
        .createTestOutputPredicate(
            sequences, new LinkedHashSet<Class<?>>(), include_if_classname_appears);
  }
}
