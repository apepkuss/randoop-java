package test.java.randoop.operation;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import main.java.randoop.field.AccessibleField;
import test.java.randoop.field.ClassWithFields;
import test.java.randoop.field.SubclassWithFields;
import main.java.randoop.operation.ConcreteOperation;
import main.java.randoop.operation.FieldGet;
import main.java.randoop.operation.FieldSet;
import main.java.randoop.reflection.DefaultReflectionPredicate;
import main.java.randoop.reflection.ModelCollections;
import main.java.randoop.reflection.OperationExtractor;
import main.java.randoop.reflection.PublicVisibilityPredicate;
import main.java.randoop.reflection.ReflectionManager;
import main.java.randoop.reflection.ReflectionPredicate;
import main.java.randoop.reflection.TypedOperationManager;
import main.java.randoop.reflection.VisibilityPredicate;
import main.java.randoop.types.ConcreteSimpleType;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.ConcreteTypeTuple;
import main.java.randoop.types.ConcreteTypes;
import main.java.randoop.types.RandoopTypeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * FieldReflectionTest consists of tests of reflection collection of field methods
 * to verify that field operations are collected as expected.
 *
 */
public class FieldReflectionTest {

  /**
   * basicFields tests that all of the expected fields are collected for the
   * ClassWithFields class.
   */
  @Test
  public void basicFields() {
    Class<?> c = ClassWithFields.class;
    ConcreteType declaringType = new ConcreteSimpleType(c);

    @SuppressWarnings("unchecked")
    List<Field> fields = Arrays.asList(c.getFields());

    final Set<ConcreteOperation> operations = getConcreteOperations(c);

    //number of operations is twice number of fields plus constructor and getter minus one for each constant
    //in this case, 11
    assertEquals("number of operations twice number of fields", 2 * fields.size(), operations.size());

    //exclude private or protected fields
    List<Field> exclude = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers();
      if (Modifier.isPrivate(mods) || Modifier.isProtected(mods)) {
        exclude.add(f);
      }
    }

    try {
      for (Field f : fields) {
        assertTrue(
                "field " + f.toGenericString() + " should occur", operations.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      for (Field f : exclude) {
        assertFalse(
                "field " + f.toGenericString() + " should not occur",
                operations.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    final Set<ConcreteOperation> operations = new LinkedHashSet<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        operations.add(operation);
      }
    });
    OperationExtractor extractor = new OperationExtractor(operationManager, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }

  /**
   * inheritedFields looks for operations built for inherited fields.
   * Avoid hidden fields, because we cannot get to them without reflection.
   */
  @Test
  public void inheritedFields() {
    Class<?> c = SubclassWithFields.class;
    ConcreteType declaringType = new ConcreteSimpleType(c);

    List<Field> expected = new ArrayList<>();
    List<Field> exclude = new ArrayList<>();
    List<String> declared = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      declared.add(f.getName());
    }

    for (Field f : c.getFields()) {
      if (declared.contains(f.getName())) {
        if (c.equals(f.getDeclaringClass())) {
          expected.add(f);
        } else { //hidden
          exclude.add(f);
        }
      } else {
        expected.add(f);
      }
    }
    Set<ConcreteOperation> actual = getConcreteOperations(c);

    assertEquals("number of operations ", 2 * expected.size() - 1 + 2, actual.size());
    try {
      for (Field f : expected) {
        assertTrue(
                "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }

    try {
      for (Field f : exclude) {
        assertFalse(
                "field " + f.toGenericString() + " should not occur",
                actual.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }
  }

  /**
   * filteredFields checks to ensure we don't get any fields that should be removed
   *
   */
  @Test
  public void filteredFields() {
    Class<?> c = ClassWithFields.class;
    ConcreteType declaringType = new ConcreteSimpleType(c);

    //let's exclude every field
    List<Field> exclude = new ArrayList<>();
    Set<String> excludeNames = new TreeSet<>();
    for (Field f : c.getFields()) {
      excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      exclude.add(f);
    }

    ReflectionPredicate filter = new DefaultReflectionPredicate(null, excludeNames);
    Set<ConcreteOperation> actual = getConcreteOperations(c, filter, new PublicVisibilityPredicate());

    assertEquals("number of operations ", 2, actual.size());

    for (Field f : exclude) {
      try {
        assertFalse(
            "field " + f.toGenericString() + " should not occur",
            actual.containsAll(getOperations(f, declaringType)));
      } catch (RandoopTypeException e) {
        fail("type error: " + e.getMessage());
      }
    }
  }

  /**
   * getOperations maps a field into possible operations.
   * Looks at modifiers to decide which kind of field wrapper
   * to create and then builds list with getter and setter.
   *
   * @param f - reflective Field object
   * @return List of getter/setter statements for the field
   */
  private List<ConcreteOperation> getOperations(Field f, ConcreteType declaringType) throws RandoopTypeException {
    List<ConcreteOperation> statements = new ArrayList<>();
    ConcreteType fieldType = ConcreteType.forType(f.getGenericType());
    AccessibleField field = new AccessibleField(f, declaringType);
    List<ConcreteType> getInputTypeList = new ArrayList<>();
    List<ConcreteType> setInputTypeList = new ArrayList<>();
    if (! field.isStatic()) {
      getInputTypeList.add(declaringType);
      setInputTypeList.add(declaringType);
    }

    statements.add(new ConcreteOperation(new FieldGet(field), declaringType, new ConcreteTypeTuple(getInputTypeList), fieldType));

    if (! field.isFinal()) {
      setInputTypeList.add(fieldType);
      statements.add(new ConcreteOperation(new FieldSet(field), declaringType, new ConcreteTypeTuple(setInputTypeList), ConcreteTypes.VOID_TYPE));
    }
    return statements;
  }
}
