package org.fuzzer.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TreeTest {

    private List<String> nodeNames;
    private Tree<String> simpleTree;
    private Tree<String> twoDeepTree;
    @BeforeEach
    void setUp() {
        nodeNames = new ArrayList<>();

        for (int i = 0; i < 10; i ++) {
            nodeNames.add("Node_" + i);
        }

        simpleTree = new Tree<>(nodeNames.get(0));

        twoDeepTree = new Tree<>(nodeNames.get(0));
        twoDeepTree.addChild(nodeNames.get(1));
        twoDeepTree.addChild(nodeNames.get(2));
    }

    @Test
    void getValue() {
        assertEquals(nodeNames.get(0), simpleTree.getValue());
    }

    @Test
    void addChild() {
        assertTrue(simpleTree.getChildren().isEmpty());
        Tree<String> c0 = new Tree<>(nodeNames.get(1));

        simpleTree.addChild(c0);
        assertEquals(1, simpleTree.getChildren().size());
        assertTrue(simpleTree.getChildren().contains(c0));
        assertTrue(c0.getParent().isPresent());
        assertEquals(c0.getParent().get(), simpleTree);

        assertThrows(IllegalArgumentException.class, () -> {
            simpleTree.addChild(c0);
        });

        assertEquals(1, simpleTree.getChildren().size());
        assertTrue(simpleTree.getChildren().contains(c0));
        assertTrue(c0.getParent().isPresent());
        assertEquals(c0.getParent().get(), simpleTree);

        Tree<String> c1 = new Tree<>("", twoDeepTree);
        assertThrows(IllegalArgumentException.class, () -> {
            simpleTree.addChild(c1);
        });

        assertEquals(1, simpleTree.getChildren().size());
        assertTrue(simpleTree.getChildren().contains(c0));
        assertTrue(c0.getParent().isPresent());
        assertEquals(c0.getParent().get(), simpleTree);

        simpleTree.addChild("");
        assertEquals(2, simpleTree.getChildren().size());
        assertTrue(simpleTree.getChildren().contains(c0));
    }

    @Test
    void getChildren() {
        assertFalse(simpleTree.hasChildren());
        assertTrue(twoDeepTree.hasChildren());

        simpleTree.addChild(twoDeepTree);

        assertTrue(simpleTree.hasChildren());
        assertTrue(twoDeepTree.hasChildren());
    }
    @Test
    void equals() {
        assertEquals(simpleTree, simpleTree);

        Tree<String> simpleClone = new Tree<>(nodeNames.get(0));
        Tree<String> differentValue = new Tree<>(nodeNames.get(1));
        Tree<String> differentParent = new Tree<>(nodeNames.get(0), new Tree<>(nodeNames.get(1)));
        Tree<String> differentChildren = new Tree<>(nodeNames.get(0));
        differentChildren.addChild(new Tree<>(nodeNames.get(1)));
        Tree<Integer> differentType = new Tree<>(0);

        assertEquals(simpleTree, simpleClone);
        assertNotEquals(simpleTree, differentValue);
        assertNotEquals(simpleTree, differentParent);
        assertNotEquals(simpleTree, differentChildren);
        assertNotEquals(simpleTree, differentType);
        assertNotEquals(simpleTree, 0);
    }

    @Test
    void getChildrenValues() {
        Set<String> values = twoDeepTree.getChildrenValues();

        assertEquals(2, values.size());

        for (String v : values){
            assertTrue(twoDeepTree.getChildren().stream().anyMatch(ch -> ch.getValue().equals(v)));
        }
    }

    @Test
    void addChildren() {
        List<String> toAdd = nodeNames.subList(1, nodeNames.size() - 1);
        simpleTree.addChildren(toAdd);

        Set<Tree<String>> children = simpleTree.getChildren();

        assertEquals(toAdd.size(), children.size());
        for (Tree<String> c : children) {
            assertTrue(c.getParent().isPresent());
            assertEquals(simpleTree, c.getParent().get());
            assertTrue(toAdd.stream().anyMatch(name -> name.equals(c.getValue())));
        }
    }

    @Test
    void findSame() {
        Optional<Tree<String>> result = simpleTree.find(simpleTree.getValue());

        assertTrue(result.isPresent());
        assertEquals(simpleTree, result.get());
    }

    @Test
    void findNone() {
        Optional<Tree<String>> result = simpleTree.find("");
        assertTrue(result.isEmpty());
    }

    @Test
    void findDeeperLevel() {
        Tree<String> c3 = new Tree<>(nodeNames.get(3));
        Tree<String> c4 = new Tree<>(nodeNames.get(4));
        c3.addChild(c4);

        Optional<Tree<String>> c2 = twoDeepTree.find(nodeNames.get(2));
        assertTrue(c2.isPresent());

        c2.get().addChild(c3);

        Optional<Tree<String>> optc3 = twoDeepTree.find(nodeNames.get(3));
        Optional<Tree<String>> optc4 = twoDeepTree.find(nodeNames.get(4));

        assertTrue(optc3.isPresent());
        assertTrue(optc4.isPresent());

        assertEquals(c3, optc3.get());
        assertEquals(c4, optc4.get());
    }

    @Test
    void hasDescendant() {
        for (Tree<String> child : twoDeepTree.getChildren()) {
            assertTrue(twoDeepTree.hasDescendant(child.getValue()));
            assertFalse(simpleTree.hasDescendant(child.getValue()));
        }
    }

    @Test
    void hasAncestor() {
        for (Tree<String> child : twoDeepTree.getChildren()) {
            assertTrue(child.hasAncestor(twoDeepTree.getValue()));
            assertTrue(child.hasAncestor(simpleTree.getValue()));
        }
    }
}