package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.callables.KFunction;
import org.fuzzer.representations.types.KType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CodeBlockTest {

    CodeBlock bigBlock;
    KCallable x, y, z, a, b;
    CodeFragment xFrag, yFrag, zFrag, aFrag, bFrag;
    CodeSnippet xSnippet, ySnippet, zSnippet, aSnippet, bSnippet;

    @BeforeEach
    void setUp() {
        /*
         * fun x() {}
         * fun y() { x() }
         * fun z() { y() }
         * fun a() {}
         * fun b() { y(); a() }
         *
         * (a) <- (b)
         *         |
         *         v
         * (x) <- (y) <- (z)
         */
        x = new KFunction("x", List.of(), null);
        xFrag = CodeFragment
                .textCodeFragment("fun x() {}")
                .withNewType(FragmentType.FUNC);
        xSnippet = new CodeSnippet(xFrag, "x", Set.of(), x, new FuzzerStatistics(), SnippetType.FUNC);

        y = new KFunction("y", List.of(), null);
        yFrag = CodeFragment
                .textCodeFragment("fun y() {x()}")
                .withNewType(FragmentType.FUNC)
                .withNewDependencies(Set.of(x));
        ySnippet = new CodeSnippet(yFrag, "y", Set.of(x), y, new FuzzerStatistics(), SnippetType.FUNC);

        z = new KFunction("z", List.of(), null);
        zFrag = CodeFragment
                .textCodeFragment("fun z() {y()}")
                .withNewType(FragmentType.FUNC)
                .withNewDependencies(Set.of(x, y));
        zSnippet = new CodeSnippet(zFrag, "z", Set.of(y), z, new FuzzerStatistics(), SnippetType.FUNC);

        a = new KFunction("a", List.of(), null);
        aFrag = CodeFragment
                .textCodeFragment("fun a() {}")
                .withNewType(FragmentType.FUNC);
        aSnippet = new CodeSnippet(aFrag, "a", Set.of(), a, new FuzzerStatistics(), SnippetType.FUNC);

        b = new KFunction("b", List.of(), null);
        bFrag = CodeFragment
                .textCodeFragment("fun b() {a() y()}")
                .withNewType(FragmentType.FUNC)
                .withNewDependencies(Set.of(a, y));
        bSnippet = new CodeSnippet(bFrag, "b", Set.of(a, y), b, new FuzzerStatistics(), SnippetType.FUNC);

        bigBlock = new CodeBlock(List.of(xSnippet, ySnippet, zSnippet, aSnippet, bSnippet));
    }

    @Test
    void getUpStreamDependentsTrivial() {
        List<CodeSnippet> upStreamDependents = bigBlock.getUpStreamDependents(bSnippet);
        assertEquals(List.of(bSnippet), upStreamDependents);
    }

    @Test
    void getUpStreamDependentsOneLevel() {
        List<CodeSnippet> upStreamDependents = bigBlock.getUpStreamDependents(aSnippet);
        assertEquals(List.of(aSnippet, bSnippet), upStreamDependents);
    }

    @Test
    void getUpStreamDependentsTwoLevels() {
        List<CodeSnippet> upStreamDependents = bigBlock.getUpStreamDependents(xSnippet);

        assertTrue(upStreamDependents.containsAll(List.of(xSnippet, ySnippet, zSnippet, bSnippet)));
        assertEquals(4, upStreamDependents.size());
        assertTrue(upStreamDependents.indexOf(xSnippet) < upStreamDependents.indexOf(ySnippet));
        assertTrue(upStreamDependents.indexOf(ySnippet) < upStreamDependents.indexOf(zSnippet));
    }

    @Test
    void getDownStreamDependenciesTrivial1() {
        List<CodeSnippet> downStreamDependencies = bigBlock.getDownStreamDependencies(xSnippet);
        assertEquals(List.of(xSnippet), downStreamDependencies);
    }

    @Test
    void getDownStreamDependenciesTrivial2() {
        List<CodeSnippet> downStreamDependencies = bigBlock.getDownStreamDependencies(aSnippet);
        assertEquals(List.of(aSnippet), downStreamDependencies);
    }

    @Test
    void getDownStreamDependenciesOneLevel() {
        List<CodeSnippet> downStreamDependencies = bigBlock.getDownStreamDependencies(ySnippet);
        assertEquals(List.of(xSnippet, ySnippet), downStreamDependencies);
    }

    @Test
    void getDownStreamDependenciesTwoLevels() {
        List<CodeSnippet> downStreamDependencies = bigBlock.getDownStreamDependencies(zSnippet);
        assertEquals(List.of(xSnippet, ySnippet, zSnippet), downStreamDependencies);
    }

    @Test
    void getDownStreamDependenciesMultipleLevels() {
        List<CodeSnippet> downStreamDependencies = bigBlock.getDownStreamDependencies(bSnippet);

        assertTrue(downStreamDependencies.containsAll(List.of(aSnippet, xSnippet, ySnippet, bSnippet)));
        assertEquals(4, downStreamDependencies.size());
        assertTrue(downStreamDependencies.indexOf(xSnippet) < downStreamDependencies.indexOf(ySnippet));
        assertTrue(downStreamDependencies.indexOf(aSnippet) < downStreamDependencies.indexOf(bSnippet));
    }

    @Test
    void getDependencyTopology1() {
        List<CodeSnippet> topology = bigBlock.getDependencyTopology(aSnippet);
        assertTrue(topology.containsAll(List.of(aSnippet, bSnippet, xSnippet, ySnippet)));
        assertEquals(4, topology.size());
        assertTrue(topology.indexOf(xSnippet) < topology.indexOf(ySnippet));
        assertTrue(topology.indexOf(aSnippet) < topology.indexOf(bSnippet));
    }

    @Test
    void getDependencyTopology2() {
        List<CodeSnippet> topology = bigBlock.getDependencyTopology(xSnippet);
        assertTrue(topology.containsAll(List.of(zSnippet, aSnippet, bSnippet, xSnippet, ySnippet)));
        assertEquals(5, topology.size());
        assertTrue(topology.indexOf(xSnippet) < topology.indexOf(ySnippet));
        assertTrue(topology.indexOf(ySnippet) < topology.indexOf(zSnippet));
        assertTrue(topology.indexOf(aSnippet) < topology.indexOf(bSnippet));
    }

    @Test
    void getDependencyTopology3() {
        List<CodeSnippet> topology = bigBlock.getDependencyTopology(ySnippet);
        assertTrue(topology.containsAll(List.of(zSnippet, aSnippet, bSnippet, xSnippet, ySnippet)));
        assertEquals(5, topology.size());
        assertTrue(topology.indexOf(xSnippet) < topology.indexOf(ySnippet));
        assertTrue(topology.indexOf(ySnippet) < topology.indexOf(zSnippet));
        assertTrue(topology.indexOf(aSnippet) < topology.indexOf(bSnippet));
    }

    @Test
    void getDependencyTopology4() {
        List<CodeSnippet> topology = bigBlock.getDependencyTopology(zSnippet);
        assertTrue(topology.containsAll(List.of(xSnippet, ySnippet, zSnippet)));
        assertEquals(3, topology.size());
        assertTrue(topology.indexOf(xSnippet) < topology.indexOf(ySnippet));
        assertTrue(topology.indexOf(ySnippet) < topology.indexOf(zSnippet));
    }
}