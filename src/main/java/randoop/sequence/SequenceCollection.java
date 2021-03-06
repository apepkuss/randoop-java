package main.java.randoop.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.randoop.Globals;
import main.java.randoop.SubTypeSet;
import main.java.randoop.main.GenInputsAbstract;
import main.java.randoop.types.ConcreteType;
import main.java.randoop.util.ArrayListSimpleList;
import main.java.randoop.util.ListOfLists;
import main.java.randoop.util.Log;
import main.java.randoop.util.SimpleList;

/**
 * A collection of sequences that makes its efficient to ask for all the
 * sequences that create a value of a given type.
 *
 * <p>
 * RANDOOP IMPLEMENTATION NOTE.
 * <p>
 *
 * When creating new sequences, Randoop often needs to search for all the
 * previously-generated sequences that create one or more values of a given
 * type. Since this set can contain thousands of sequences, finding these
 * sequences can can be time-consuming and a bottleneck in generation (as we
 * discovered during profiling).
 *
 * <p>
 *
 * This class makes the above search faster by maintaining two data structures:
 *
 * <ul>
 * <li>A map from types to the sets of all sequences that create one or more
 * values of exactly the given type.
 *
 * <li>A set of all the types that can be created with the existing set of
 * sequences. The set is maintained as a {@link SubTypeSet} that allows for
 * quick queries about can-be-used-as relationships among the types in the set.
 * </ul>
 *
 * To find all the sequences that create values of a given type, Randoop first
 * uses the <code>SubTypeSet</code> to find the set <code>S</code> of feasible
 * subtypes in set of sequences, and returns the range of <code>S</code> in the
 * sequence map.
 */
public class SequenceCollection {

  // We make it a list to make it easier to pick out an element at random.
  private Map<ConcreteType, ArrayListSimpleList<Sequence>> sequenceMap = new LinkedHashMap<>();

  private SubTypeSet typeSet = new SubTypeSet(false);

  private int sequenceCount = 0;

  private void checkRep() {
    if (!GenInputsAbstract.debug_checks) return;
    if (sequenceMap.size() != typeSet.size()) {
      String b = "activesequences types=" + Globals.lineSep
              + sequenceMap.keySet()
              + ", typesWithsequencesMap types=" + Globals.lineSep
              + typeSet.typesWithsequences;
      throw new IllegalStateException(b);
    }
  }

  public int size() {
    return sequenceCount;
  }

  /**
   * Removes all sequences from this collection.
   */
  public void clear() {
    if (Log.isLoggingOn()) Log.logLine("Clearing sequence collection.");
    this.sequenceMap = new LinkedHashMap<>();
    this.typeSet = new SubTypeSet(false);
    sequenceCount = 0;
    checkRep();
  }

  /**
   * Create a new, empty collection.
   */
  public SequenceCollection() {
    this(new ArrayList<Sequence>());
  }

  /**
   * Create a new collection and adds the given initial sequences.
   */
  public SequenceCollection(Collection<Sequence> initialSequences) {
    if (initialSequences == null) throw new IllegalArgumentException("initialSequences is null.");
    this.sequenceMap = new LinkedHashMap<>();
    this.typeSet = new SubTypeSet(false);
    sequenceCount = 0;
    addAll(initialSequences);
    checkRep();
  }

  public void addAll(Collection<Sequence> col) {
    if (col == null) {
      throw new IllegalArgumentException("col is null");
    }
    for (Sequence c : col) {
      add(c);
    }
  }

  public void addAll(SequenceCollection components) {
    for (ArrayListSimpleList<Sequence> s : components.sequenceMap.values()) {
      for (Sequence seq : s.theList) {
        add(seq);
      }
    }
  }

  /**
   * Add a sequence to this collection. This method takes into account the
   * active indices in the sequence. If sequence[i] creates a values of type T,
   * and sequence[i].isActive==true, then the sequence is seen as creating a
   * useful value at index i. More precisely, the method/constructor at that
   * index is said to produce a useful value (and if the user later queries for
   * all sequences that create a T, the sequence will be in the collection
   * returned by the query). How a value is deemed useful or not is left up to
   * the client.
   */
  public void add(Sequence sequence) {
    List<ConcreteType> formalTypes = sequence.getTypesForLastStatement();
    List<Variable> arguments = sequence.getVariablesOfLastStatement();
    assert formalTypes.size() == arguments.size();
    for (int i = 0; i < formalTypes.size(); i++) {
      Variable argument = arguments.get(i);
      assert formalTypes
          .get(i)
          .isAssignableFrom(argument.getType()) : formalTypes.get(i).getName() + " should be assignable from " + argument.getType().getName();
      if (sequence.isActive(argument.getDeclIndex())) {
        ConcreteType type = formalTypes.get(i);
        typeSet.add(type);
        updateCompatibleMap(sequence, type);
      }
    }
    checkRep();
  }

  private void updateCompatibleMap(Sequence sequence, ConcreteType type) {
      ArrayListSimpleList<Sequence> set = this.sequenceMap.get(type);
      if (set == null) {
        set = new ArrayListSimpleList<>();
        this.sequenceMap.put(type, set);
      }
      if (Log.isLoggingOn()) Log.logLine("Adding sequence of type " + type);
      boolean added = set.add(sequence);
      sequenceCount++;
      assert added;
  }

  /**
   * Searches through the set of active sequences to find all sequences whose
   * types match with the parameter type.
   *
   * @param type  the type desired for the sequences being sought
   * @return list of sequence objects that are of typp 'type' and abide by the
   *         constraints defined by nullOk.
   */
  public SimpleList<Sequence> getSequencesForType(ConcreteType type, boolean exactMatch) {

    if (type == null) throw new IllegalArgumentException("type cannot be null.");

    if (Log.isLoggingOn()) {
      Log.logLine("getSequencesForType: entering method, type=" + type.toString());
    }

    List<SimpleList<Sequence>> resultList = new ArrayList<>();

    if (exactMatch) {
      SimpleList<Sequence> l = this.sequenceMap.get(type);
      if (l != null) {
        resultList.add(l);
      }
    } else {
      for (ConcreteType compatibleType : typeSet.getMatches(type)) {
        resultList.add(this.sequenceMap.get(compatibleType));
      }
    }

    if (resultList.isEmpty()) {
      if (Log.isLoggingOn())
        Log.logLine("getSequencesForType: found no sequences matching type " + type);
    }
    SimpleList<Sequence> selector = new ListOfLists<>(resultList);
    if (Log.isLoggingOn())
      Log.logLine("getSequencesForType: returning " + selector.size() + " sequences.");
    return selector;
  }


  public Set<Sequence> getAllSequences() {
    Set<Sequence> result = new LinkedHashSet<>();
    for (ArrayListSimpleList<Sequence> a : sequenceMap.values()) {
      result.addAll(a.theList);
    }
    return result;
  }

}
