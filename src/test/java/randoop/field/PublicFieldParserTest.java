package test.java.randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import main.java.randoop.field.AccessibleField;
import main.java.randoop.field.FieldParser;
import main.java.randoop.operation.OperationParseException;
import main.java.randoop.types.GeneralType;
import main.java.randoop.types.RandoopTypeException;

public class PublicFieldParserTest {

  @Test
  public void parseConstraintInstance() {
    String fieldPair = "randoop.field.ClassWithFields.oneField";
    String classname = "randoop.field.ClassWithFields";
    String fieldname = "oneField";
    GeneralType classType = null;
    try {
      classType = GeneralType.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("couldn't load class: " + e.getMessage());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assert classType != null;
    try {
      AccessibleField pf = FieldParser.parse(fieldPair, classname, fieldname);
      assertEquals(
          "parse(\"t:v\").toParseableString(classType) should equal \"t:v\"",
          fieldPair,
          pf.toParseableString(classType));
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @Test
  public void parseConstraintStatic() {
    String fieldPair = "randoop.field.ClassWithFields.fourField";
    String classname = "randoop.field.ClassWithFields";
    String fieldname = "fourField";
    GeneralType classType = null;
    try {
      classType = GeneralType.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("couldn't load class: " + e);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assert classType != null;
    try {
      AccessibleField pf = FieldParser.parse(fieldPair, classname, fieldname);
      assertEquals(
          "parse(\"t:v\").toParseableString(classType) should equal \"t:v\"",
          fieldPair,
          pf.toParseableString(classType));

      assertTrue("object is a static field", pf.isStatic());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @Test
  public void parseConstraintStaticFinal() {
    String fieldPair = "randoop.field.ClassWithFields.FIVEFIELD";
    String classname = "randoop.field.ClassWithFields";
    String fieldname = "FIVEFIELD";
    GeneralType classType = null;
    try {
      classType = GeneralType.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("couldn't load class: " + e);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assert classType != null;
    try {
      AccessibleField pf = FieldParser.parse(fieldPair, classname, fieldname);
      assertEquals(
          "parse(\"t:v\").toParseableString(classType) should equal \"t:v\"",
          fieldPair,
          pf.toParseableString(classType));

      assertTrue("object is a static final field", pf.isStatic() && pf.isFinal());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void parseErrors() {
    String spaceInValue = "randoop.field.ClassWithFields.one ield";
    String nonField = "randoop.field.ClassWithFields.oneMethod";

    String errorPrefix1 = "Error when parsing field ";

    String classname = "randoop.field.ClassWithFields";
    String fieldname = "one ield";
    try {
      AccessibleField pf = FieldParser.parse(spaceInValue, classname, fieldname);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + spaceInValue
              + "."
              + " The field name " + fieldname + " has unexpected whitespace characters.";
      assertEquals("Expecting space in field message", msg, e.getMessage());
    }

    classname = "randoop.field.ClassWithFields";
    fieldname = "oneMethod";
    try {
      AccessibleField pf = FieldParser.parse(nonField, classname, fieldname);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + nonField
              + ". The field name \"oneMethod\""
              + " is not a field of the class \"randoop.field.ClassWithFields\".";
      assertEquals("Expecting not a field message", msg, e.getMessage());
    }
  }
}
