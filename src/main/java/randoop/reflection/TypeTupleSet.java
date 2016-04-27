package main.java.randoop.reflection;

import java.util.ArrayList;
import java.util.List;

import main.java.randoop.types.ConcreteType;
import main.java.randoop.types.RandoopTypeException;
import main.java.randoop.types.Substitution;
import main.java.randoop.types.TypeParameter;

/**
 * Created by bjkeller on 4/12/16.
 */
public class TypeTupleSet {

  private List<List<ConcreteType>> typeTuples;

  public TypeTupleSet() {
    typeTuples = new ArrayList<>();
    typeTuples.add(new ArrayList<ConcreteType>());
  }


  public void extend(List<ConcreteType> types) {
    List<List<ConcreteType>> tupleList = new ArrayList<>();
    for (List<ConcreteType> tuple : typeTuples) {
      for (ConcreteType type : types) {
        List<ConcreteType> extTuple = new ArrayList<>(tuple);
        extTuple.add(type);
        tupleList.add(extTuple);
      }
    }
    typeTuples = tupleList;
  }

  public List<Substitution> filter(List<TypeParameter> typeParameters) throws RandoopTypeException {
    List<Substitution> substitutionSet = new ArrayList<>();
    List<List<ConcreteType>> tupleList = new ArrayList<>();
    for (List<ConcreteType> tuple : typeTuples) {
      Substitution substitution = Substitution.forArgs(typeParameters, tuple);
      int i = 0;
      while (i < tuple.size() && typeParameters.get(i).getBound().isSatisfiedBy(tuple.get(i), substitution)) {
        i++;
      }
      if (i == tuple.size()) {
        substitutionSet.add(substitution);
        tupleList.add(tuple);
      }
    }
    typeTuples = tupleList;
    return substitutionSet;
  }
}
