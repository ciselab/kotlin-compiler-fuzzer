package org.fuzzer.utils;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.util.*;
import java.util.stream.Collectors;

public class Tree<T> {

    private final T value;

    private final List<Tree<T>> children;

    private Optional<Tree<T>> parent;

    public Tree(T value, Tree<T> parent) {
        this.value = value;
        this.parent = Optional.of(parent);
        this.children = new ArrayList<>();
    }

    public Tree(T value) {
        this.value = value;
        this.parent = Optional.empty();
        this.children = new ArrayList<>();
    }

    public T getValue() {
        return value;
    }

    public Optional<Tree<T>> getParent() {
        return parent;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public List<T> getChildrenValues() {
        return children.stream().map(Tree::getValue).toList();
    }

    public void addChild(Tree<T> child) {
        if (child.getParent().isPresent())
            throw new IllegalArgumentException("Child with value " + child.getValue() + " already has a parent.");
        child.parent = Optional.of(this);
        this.children.add(child);
    }

    public <S extends T> void addChild(S value) {
        addChild(new Tree<>(value));
    }

    public <S extends T> void addChildren(List<S> values) {
        for (S t : values) {
            this.addChild(t);
        }
    }

    public Optional<Tree<T>> find(T value) {
        if (this.value.equals(value))
            return Optional.of(this);

        for (Tree<T> child : this.children) {
            Optional<Tree<T>> result = child.find(value);
            if (result.isPresent())
                return result;
        }
        return Optional.empty();
    }

    public <S extends T> boolean hasDescendant(S value) {
        return this.find(value).isPresent();
    }

    public <S extends T> boolean hasAncestor(S value) {
        if (this.value.equals(value)) {
            return true;
        }

        return parent.isPresent() && parent.get().hasAncestor(value);
    }

    public Set<Tree<T>> allDescendants() {
        Set<Tree<T>> allSubtrees = new HashSet<>();
        allSubtrees.add(this);

        for (Tree<T> child : this.getChildren()) {
            allSubtrees.addAll(child.allDescendants());
        }

        return allSubtrees;
    }

    public List<T> toList() {
        return allDescendants().stream()
                .map(Tree::getValue)
                .collect(Collectors.toList());
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof Tree))
            return false;

        Tree<T> otherTree = (Tree<T>) other;

        if (!this.value.equals((otherTree).getValue()))
            return false;

        if (!this.children.equals(otherTree.getChildren()))
            return false;

        return this.getParent().equals(otherTree.getParent());
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
