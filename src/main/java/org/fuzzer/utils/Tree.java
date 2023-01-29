package org.fuzzer.utils;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Tree<T> {
    private final T value;

    private final Set<Tree<T>> children;
    private Optional<Tree<T>> parent;

    public Tree(T value, Tree<T> parent) {
        this.value = value;
        this.parent = Optional.of(parent);
        this.children = new OrderedHashSet<>();
    }

    public Tree(T value) {
        this.value = value;
        this.parent = Optional.empty();
        this.children = new OrderedHashSet<>();
    }

    public T getValue() {
        return value;
    }

    public Optional<Tree<T>> getParent() {
        return parent;
    }

    public Set<Tree<T>> getChildren() {
        return children;
    }

    public Set<T> getChildrenValues() {
        return children.stream().map(Tree::getValue).collect(Collectors.toSet());
    }

    public void addChild(Tree<T> child) {
        if (child.getParent().isPresent())
            throw new IllegalArgumentException("Child with value " + child.getValue() + " already has a parent.");
        child.parent = Optional.of(this);
        this.children.add(child);
    }
    public void addChild(T value) {
        addChild(new Tree<>(value));
    }

    public void addChildren(List<T> values) {
        for (T t : values) {
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
    public boolean hasDescendant(T value) {
        return this.find(value).isPresent();
    }

    public boolean hasAncestor(T value) {
        if (this.value == value) {
            return true;
        }

        return parent.isPresent() && parent.get().hasAncestor(value);
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
