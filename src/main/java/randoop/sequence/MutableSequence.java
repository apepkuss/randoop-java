package main.java.randoop.sequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.randoop.BugInRandoopException;
import main.java.randoop.Globals;
import main.java.randoop.operation.Operation;
import main.java.randoop.util.CollectionsExt;

/**
 * A sequence that can be mutated (unlike a {@code Sequence}, which is
 * immutable).
 */
public class MutableSequence {

  public List<MutableStatement> statements;

  /**
   * Checks the following well-formedness properties for every statement in the
   * list of statements:
   * <ul>
   * <li>The number of inputs is the same as the input of inputs specified by
   * the statement kind.
   * <li>Type type of each input is compatible with the type required by the
   * statement kind.
   * <li>The input variables come from earlier statements.
   * <li>The result variable does not appear in earlier statements.
   * </ul>
   */
  public void checkRep() {
    Set<MutableVariable> prevVars = new LinkedHashSet<>();
    for (MutableStatement st : statements) {
      assert st.inputs.size() == st.operation.getInputTypes().size();
      for (int i = 0; i < st.inputs.size(); i++) {
        MutableVariable in = st.inputs.get(i);
        assert prevVars.contains(in) : this;
        assert in.owner == this : this;
        assert st.operation.getInputTypes().get(i).isAssignableFrom(in.getType());
      }
      assert !prevVars.contains(st.result);
      prevVars.add(st.result);
    }
  }

  public MutableSequence makeCopy() {
    MutableSequence newSeq = new MutableSequence();

    // Create a list of new variables, one per index.
    List<MutableVariable> newvars = new ArrayList<>();
    for (int i = 0; i < size(); i++) {
      newvars.add(new MutableVariable(newSeq, getVariable(i).getName()));
    }

    // Create a list of new statements that use the new variables.
    List<MutableStatement> statements = new ArrayList<>();
    for (int i = 0; i < size(); i++) {
      MutableStatement sti = this.statements.get(i);
      List<MutableVariable> newinputs = new ArrayList<>();
      for (MutableVariable v : sti.inputs) {
        newinputs.add(newvars.get(v.getDeclIndex()));
      }
      statements.add(new MutableStatement(sti.operation, newinputs, newvars.get(i)));
    }

    // Set the statements of the new sequence to the new statements.
    newSeq.statements = statements;
    checkRep();
    return newSeq;
  }

  /**
   * Returns all the indices of statements where v is an input to the statement.
   */
  public List<Integer> getUses(MutableVariable v) {
    List<Integer> uses = new ArrayList<>();
    // All uses will come after declaration.
    for (int i = v.getDeclIndex() + 1; i < size(); i++) {
      if (statements.get(i).inputs.contains(v)) uses.add(i);
    }
    return uses;
  }

  @SuppressWarnings("unchecked")
  public int numInfluencingStatements(int maxIdx, List<MutableVariable> vars) {
    Set<MutableVariable> influencingVars = new LinkedHashSet<>();
    for (MutableVariable v : vars) {
      findInfluencingVars(v, influencingVars);
    }
    int count = 0;
    for (int i = 0; i <= maxIdx; i++) {
      MutableStatement st = statements.get(i);
      Set<MutableVariable> statementVars = new LinkedHashSet<>(st.inputs);
      statementVars.add(st.result);
      if (!CollectionsExt.intersection(influencingVars, statementVars).isEmpty()) {
        count++;
      }
    }
    return count;
  }

  private void findInfluencingVars(MutableVariable v, Set<MutableVariable> infvars) {
    infvars.add(v);

    for (MutableVariable v2 : v.getCreatingStatementWithInputs().inputs) {
      if (v2.getCreatingStatementWithInputs().operation.isNonreceivingValue()) continue;
      if (!infvars.contains(v2)) {
        infvars.add(v2);
        findInfluencingVars(v2, infvars);
      }
    }

    for (Integer i : getUses(v)) {

      MutableVariable result = statements.get(i).result;
      if (!infvars.contains(result)) {
        assert !(result.getCreatingStatementWithInputs().operation.isNonreceivingValue());
        infvars.add(result);
        findInfluencingVars(result, infvars);
      }

      for (MutableVariable v2 : statements.get(i).inputs) {
        if (v2.getCreatingStatementWithInputs().operation.isNonreceivingValue()) continue;
        if (!infvars.contains(v2)) {
          infvars.add(v2);
          findInfluencingVars(v2, infvars);
        }
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (MutableStatement st : statements) {
      b.append(st.toString());
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  public MutableStatement getDeclaringStatement(MutableVariable v) {
    return statements.get(getIndex(v));
  }

  public MutableStatement getStatement(int i) {
    return statements.get(i);
  }

  public int getIndex(MutableVariable v) {
    if (v == null) throw new IllegalArgumentException();
    if (v.owner != this) throw new IllegalArgumentException();
    for (int i = 0; i < statements.size(); i++) {
      MutableStatement st = statements.get(i);
      if (st.result == v) return i;
    }
    throw new BugInRandoopException();
  }

  public int size() {
    return statements.size();
  }

  public MutableVariable getVariable(int i) {
    if (i < 0 || i >= this.size()) throw new IllegalArgumentException();
    return this.statements.get(i).result;
  }

  public Sequence toImmutableSequence() {
    Sequence seq = new Sequence();
    for (int i = 0; i < this.size(); i++) {
      List<Variable> inputs = new ArrayList<>();
      for (MutableVariable sv : this.statements.get(i).inputs) {
        inputs.add(seq.getVariable(sv.getDeclIndex()));
      }
      seq = seq.extend(this.statements.get(i).operation, inputs);
    }
    return seq;
  }

  /** A compilable (Java source code) representation of this sequence. */
  public String toCodeString() {
    return toImmutableSequence().toCodeString();
  }

  /**
   * inserts the statements making up the given sequence into this sequence, at
   * the given index. The parameter sequence is left unchanged. The receiver
   * sequence is mutated. For convenience, returns a mapping from variables in
   * the parameter sequence to their corresponding variables in the modified
   * receiver sequence.
   *
   * @param index
   *          The index at which to insert the given sequence.
   * @param seq
   *          The sequence to insert.
   * @return A mapping from variables in the parameter sequence to their
   *         corresponding variables in this sequence.
   */
  public Map<MutableVariable, MutableVariable> insert(int index, MutableSequence seq) {
    if (seq == null) {
      throw new IllegalArgumentException("seq cannot be null.");
    }
    if (index < 0 || index >= size()) {
      String msg = "Invalid index: " + index + " for sequence " + toString();
      throw new IllegalArgumentException(msg);
    }

    List<MutableStatement> sts = new ArrayList<>();

    Map<MutableVariable, MutableVariable> varmap = new LinkedHashMap<>();

    for (MutableStatement oldst : seq.statements) {

      // Create input list for statement.
      List<MutableVariable> newInputs = new ArrayList<>(oldst.inputs.size());
      for (MutableVariable var : oldst.inputs) {
        MutableVariable newvar = varmap.get(var);
        assert newvar != null;
        assert newvar.owner == this;
        newInputs.add(newvar);
      }
      MutableVariable newvar = new MutableVariable(this, oldst.result.getName());
      varmap.put(oldst.result, newvar);
      MutableStatement newStatement = new MutableStatement(oldst.operation, newInputs, newvar);
      sts.add(newStatement);
    }

    this.statements.addAll(index, sts);
    checkRep();
    return varmap;
  }

  public List<MutableVariable> getInputs(int statementIndex) {
    return statements.get(statementIndex).inputs;
  }

  public Operation getOperation(int statementIndex) {
    return statements.get(statementIndex).operation;
  }
}
