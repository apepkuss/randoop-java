package test.java.randoop.test;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import main.java.randoop.DummyVisitor;
import main.java.randoop.contract.EqualsHashcode;
import main.java.randoop.contract.EqualsReflexive;
import main.java.randoop.contract.EqualsSymmetric;
import main.java.randoop.contract.EqualsToNullRetFalse;
import main.java.randoop.Globals;
import main.java.randoop.contract.ObjectContract;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.main.GenInputsAbstract.BehaviorType;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.VisibilityPredicate;
import main.java.randoop.sequence.ExecutableSequence;
import main.java.randoop.sequence.Sequence;
import main.java.randoop.sequence.SequenceParseException;
import main.java.randoop.test.ContractCheckingVisitor;
import main.java.randoop.test.ExpectedExceptionCheckGen;
import main.java.randoop.test.ExtendGenerator;
import main.java.randoop.test.predicate.ExceptionBehaviorPredicate;
import main.java.randoop.test.predicate.ExceptionPredicate;
import main.java.randoop.test.RegressionCaptureVisitor;
import main.java.randoop.test.TestCheckGenerator;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.util.MultiMap;
import main.java.randoop.util.RecordListReader;
import main.java.randoop.util.RecordProcessor;
import main.java.randoop.util.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/*
Note: I disabled this test in the build script because the test-script includes the use of
ArrayList as a rawtype. Capturing operations using rawtypes conflicts with the use of parsing to
add methods when populating the model, which recognizes that the type is generic and adds it to the
pool of types to be instantiated. Since this test is the only reason for parsing sequences,
and we aren't trying to parse parameterized types, I'm kicking this down the road.
*/

public class SequenceTests {

  /**
   * Tests the sequence execution and code generation aspects of Randoop.
   *
   * Reads in a file describing a collection of sequences along with the test
   * code that Randoop should generate after executing them. The file consists
   * of a collection of records, each of the form:
   *
   * START TEST
   * TEST_ID
   * <string identifying this test for debugging>
   * SEQUENCE
   * <parseable sequence description>
   * EXPECTED_CODE
   * <Expected Java code resulting from sequence execution>
   * END RECORD
   *
   * Each sequence is parsed, then two checks performed:
   *
   * 1. (Test parsing code) s.toParseableString() can be parsed back into an equivalent sequence
   * 2. (Test execution and test generation code) sequence is executed and the resulting test code is
   *    compared with the expected code from the "EXPECTED_CODE" field in the record.
   */
  @Test
  public void test1() throws Exception {

    RecordProcessor processor =
        new RecordProcessor() {
          @Override
          public void processRecord(List<String> lines) {

            parseRecord(lines);
          }
        };

    RecordListReader reader = new RecordListReader("TEST", processor);
    InputStream stream = SequenceTests.class.getResourceAsStream("/sequence_tests_script.txt");
    BufferedReader b = new BufferedReader(new InputStreamReader(stream));
    reader.parse(b);
  }

  /**
   * The "default" set of visitors that Randoop uses during execution.
   */
  private static final TestCheckGenerator testGen;

  static {
    Set<ObjectContract> contracts = new LinkedHashSet<>();
    contracts.add(new EqualsReflexive());
    contracts.add(new EqualsToNullRetFalse());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsSymmetric());

    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ExceptionPredicate isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
    ExpectedExceptionCheckGen expectation;
    expectation = new ExpectedExceptionCheckGen(visibility, isExpected);
    testGen =
        new ExtendGenerator(
            new ContractCheckingVisitor(
                contracts, new ExceptionBehaviorPredicate(BehaviorType.ERROR)),
            new RegressionCaptureVisitor(expectation, new MultiMap<ConcreteType,ConcreteOperation>(), new LinkedHashSet<ConcreteOperation>() , true));
  }

  // See http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4094886
  private static void parseRecord(List<String> lines) {

    String testId;
    if (!lines.get(0).equals("TEST_ID")) {
      throw new RuntimeException(
          "Malformed test record (does not have a \"TEST_ID\" field): " + lines.toString());
    }
    testId = lines.get(1);

    if (!lines.get(2).equals("SEQUENCE")) {
      throw new RuntimeException(
          "Malformed test record (does not have a \"SEQUENCE\" field): " + lines.toString());
    }

    int currIdx = 3;
    List<String> sequenceLines = new ArrayList<>();
    while (currIdx < lines.size() && !lines.get(currIdx).equals("EXPECTED_CODE")) {
      sequenceLines.add(lines.get(currIdx));
      currIdx++;
    }
    if (currIdx == lines.size()) {
      throw new IllegalArgumentException(
          "Malformed test record (missing \"EXPECTED_CODE\" record): " + lines.toString());
    }
    if (sequenceLines.size() == 0) {
      throw new IllegalArgumentException("Empty sequence found.");
    }

    currIdx++;
    List<String> expectedCode = new ArrayList<>();
    while (currIdx < lines.size()) {
      expectedCode.add(lines.get(currIdx));
      currIdx++;
    }

    if (expectedCode.size() == 0) {
      throw new IllegalArgumentException("Expected code is empty.");
    }

    Sequence sequence;
    try {
      sequence = Sequence.parse(sequenceLines);
    } catch (SequenceParseException e) {
      throw new RuntimeException(e);
    }

    checkListsEqual(
        sequenceLines, Arrays.asList(sequence.toParseableString().split(Globals.lineSep)), testId);

    ExecutableSequence ds = new ExecutableSequence(sequence);
    ds.execute(new DummyVisitor(), testGen);
    checkListsEqual(expectedCode, Arrays.asList(ds.toCodeString().split(Globals.lineSep)), testId);
  }

  private static void checkListsEqual(List<String> expected, List<String> actual, String testId) {

    expected = trimmedLines(expected);
    actual = trimmedLines(actual);

    if (expected.size() != actual.size()) {
      fail(
          failureMessage(
              testId,
              "List lengths differ: expected " + expected.size() + " but got " + actual.size(),
              expected,
              actual));
    }

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(
          failureMessage(testId, "(lists differ at index " + i + ")", expected, actual),
          expected.get(i),
          actual.get(i));
    }
  }

  private static String failureMessage(
      String testId, String msg, List<String> expected, List<String> actual) {
    StringBuilder b = new StringBuilder();
    b.append("Failure in test ").append(testId).append(": ").append(msg).append(".");
    b.append("").append(Globals.lineSep).append("Expected:").append(Globals.lineSep).append("");
    for (int i = 0; i < expected.size(); i++) b.append(i).append(": ").append(expected.get(i)).append(Util.newLine);
    b.append("").append(Globals.lineSep).append("Actual:").append(Globals.lineSep).append("");
    for (int i = 0; i < actual.size(); i++) {
      b.append(i).append(": ").append(actual.get(i)).append(Util.newLine);
    }
    return b.toString();
  }

  // Skips empty lines.
  private static List<String> trimmedLines(List<String> list) {
    List<String> trimmed = new ArrayList<>();
    for (String str : list) {
      String t = str.trim();
      if (! t.isEmpty()) {
        trimmed.add(t);
      }
    }
    return trimmed;
  }
}
