package org.fuzzer.utils;

import kotlin.TuplesKt;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConstrainedDAG<T> implements Graph<T> {

    private final Map<T, Set<T>> childrenList;

    private final Map<T, Set<T>> parentList;

    private final Predicate<T> newNodeInvariant;

    private final Predicate<Set<T>> nodeParentsInvariant;

    private final Map<Tuple<T, T>, Label<T>> edges;

    public ConstrainedDAG(Predicate<T> newNodeInvariant, Predicate<Set<T>> nodeParentsInvariant) {
        childrenList = new HashMap<>();
        parentList = new HashMap<>();
        this.newNodeInvariant = newNodeInvariant;
        this.nodeParentsInvariant = nodeParentsInvariant;
        this.edges = new HashMap<>();
    }

    public ConstrainedDAG() {
        childrenList = new HashMap<>();
        parentList = new HashMap<>();
        newNodeInvariant = x -> true;
        nodeParentsInvariant = x -> true;
        this.edges = new HashMap<>();
    }

    @Override
    public boolean isEmpty() {
        return childrenList.isEmpty() && parentList.isEmpty();
    }

    @Override
    public <S extends T> Set<T> allDescendants(S node) {
        verifyExists(node);

        Set<T> descendants = new HashSet<>();
        descendants.add(node);

        for (T child : childrenList.get(node)) {
            descendants.addAll(allDescendants(child));
        }

        return descendants;
    }

    @Override
    public <S extends T> Set<T> allAncestors(S node) {
        verifyExists(node);

        Set<T> ancestors = new HashSet<>();
        ancestors.add(node);

        for (T parent : parentList.get(node)) {
            ancestors.addAll(allAncestors(parent));
        }

        return ancestors;
    }

    @Override
    public Set<T> allNodes() {
        return childrenList.keySet();
    }

    @Override
    public <S extends T, U extends T> boolean hasAncestor(S node, U ancestor) {
        verifyExists(ancestor);
        verifyExists(node);

        return allAncestors(node).contains(ancestor);
    }

    @Override
    public <S extends T, U extends T>  boolean hasDescendant(S node, U descendant) {
        verifyExists(descendant);
        verifyExists(node);

        return allDescendants(node).contains(descendant);
    }

    @Override
    public <S extends T> boolean contains(S node) {
        return childrenList.containsKey(node);
    }

    @Override
    public <S extends T, U extends T> void addNode(S node, Set<U> parents) {
        verifyNotExists(node);

        if (!newNodeInvariant.test(node)) {
            throw new IllegalArgumentException("New node invariant violated");
        }

        for (U parent : parents) {
            verifyExists(parent);
            childrenList.get(parent).add(node);
        }

        if (!nodeParentsInvariant.test((Set<T>) parents)) {
            throw new IllegalArgumentException("Parents invariant violated");
        }

        Set<T> copy = new HashSet<>(parents);

        parentList.put(node, copy);
        childrenList.put(node, new HashSet<>());
    }

    @Override
    public <S extends T> Set<T> parentsOf(S node) {
        verifyExists(node);
        return parentList.get(node);
    }

    @Override
    public <S extends T> Set<T> childrenOf(S node) {
        verifyExists(node);
        return childrenList.get(node);
    }

    public <S extends T> void verifyExists(S node) {
        if (!(parentList.containsKey(node) && childrenList.containsKey(node))) {
            throw new IllegalArgumentException("Node " + node + " does not exist in the graph.");
        }
    }

    private <S extends T> void verifyNotExists(S node) {
        if (parentList.containsKey(node) || childrenList.containsKey(node)) {
            throw new IllegalArgumentException("Node " + node + " does already exists in the graph.");
        }
    }

    public List<T> allEntries() {
        return parentList.keySet().stream().toList();
    }

    public void labelEdge(T from, T to, List<T> conditions) {
        verifyExists(from);
        verifyExists(to);
        if (isLabeled(from, to)) {
            throw new IllegalArgumentException("Edge from " + from + " to " + to + " is already labeled " + getLabel(from, to));
        }

        edges.put(new Tuple<>(from, to), new Label<>(conditions));
    }

    public boolean isLabeled(T from, T to) {
        return edges.containsKey(new Tuple<>(from, to));
    }

    public Label<T> getLabel(T from, T to) {
        return isLabeled(from, to) ? edges.get(new Tuple<>(from, to)) : null;
    }

    public List<T> labeledChildren(T from) {
        return edges.keySet().stream()
                .filter(tLabel -> tLabel.first().equals(from))
                .map(Tuple::second)
                .toList();
    }

    public List<T> pathBetween(T start, T end, List<T> path) {
        verifyExists(start);
        verifyExists(end);

        path.add(start);

        if (start.equals(end)) {
            return path;
        }

        if (childrenOf(start).isEmpty()) {
            return new ArrayList<>();
        }

        for (T child : childrenOf(start)) {
            List<T> res = pathBetween(child, end, path);
            if (!res.isEmpty()) {
                return res;
            }
        }

        throw new IllegalArgumentException("No path between " + start + " and " + end);
    }
}
