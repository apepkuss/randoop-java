package main.java.randoop.operation;

import java.io.PrintStream;
import java.util.List;

import plume.UtilMDE;
import main.java.randoop.ExecutionOutcome;
import main.java.randoop.NormalExecution;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.sequence.Variable;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.ConcreteTypes;
import main.java.randoop.types.GeneralType;
import main.java.randoop.types.GeneralTypeTuple;
import main.java.randoop.types.GenericTypeTuple;
import main.java.randoop.types.PrimitiveTypes;
import main.java.randoop.types.RandoopTypeException;
import main.java.randoop.util.StringEscapeUtils;
import main.java.randoop.util.Util;

/**
 * Represents a value that either cannot (primitive or null values), or we don't
 * care to have (String) be a receiver for a method call as an {@link Operation}
 * .
 *
 * As an {@link Operation} a value v of type T is formally represented by an
 * operation v : [] &rarr; T, with no input types, and the type of the value as
 * the output type. This kind of operation is a <i>ground</i> term &mdash; it
 * requires no inputs.
 *
 * The execution of this {@link Operation} simply returns the value.
 */
public final class NonreceiverTerm extends CallableOperation {

  /**
   * ID for parsing purposes.
   *
   * @see OperationParser#getId(ConcreteOperation)
   */
  public static final String ID = "prim";

  // State variables.
  private final ConcreteType type;
  // This value is guaranteed to be null, a String, or a boxed primitive.
  private final Object value;

  /**
   * Constructs a NonreceiverTerm with type t and value o.
   *
   * @param type
   *          the type of the term
   * @param value
   *          the value of the term
   */
  public NonreceiverTerm(ConcreteType type, Object value) {
    if (type == null) throw new IllegalArgumentException("type should not be null.");

    if (type.isVoid()) throw new IllegalArgumentException("type should not be void.class.");

    if (type.isPrimitive() || type.isBoxedPrimitive()) {
      if (value == null) {
        if (type.isPrimitive()) {
          throw new IllegalArgumentException("primitive-like values cannot be null.");
        }
      } else {
        if (!type.isInstance(value))
          throw new IllegalArgumentException("value.getClass()=" + value.getClass() + ",type=" + type);
        if (!PrimitiveTypes.isBoxedOrPrimitiveOrStringType(value.getClass()))
          throw new IllegalArgumentException("value is not a primitive-like value.");
      }
    } else if (type.isString()) {
      if (value != null && !PrimitiveTypes.stringLengthOK((String) value)) {
        throw new IllegalArgumentException(
            "String too long, length = " + ((String) value).length());
      }
    } else {
      // if it's not primitive or string then must be null
      if (value != null) {
        throw new IllegalArgumentException(
            "value must be null for non-primitive, non-string type " + type + " but was " + value);
      }
    }

    this.type = type;
    this.value = value;
  }

  /**
   * Indicates whether this object is equal to o
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NonreceiverTerm)) return false;
    if (this == o) return true;
    NonreceiverTerm other = (NonreceiverTerm) o;

    return this.type.equals(other.type) && Util.equalsWithNull(this.value, other.value);
  }

  /**
   * Returns a hash code value for this NonreceiverTerm
   */
  @Override
  public int hashCode() {
    return this.type.hashCode() + (this.value == null ? 0 : this.value.hashCode());
  }

  /**
   * Returns string representation of this NonreceiverTerm
   */
  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public String getName() {
    return this.toString();
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link NormalExecution} object enclosing value of this non-receiver
   *         term.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == 0;
    return new NormalExecution(this.value, 0);
  }

  /**
   * {@inheritDoc} For NonreceiverTerm, simply adds a code representation of the
   * value to the string builder. Note: this does not explicitly box primitive
   * values.
   *
   * @see ConcreteOperation#appendCode(List, StringBuilder)
   *
   * @param inputVars
   *          ignored
   * @param b
   *          {@link StringBuilder} to which string representation is appended.
   *
   */
  @Override
  public void appendCode(ConcreteType declaringType, ConcreteTypeTuple inputTypes, ConcreteType outputType, List<Variable> inputVars, StringBuilder b) {
    b.append(PrimitiveTypes.toCodeString(getValue()));
  }

  /**
   * {@inheritDoc}
   *
   * @return value of this {@link NonreceiverTerm}
   */
  @Override
  public Object getValue() {
    return value;
  }

  /**
   * @return Returns the type.
   */
  public ConcreteType getType() {
    return this.type;
  }

  /**
   * Returns a NonreceiverTerm holding the zero value for the specified class c.
   * In the case of characters there is no natural zero, so the value 'a' is
   * used.
   *
   * @param type
   *          the type of value desired.
   * @return a {@link NonreceiverTerm} with a canonical representative of the
   *         given type.
   */
  static NonreceiverTerm createNullOrZeroTerm(ConcreteType type) {
    if (type.isBoxedPrimitive()) {
      type = type.toPrimitive();
    }
    if (type.isString()) return new NonreceiverTerm(type, "");
    if (type.equals(ConcreteTypes.CHAR_TYPE))
      return new NonreceiverTerm(type, 'a'); // TODO This is not null or zero...
    if (type.equals(ConcreteTypes.BYTE_TYPE)) return new NonreceiverTerm(type, (byte) 0);
    if (type.equals(ConcreteTypes.SHORT_TYPE)) return new NonreceiverTerm(type, (short) 0);
    if (type.equals(ConcreteTypes.INT_TYPE))
      return new NonreceiverTerm(type, 0);
    if (type.equals(ConcreteTypes.LONG_TYPE)) return new NonreceiverTerm(type, 0L);
    if (type.equals(ConcreteTypes.FLOAT_TYPE))
      return new NonreceiverTerm(type, 0f);
    if (type.equals(ConcreteTypes.DOUBLE_TYPE))
      return new NonreceiverTerm(type, 0d);
    if (type.equals(ConcreteTypes.BOOLEAN_TYPE)) return new NonreceiverTerm(type, false);
    return new NonreceiverTerm(type, null);
  }

  /**
   * {@inheritDoc} Returns a string representing this primitive declaration. The
   * string is of the form:<br>
   *
   * <code>TYPE:VALUE</code><br>
   *
   * Where TYPE is the type of the primitive declaration, and VALUE is its
   * value. If VALUE is "null" then the value is null (not the String "null").
   * If TYPE is "char" then (char)Integer.parseInt(VALUE, 16) yields the
   * character value.
   * <p>
   * Examples:
   *
   * <pre>
   * String:null                  represents: String x = null
   * java.lang.String:""          represents: String x = "";
   * String:""                    represents: String x = "";
   * String:" "                   represents: String x = " ";
   * String:"\""                  represents: String x = "\"";
   * String:"\n"                  represents: String x = "\n";
   * String:"\u0000"              represents: String x = "\u0000";
   * java.lang.Object:null        represents: Object x = null;
   * [[Ljava.lang.Object;:null    represents: Object[][] = null;
   * int:0                        represents: int x = 0;
   * boolean:false                represents: boolean x = false;
   * char:20                      represents: char x = ' ';
   * </pre>
   *
   * Note that a string type can be given as both "String" or
   * "java.lang.String".
   *
   * @return string representation of primitive, String or null value.
   */
  @Override
  public String toParseableString(ConcreteType declaringType, ConcreteTypeTuple inputTypes, ConcreteType outputType) {

    String valStr;
    if (value == null) {
      valStr = "null";
    } else {
      if (type.isString()) {
        valStr = "\"" + StringEscapeUtils.escapeJava(value.toString()) + "\"";
      } else if (type.equals(ConcreteTypes.CHAR_TYPE)) {
        valStr = Integer.toHexString((Character) value);
      } else {
        valStr = value.toString();
      }
    }

    return type.getName() + ":" + valStr;
  }

  /**
   * Parse a non-receiver value in a string in the form generated by
   * {@link NonreceiverTerm#toParseableString(ConcreteType, ConcreteTypeTuple, ConcreteType)}
   *
   * @param s
   *          a string representing a value of a non-receiver type.
   * @param manager
   *          the {@link TypedOperationManager} to collect operations
   * @throws OperationParseException
   *           if string does not represent valid object.
   */
  public static void parse(String s, TypedOperationManager manager) throws OperationParseException {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    int colonIdx = s.indexOf(':');
    if (colonIdx == -1) {
      String msg =
          "A primitive value declaration description must be of the form "
              + "<type>:<value>"
              + " but the description \""
              + s
              + "\" does not have this form.";
      throw new OperationParseException(msg);
    }
    // Extract type and value.
    String typeString = s.substring(0, colonIdx);
    String valString = s.substring(colonIdx + 1);

    // Basic sanity check: no whitespace in type string.
    if (typeString.matches(".*\\s+.*")) {
      String msg =
          "Error when parsing type/value pair "
              + s
              + ". A primitive value declaration description must be of the form "
              + "<type>:<value>"
              + " but the <type> description \""
              + s
              + "\" contains invalid whitespace characters.";
      throw new OperationParseException(msg);
    }

    // Convert "String" to "java.lang.String"
    if (typeString.equals("String")) {
      typeString = "java.lang.String";
    }

    ConcreteType type;
    try {
      type = (ConcreteType)ConcreteType.forName(typeString);
    } catch (ClassNotFoundException e1) {
      String msg =
          "Error when parsing type/value pair "
              + s
              + ". A primitive value declaration description must be of the form "
              + "<type>:<value>"
              + " but the <type> given (\""
              + typeString
              + "\") was unrecognized.";
      throw new OperationParseException(msg);
    } catch (RandoopTypeException e) {
      String msg = "Error when parsing type/value pair " + s + ". Type error: " + e.getMessage();
      throw new OperationParseException(msg);
    }

    Object value;
    if (type.equals(ConcreteTypes.CHAR_TYPE)) {
      try {
        value = (char) Integer.parseInt(valString, 16);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.BYTE_TYPE)) {
      try {
        value = Byte.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.SHORT_TYPE)) {
      try {
        value = Short.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.INT_TYPE)) {
      try {
        value = Integer.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.LONG_TYPE)) {
      try {
        value = Long.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.FLOAT_TYPE)) {
      try {
        value = Float.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.DOUBLE_TYPE)) {
      try {
        value = Double.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(ConcreteTypes.BOOLEAN_TYPE)) {
      if (valString.equals("true") || valString.equals("false")) {
        value = Boolean.valueOf(valString);
      } else {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.isString()) {
      if (valString.equals("null")) {
        value = null;
      } else {
        if (valString.charAt(0) != '"' || valString.charAt(valString.length() - 1) != '"') {
          String msg =
              "Error when parsing type/value pair "
                  + s
                  + ". A String value declaration description must be of the form "
                  + "java.lang.String:\"thestring\""
                  + " but the string given was not enclosed in quotation marks.";
          throw new OperationParseException(msg);
        }
        value = UtilMDE.unescapeNonJava(valString.substring(1, valString.length() - 1));
        if (!PrimitiveTypes.stringLengthOK((String) value)) {
          throw new OperationParseException(
              "Error when parsing String; length is greater than "
                  + GenInputsAbstract.string_maxlen);
        }
      }
    } else {
      if (valString.equals("null")) {
        value = null;
      } else {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitve value declaration description that is not a primitive value or a string must be of the form "
                + "<type>:null but the string given (\""
                + valString
                + "\") was not of this form.";
        throw new OperationParseException(msg);
      }
    }

    NonreceiverTerm nonreceiverTerm = new NonreceiverTerm(type, value);
    manager.createTypedOperation(nonreceiverTerm, type, new GenericTypeTuple(), type);
  }

  /**
   * {@inheritDoc}
   *
   * @return true, since all of objects are non-receivers.
   */
  @Override
  public boolean isNonreceivingValue() {
    return true;
  }
}
