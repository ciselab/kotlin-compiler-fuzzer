package org.fuzzer.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class ConstrainedDAGTest {

    interface AbstractBase {
        boolean foo();
    }

    record ConcreteClass(boolean foo, int num) implements AbstractBase {
        @Override
        public int hashCode() {
            return num;
        }
    }

    private ConstrainedDAG<AbstractBase> emptyDAG;
    private Predicate<AbstractBase> nodePred;
    private Predicate<Set<AbstractBase>> parentsPred;

    private ConstrainedDAG<AbstractBase> populatedDAG;
    private Set<AbstractBase> allNodes;

    ConcreteClass c1, c2, c3, c4, c5;

    @BeforeEach
    void setUp() {
        nodePred = AbstractBase::foo;
        parentsPred = v -> v.stream().allMatch(AbstractBase::foo);
        emptyDAG = new ConstrainedDAG<>(nodePred, parentsPred);
        populatedDAG = new ConstrainedDAG<>(nodePred, parentsPred);

        c1 = new ConcreteClass(true, 1);
        c2 = new ConcreteClass(true, 2);
        c3 = new ConcreteClass(true, 3);
        c4 = new ConcreteClass(true, 4);
        c5 = new ConcreteClass(false, 4);

        populatedDAG.addNode(c1, new HashSet<>());
        populatedDAG.addNode(c2, new HashSet<>(Collections.singleton(c1)));
        Set<AbstractBase> c3Parents = new HashSet<>();
        c3Parents.add(c1);
        c3Parents.add(c2);
        populatedDAG.addNode(c3, c3Parents);
        populatedDAG.addNode(c4, new HashSet<>(Collections.singleton(c3)));

        /*
                          c1
                        /    \
                       c2 -> c3 -> c4
         */

        allNodes = new HashSet<>();
        allNodes.add(c1);
        allNodes.add(c2);
        allNodes.add(c3);
        allNodes.add(c4);
    }

    @Test
    void isEmpty() {
        assertTrue(emptyDAG.isEmpty());

        emptyDAG.addNode(new ConcreteClass(true, 2), new HashSet<>());

        assertFalse(emptyDAG.isEmpty());
    }

    @Test
    void allDescendants() {
        Set<AbstractBase> c1Descendants = new HashSet<>();
        c1Descendants.add(c1);
        c1Descendants.add(c2);
        c1Descendants.add(c3);
        c1Descendants.add(c4);

        Set<AbstractBase> c2Descendants = new HashSet<>();
        c2Descendants.add(c2);
        c2Descendants.add(c3);
        c2Descendants.add(c4);

        assertEquals(c1Descendants, populatedDAG.allDescendants(c1));
        assertEquals(c2Descendants, populatedDAG.allDescendants(c2));
        assertEquals(new HashSet<>(Collections.singleton(c4)), populatedDAG.allDescendants(c4));
    }

    @Test
    void allAncestors() {
        Set<AbstractBase> c4Ancestors = new HashSet<>();
        c4Ancestors.add(c1);
        c4Ancestors.add(c2);
        c4Ancestors.add(c3);
        c4Ancestors.add(c4);

        Set<AbstractBase> c2Ancestors = new HashSet<>();
        c2Ancestors.add(c2);
        c2Ancestors.add(c1);

        assertEquals(c4Ancestors, populatedDAG.allAncestors(c4));
        assertEquals(c2Ancestors, populatedDAG.allAncestors(c2));
        assertEquals(new HashSet<>(Collections.singleton(c1)), populatedDAG.allAncestors(c1));
    }

    @Test
    void allNodes() {

        assertEquals(allNodes, populatedDAG.allNodes());
        assertTrue(emptyDAG.allNodes().isEmpty());
    }

    @Test
    void hasAncestor() {
        for (AbstractBase node : allNodes) {
            assertTrue(populatedDAG.hasAncestor(c4, node));
        }

        assertFalse(populatedDAG.hasAncestor(c1, c2));
        assertTrue(populatedDAG.hasAncestor(c1, c1));
    }

    @Test
    void hasDescendant() {
        for (AbstractBase node : allNodes) {
            assertTrue(populatedDAG.hasAncestor(c4, node));
        }

        assertFalse(populatedDAG.hasAncestor(c1, c2));
        assertTrue(populatedDAG.hasAncestor(c1, c1));
    }

    @Test
    void contains() {
        for (AbstractBase node : allNodes) {
            assertTrue(populatedDAG.contains(node));
            assertFalse(emptyDAG.contains(node));
        }
    }

    @Test
    void addNodeSimple() {
        emptyDAG.addNode(c1, new HashSet<>());
        assertTrue(emptyDAG.contains(c1));
    }

    @Test
    void addNodeChain() {
        emptyDAG.addNode(c1, new HashSet<>());
        emptyDAG.addNode(c2, new HashSet<>(Collections.singleton(c1)));
        emptyDAG.addNode(c3, new HashSet<>(Collections.singleton(c2)));
        emptyDAG.addNode(c4, new HashSet<>(Collections.singleton(c3)));

        for (AbstractBase node : allNodes) {
            assertTrue(emptyDAG.contains(node));
        }
    }

    @Test
    void addNodeAlreadyExists() {
        emptyDAG.addNode(c1, new HashSet<>());

        assertTrue(emptyDAG.contains(c1));

        assertThrows(IllegalArgumentException.class,
                () -> {
                    emptyDAG.addNode(c1, new HashSet<>());
                });
    }

    @Test
    void addNodeInvariantFailure() {
        assertThrows(IllegalArgumentException.class,
                () -> {
                    emptyDAG.addNode(new ConcreteClass(false, 0), new HashSet<>());
                });
    }

    @Test
    void addNodeParentsNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> {
                    emptyDAG.addNode(c1, new HashSet<>(Collections.singleton(c2)));
                });
    }

    @Test
    void addNodeParentsInvariantFailure() {
        Predicate<Set<AbstractBase>> parentsPred = s -> s.size() == 0 || s.size() > 2;
        ConstrainedDAG<AbstractBase> dag = new ConstrainedDAG<>(nodePred, parentsPred);
        dag.addNode(c1, new HashSet<>());
        dag.addNode(c2, new HashSet<>());

        Set<AbstractBase> parents = new HashSet<>();
        parents.add(c1);
        parents.add(c2);
        assertThrows(IllegalArgumentException.class,
                () -> {
                    dag.addNode(c3, parents);
                });
    }

    @Test
    void parentsOf() {
       Set<AbstractBase> parents = new HashSet<>();
       assertEquals(parents, populatedDAG.parentsOf(c1));

       parents.add(c1);
       assertEquals(parents, populatedDAG.parentsOf(c2));

       parents.add(c2);
       assertEquals(parents, populatedDAG.parentsOf(c3));

       assertEquals(new HashSet<>(Collections.singleton(c3)), populatedDAG.parentsOf(c4));
    }

    @Test
    void childrenOf() {
        Set<AbstractBase> children = new HashSet<>(Collections.singleton(c2));
        children.add(c3);
        assertEquals(children, populatedDAG.childrenOf(c1));

        assertEquals(new HashSet<>(Collections.singleton(c3)), populatedDAG.childrenOf(c2));
        assertEquals(new HashSet<>(Collections.singleton(c4)), populatedDAG.childrenOf(c3));
        assertTrue(populatedDAG.childrenOf(c4).isEmpty());
    }
}