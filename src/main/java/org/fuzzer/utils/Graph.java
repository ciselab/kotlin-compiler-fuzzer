package org.fuzzer.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface Graph<T> extends Serializable {

    boolean isEmpty();
    <S extends T> Set<T> childrenOf(S node);

    <S extends T> Set<T> parentsOf(S node);

    <S extends T, U extends T> void addNode(S node, Set<U> parents);

    <S extends T> boolean contains(S node);

    <S extends T, U extends T>  boolean hasAncestor(S node, U ancestor);

    <S extends T, U extends T>  boolean hasDescendant(S node, U descendant);

    <S extends T> Set<T> allDescendants(S node);

    <S extends T> Set<T> allAncestors(S node);

    Set<T> allNodes();
}
