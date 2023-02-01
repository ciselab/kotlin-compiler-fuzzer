package org.fuzzer.representations.types;

import org.fuzzer.utils.ConstrainedDAG;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.*;

public class DAGTypeEnvironment implements TypeEnvironment {

    private ConstrainedDAG dag;

    private RandomNumberGenerator rng;

    public DAGTypeEnvironment(RandomNumberGenerator rng, ConstrainedDAG dag) {
        this.rng = rng;
        this.dag = dag;
    }

    public DAGTypeEnvironment(RandomNumberGenerator rng) {
        this.rng = rng;
        dag = new ConstrainedDAG<KType>(
                t -> t instanceof KClassifierType,
                parents -> {
                    boolean allClassifiers = parents.stream().allMatch(t -> t instanceof KClassifierType);

                    if (!allClassifiers)
                        return false;

                    List<KType> classes = parents.stream().filter(t -> t instanceof KClassType).toList();
                    if (classes.isEmpty())
                        return true;

                    return classes.size() == 1;
                }
        );
    }

    @Override
    public void populateEnvironment() {

    }

    @Override
    public boolean hasType(KType type) {
        return dag.contains(type);
    }

    @Override
    public boolean isSubtypeOf(KType subtype, KType supertype) {
        return dag.hasAncestor(subtype, supertype);
    }

    @Override
    public Set<KType> subtypesOf(KType type) {
        return dag.allDescendants(type);
    }

    @Override
    public Set<KType> supertypesOf(KType type) {
        return dag.allAncestors(type);
    }

    @Override
    public void addType(KType parent, KType newType) {
        dag.addNode(newType, Collections.singleton(newType));
    }

    @Override
    public KType randomType() {
        List<KType> typeList = dag.allNodes().stream().toList();
        return typeList.get(rng.fromUniformDiscrete(0, typeList.size() - 1));
    }
}
