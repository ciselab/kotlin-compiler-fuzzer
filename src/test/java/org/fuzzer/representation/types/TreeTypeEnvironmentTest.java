package org.fuzzer.representation.types;

import org.fuzzer.representations.types.KType;
import org.fuzzer.representations.types.TreeTypeEnvironment;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TreeTypeEnvironmentTest {
    private List<KType> typeList;

    private TreeTypeEnvironment env;

    private KType fakeType;

    @BeforeEach
    void setUp() {
        typeList = Arrays.stream(new String[]{"Any", "Number", "Byte", "Short", "Int", "Long", "String", "Char", "Boolean"})
                .map(KType::new).toList();

        Tree<KType> root = new Tree<>(new KType("Any"));
        root.addChildren(Arrays.stream(new String[]{"Number", "String", "Char", "Boolean"}).map(KType::new).toList());

        Optional<Tree<KType>> numberType = root.find(new KType("Number"));
        numberType.get().addChildren(Arrays.stream(new String[]{"Byte", "Short", "Int", "Long"}).map(KType::new).toList());

        RandomNumberGenerator rng = new RandomNumberGenerator(0);
        env = new TreeTypeEnvironment(root, rng);

        fakeType = new KType("Fake");
    }

    @Test
    void hasType() {
        for (KType type : typeList) {
            assertTrue(env.hasType(type));
            assertFalse(env.hasType(new KType(type.getName() + "_")));
        }
    }

    @Test
    void isSubtypeOf() {
        KType root = typeList.get(0);
        for (KType type : typeList) {
            assertTrue(env.isSubtypeOf(type, root));
            assertThrows(IllegalArgumentException.class,
                    () -> {
                        env.isSubtypeOf(type, fakeType);
                    });
            assertThrows(IllegalArgumentException.class,
                    () -> {
                        env.isSubtypeOf(fakeType, type);
                    });
        }

        assertFalse(env.isSubtypeOf(new KType("Byte"), new KType("Int")));
        assertTrue(env.isSubtypeOf(new KType("Int"), new KType("Number")));
    }

    @Test
    void subtypesOf() {
        Set<KType> subtypesOfRoot = env.subtypesOf(typeList.get(0));
        assertEquals(typeList.size(), subtypesOfRoot.size());

        for (KType type : typeList) {
            assertTrue(subtypesOfRoot.contains(type));
        }

        Set<KType> subtypesOfNumber = env.subtypesOf(new KType("Number"));
        Set<KType> expectedSubtypes = Arrays.stream(new String[]{"Number", "Byte", "Short", "Int", "Long"})
                .map(KType::new)
                .collect(Collectors.toSet());

        assertEquals(expectedSubtypes, subtypesOfNumber);
    }

    @Test
    void subtypesOfException() {
        assertThrows(IllegalArgumentException.class,
                () -> {
                    env.subtypesOf(fakeType);
                });
    }

    @Test
    void supertypesOf() {
        Set<KType> supertypesOfRoot = env.supertypesOf(typeList.get(0));
        Set<KType> expectedSupertypesOfRoot = new HashSet<>();
        expectedSupertypesOfRoot.add(new KType("Any"));

        assertEquals(expectedSupertypesOfRoot, supertypesOfRoot);

        Set<KType> expectedSubtypesOfInt = Arrays.stream(new String[]{"Int", "Number", "Any"})
                .map(KType::new)
                .collect(Collectors.toSet());
        Set<KType> supertypesOfInt = env.supertypesOf(new KType("Int"));

        assertEquals(expectedSubtypesOfInt, supertypesOfInt);
    }

    @Test
    void supertypesOfException() {
        assertThrows(IllegalArgumentException.class,
                () -> {
                    env.supertypesOf(fakeType);
                });
    }

    @Test
    void addType() {
        KType intType = new KType("Int");
        KType subInt = new KType("SubInt");
        KType subSubInt = new KType("SubSubInt");

        env.addType(intType, subInt);
        env.addType(subInt, subSubInt);

        assertTrue(env.hasType(subInt));
        assertTrue(env.hasType(subSubInt));

        for (KType type : typeList) {
            assertFalse(env.isSubtypeOf(type, subInt));
            assertFalse(env.isSubtypeOf(type, subSubInt));
        }

        for (KType type : env.supertypesOf(intType)) {
            assertTrue(env.isSubtypeOf(subInt, type));
            assertTrue(env.isSubtypeOf(subSubInt, type));
        }

        assertTrue(env.isSubtypeOf(subSubInt, subInt));
    }

    @Test
    void addTypeException() {
        KType subInt = new KType("SubInt");
        KType subSubInt = new KType("SubSubInt");

        assertThrows(IllegalArgumentException.class,
                () -> {
                    env.addType(subSubInt, subInt);
                });

        assertThrows(IllegalArgumentException.class,
                () -> {
                    env.addType(new KType("Any"), new KType("Number"));
                });
    }

    @RepeatedTest(10)
    void testRandomType() {
        assertTrue(env.hasType(env.randomType()));
    }
}
