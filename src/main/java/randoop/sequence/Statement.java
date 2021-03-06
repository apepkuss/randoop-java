package main.java.randoop.sequence;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import main.java.randoop.ExecutionOutcome;
import main.java.randoop.Globals;
import main.java.randoop.operation.CallableOperation;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.operation.Operation;
import main.java.randoop.operation.OperationParser;
import main.java.randoop.sequence.Sequence.RelativeNegativeIndex;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.PrimitiveTypes;

/**
 * Statement represents a statement involving an operation (or term), and the
 * list of inputs for the statement. The inputs are variables, but are
 * represented by indexing into the enclosing sequence.
 *
 */
public final class Statement {

  /**
   * The operation (method call, constructor call, primitive values declaration,
   * etc.).
   */
  private final ConcreteOperation operation;

  // The list of values used as input to the statement.
  //
  // NOTE that the inputs to a statement are not a list
  // of Variables, but a list of RelativeNegativeIndex objects.
  // See that class for an explanation.
  final List<RelativeNegativeIndex> inputs;

  /**
   * Create a new statement of type statement that takes as input the given
   * values.
   */
  public Statement(ConcreteOperation operation, List<RelativeNegativeIndex> inputVariables) {
    this.operation = operation;
    this.inputs = new ArrayList<>(inputVariables);
  }

  /**
   * Creates a statement based on the given operation
   *
   * @param operation
   *          the operation for action of this statement.
   */
  public Statement(ConcreteOperation operation) {
    this(operation, new ArrayList<RelativeNegativeIndex>());
  }

  /**
   * True iff this statement is a void method call.
   *
   * @return true if output type is void.
   */
  boolean isVoidMethodCall() {
    return operation.isMethodCall() && operation.getOutputType().isVoid();
  }

  /**
   * equals tests whether two Statement objects are equal:
   *
   * @return true if operation is the same, the number of inputs is the same,
   *         and inputs are equal.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Statement)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    Statement s = (Statement) obj;
    if (!operation.equals(s.operation)) {
      return false;
    }
    if (inputs.size() != s.inputs.size()) {
      return false;
    }
    for (int j = 0; j < inputs.size(); j++) {
      if (!inputs.get(j).equals(s.inputs.get(j))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(operation, inputs);
  }

  public ConcreteType getOutputType() {
    return operation.getOutputType();
  }

  public ConcreteTypeTuple getInputTypes() {
    return operation.getInputTypes();
  }

  /**
   * Adds code for the statement to the given {@code StringBuilder}.
   *
   * @see Sequence#appendCode(StringBuilder, int)
   *
   * @param variable
   *          the {@link Variable} to be used if an initialization.
   * @param inputs
   *          the input list for the operation of the statement.
   * @param b
   *          the {@code StringBuilder} to which code text is appended.
   */
  public void appendCode(Variable variable, List<Variable> inputs, StringBuilder b) {
    ConcreteType type = operation.getOutputType();
    if (! type.isVoid()) {
      String typeName = type.getName();
      b.append(typeName);
      b.append(" ").append(Variable.classToVariableName(type)).append(variable.index).append(" = ");
    }
    operation.appendCode(inputs, b);
    b.append(";").append(Globals.lineSep);
  }

  public String toParseableString(String variableName, List<Variable> inputs) {
    StringBuilder b = new StringBuilder();
    b.append(variableName);
    b.append(" =  ");
    b.append(OperationParser.getId(operation));
    b.append(" : ");
    b.append(operation.toParseableString());
    b.append(" : ");
    for (Variable v : inputs) {
      b.append(v.toString());
      b.append(" ");
    }
    return b.toString();
  }

  @Override
  public String toString() {
    return "Statement(" + operation.toParseableString() + ")";
  }

  /**
   * toModifiableStatement converts the statement to the mutable form.
   *
   * @param inputs
   *          mutable version of variable inputs to statement.
   * @param mVariable
   *          the mutable variable the statement affects
   * @return instance of mutable statement corresponding to this statement.
   */
  MutableStatement toModifiableStatement(
          List<MutableVariable> inputs, MutableVariable mVariable) {
    return new MutableStatement(operation, inputs, mVariable);
  }

  /**
   * isStatic indicates whether the corresponding operation is declared as
   * static.
   *
   * @return result of isStatic on operation corresponding to statement.
   */
  public boolean isStatic() {
    return operation.isStatic();
  }

  /**
   * isMethodCall indicates whether a statement corresponds to a
   * method-call-like operation. This could be either a method call, an
   * assignment/initialization involving a method call, or the use of a public
   * field in an assignment on either lhs (as "setter") or rhs (as "getter").
   *
   * @return true if operation is method-call-like, and false otherwise.
   */
  public boolean isMethodCall() {
    return operation.isMessage();
  }

  /**
   * execute performs the operation of the statement for the input variables and
   * returns outcome.
   *
   * @param inputs
   *          list of objects to use as inputs to execution.
   * @param out
   *          stream for any output.
   * @return object representing outcome of computation.
   */
  public ExecutionOutcome execute(Object[] inputs, PrintStream out) {
    return operation.execute(inputs, out);
  }

  /**
   * getDeclaringClass returns the declaring class as defined by the
   * {@link Operation} of the statement.
   *
   * @return result of getDeclaringClass for corresponding statement.
   */
  public ConcreteType getDeclaringClass() {
    return operation.getDeclaringType();
  }

  /**
   * isConstructorCall determines if operation for statement is a call to a
   * constructor.
   *
   * @return true if operation is a constructor call, and false otherwise.
   */
  public boolean isConstructorCall() {
    return operation.isConstructorCall();
  }

  /**
   * isPrimitiveInitialization determines if operation is a nonreceiver term.
   *
   * @return true if operation is a nonreceiver, and false otherwise.
   */
  public boolean isPrimitiveInitialization() {
    return operation.isNonreceivingValue();
  }

  /**
   * isNullInitialization determines if statement represents an initialization by
   * null value.
   *
   * @return true if statement represents null initialization, and false
   *         otherwise.
   */
  public boolean isNullInitialization() {
    return isPrimitiveInitialization() && operation.getValue() == null;
  }

  /**
   * getShortForm constructs code expression of the operation for substitution
   * into argument lists as opposed to using variable.
   *
   * @return string containing code to access the value of the operation/term
   */
  // Historical note:
  // Do not use the short output format if the value is null, because
  // the variable type may disambiguate among overloaded methods.
  // (It would be even nicer to use the short output format unless
  // disambiguation is truly needed.)
  public String getShortForm() {
    if (isPrimitiveInitialization() && !isNullInitialization()) {
      return PrimitiveTypes.toCodeString(operation.getValue());
    }
    return null;
  }

  /**
   * getValue returns the "value" for a statement. Is only meaningful if
   * statement is an assignment of a constant value. Appeals to
   * {@link CallableOperation} to throw appropriate exception when unable to
   * provide a value.
   *
   * This is a hack to allow randoop.main.GenBranchDir to do mutation.
   *
   * @return value of term in statement (rhs of assignment/initialization) if
   *         any
   */
  public Object getValue() {
    return operation.getValue();
  }

  /**
   * getConcreteOperation is meant to be a temporary solution to type confusion in
   * generators. This should go away. Only intended to be called by
   * {@link Sequence#extend(ConcreteOperation, List)}.
   *
   * @return operation object in the statement.
   */
  // TODO can remove once RandomWalkGenerator.extendRandomly and
  // SequenceSimplifyUtils.makeCopy modified
  public final ConcreteOperation getOperation() {
    return operation;
  }
}
