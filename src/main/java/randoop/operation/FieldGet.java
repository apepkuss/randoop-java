package main.java.randoop.operation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import main.java.randoop.BugInRandoopException;
import main.java.randoop.ExceptionalExecution;
import main.java.randoop.ExecutionOutcome;
import main.java.randoop.NormalExecution;
import main.java.randoop.field.AccessibleField;
import main.java.randoop.field.FieldParser;
import main.java.randoop.reflection.ReflectionPredicate;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.sequence.Variable;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.GeneralType;
import main.java.randoop.types.GeneralTypeTuple;
import main.java.randoop.types.GenericTypeTuple;
import main.java.randoop.types.RandoopTypeException;

/**
 * FieldGetter is an adapter that creates a {@link Operation} from a
 * {@link AccessibleField} and behaves like a getter for the field.
 *
 * @see AccessibleField
 *
 */
public class FieldGet extends CallableOperation {

  public static String ID = "getter";

  private AccessibleField field;

  /**
   * FieldGetter sets the public field for the getter statement.
   *
   * @param field
   *          the {@link AccessibleField} object from which to get values.
   */
  public FieldGet(AccessibleField field) {
    this.field = field;
  }

  /**
   * Performs computation of getting value of field or capturing thrown exceptions.
   * Exceptions should only be NullPointerException, which happens when input is null but
   * field is an instance field. {@link AccessibleField#getValue(Object)} suppresses exceptions
   * that occur because field is not valid or accessible.
   *
   * @param statementInput  the inputs for statement.
   * @param out  the stream for printing output (unused).
   * @return outcome of access.
   * @throws BugInRandoopException
   *           if field access throws bug exception.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {

    // either 0 or 1 inputs. If none use null, otherwise give object.
    Object input = statementInput.length == 0 ? null : statementInput[0];

    try {

      Object value = field.getValue(input);
      return new NormalExecution(value, 0);

    } catch (BugInRandoopException e) {
      throw e;
    } catch (Throwable thrown) {
      return new ExceptionalExecution(thrown, 0);
    }
  }

  /**
   * Adds the text for an initialization of a variable from a field to the
   * StringBuilder.
   *
   * @param inputVars
   *          the list of variables to be used (ignored).
   * @param b
   *          the StringBuilder that strings are appended to.
   */
  @Override
  public void appendCode(ConcreteType declaringType, ConcreteTypeTuple inputTypes, ConcreteType outputType, List<Variable> inputVars, StringBuilder b) {
    b.append(field.toCode(declaringType, inputVars));
  }

  /**
   * Returns string descriptor for field that can be parsed by
   * PublicFieldParser.
   */
  @Override
  public String toParseableString(ConcreteType declaringType, ConcreteTypeTuple inputTypes, ConcreteType outputType) {
    return declaringType.getName() + ".<get>(" + field.getName() + ")";
  }

  @Override
  public String toString() {
    return field.toString();
  }

  public String getName() {
    return "<get>(" + field.getName() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FieldGet) {
      FieldGet s = (FieldGet) obj;
      return field.equals(s.field);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  /**
   * Parses a getter for a field from a string. A getter description has the
   * form "&lt;get&gt;( field-descriptor )" where &lt;get&gt;" is literal ("&lt;
   * " and "&gt;" included), and field-descriptor is as recognized by
   *
   * @param descr
   *          the string containing descriptor of getter for a field.
   * @param manager
   *          the {@link TypedOperationManager} to collect operations
   * @throws OperationParseException
   *           if any error in descriptor string
   */
  public static void parse(String descr, TypedOperationManager manager) throws OperationParseException {
    String errorPrefix = "Error parsing " + descr + " as description for field getter statement: ";

    int openParPos = descr.indexOf('(');
    int closeParPos = descr.indexOf(')');

    if (openParPos < 0) {
      String msg = errorPrefix + " expecting parentheses.";
      throw new OperationParseException(msg);
    }
    String prefix = descr.substring(0, openParPos);
    int lastDotPos = prefix.lastIndexOf('.');
    assert lastDotPos > 0 : "should be a period after the classname: " + descr;

    String classname = prefix.substring(0, lastDotPos);
    String opname = prefix.substring(lastDotPos + 1);
    assert opname.equals("<get>") : "expecting <get>, saw " + opname;

    if (closeParPos < 0) {
      String msg = errorPrefix + " no closing parentheses found.";
      throw new OperationParseException(msg);
    }
    String fieldname = descr.substring(openParPos + 1, closeParPos);

    AccessibleField accessibleField = FieldParser.parse(descr, classname, fieldname);
    GeneralType classType = accessibleField.getDeclaringType();
    GeneralType fieldType;
    try {
      fieldType = GeneralType.forType(accessibleField.getRawField().getGenericType());
    } catch (RandoopTypeException e) {
      String msg = errorPrefix + " type error " + e;
      throw new OperationParseException(msg);
    }

    List<GeneralType> getInputTypeList = new ArrayList<>();
    if (! accessibleField.isStatic()) {
      getInputTypeList.add(classType);
    }
    manager.createTypedOperation(new FieldGet(accessibleField), classType, new GenericTypeTuple(getInputTypeList), fieldType);
  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  /**
   * {@inheritDoc}
   *
   * @return true, always.
   */
  @Override
  public boolean isMessage() {
    return true;
  }

  /**
   * Determines whether enclosed {@link java.lang.reflect.Field} satisfies the
   * given predicate.
   *
   * @param predicate
   *          the {@link ReflectionPredicate} to be checked.
   * @return true only if the field used in this getter satisfies
   *         predicate.canUse.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return field.satisfies(predicate);
  }
}
