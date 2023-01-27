package org.fuzzer.utils;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Tree<T> {
    private final T value;

    private final Set<Tree<T>> children;
    private Tree<T> parent;



    public Tree(T value, Tree<T> parent) {
        this.value = value;
        this.parent = parent;
        this.children = new OrderedHashSet<>();
    }

    public Tree(T value) {
        this(value, null);
    }

    public T getValue() {
        return value;
    }

    public Set<Tree<T>> getChildren() {
        return children;
    }

    public Set<T> getChildrenValues() {
        return children.stream().map(Tree::getValue).collect(Collectors.toSet());
    }

    public void addChild(Tree<T> child) {
        child.parent = this;
        this.children.add(child);
    }
    public void addChild(T value) {
        addChild(new Tree<>(value, this));
    }

    public void addChildren(List<T> values) {
        for (T t : values) {
            this.addChild(t);
        }
    }

    public Tree<T> find(T value) {
        if (this.value.equals(value))
            return this;

        for (Tree<T> child : this.children) {
            Tree<T> result = child.find(value);
            if (result != null)
                return result;
        }

        return null;
    }
    public boolean hasDescendant(T value) {
        return this.find(value) != null;
    }

    public boolean hasAncestor(T value) {
        if (this.value == value) {
            return true;
        }

        return parent != null && parent.hasAncestor(value);
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof Tree))
            return false;

        return this.value.equals(((Tree<?>) other).value);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
