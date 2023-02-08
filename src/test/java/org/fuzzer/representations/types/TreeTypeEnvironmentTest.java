package org.fuzzer.representations.types;

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
                .map(name -> (KType) new KClassType(name, true, false)).toList();

        Tree<KType> root = new Tree<>(new KClassType("Any", true, false));
        root.addChildren(Arrays.stream(new String[]{"Number", "String", "Char", "Boolean"})
                .map(name -> (KType) new KClassType(name, true, false)).toList());

        Optional<Tree<KType>> numberType = root.find(new KClassType("Number", true, false));
        numberType.get().addChildren(Arrays.stream(new String[]{"Byte", "Short", "Int", "Long"})
                .map(name -> (KType) new KClassType(name, true, false)).toList());

        RandomNumberGenerator rng = new RandomNumberGenerator(0);
        env = new TreeTypeEnvironment(root, rng);

        fakeType = new KClassType("Fake", true, false);
    }

    @Test
    void hasType() {
        for (KType type : typeList) {
            assertTrue(env.hasType(type));
            assertFalse(env.hasType(new KClassType(type.name() + "_", true, false)));
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

        assertFalse(env.isSubtypeOf(new KClassType("Byte", true, false), new KClassType("Int", true, false)));
        assertTrue(env.isSubtypeOf(new KClassType("Int", true, false), new KClassType("Number", true, false)));
    }

    @Test
    void subtypesOf() {
        Set<KType> subtypesOfRoot = env.subtypesOf(typeList.get(0));
        assertEquals(typeList.size(), subtypesOfRoot.size());

        for (KType type : typeList) {
            assertTrue(subtypesOfRoot.contains(type));
        }

        Set<KType> subtypesOfNumber = env.subtypesOf(new KClassType("Number", true, false));
        Set<KType> expectedSubtypes = Arrays.stream(new String[]{"Number", "Byte", "Short", "Int", "Long"})
                .map(name -> new KClassType(name, true, false))
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
        expectedSupertypesOfRoot.add(new KClassType("Any", true, false));

        assertEquals(expectedSupertypesOfRoot, supertypesOfRoot);

        Set<KType> expectedSubtypesOfInt = Arrays.stream(new String[]{"Int", "Number", "Any"})
                .map(name -> new KClassType(name, true, false))
                .collect(Collectors.toSet());
        Set<KType> supertypesOfInt = env.supertypesOf(new KClassType("Int", true, false));

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
        KType intType = new KClassType("Int", true, false);
        KType subInt = new KClassType("SubInt", true, false);
        KType subSubInt = new KClassType("SubSubInt", true, false);

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
        KType subInt = new KClassType("SubInt", true, false);
        KType subSubInt = new KClassType("SubSubInt", true, false);

        assertThrows(IllegalArgumentException.class,
                () -> {
                    env.addType(subSubInt, subInt);
                });

        assertThrows(IllegalArgumentException.class,
                () -> {
                    env.addType(new KClassType("Any", true, false), new KClassType("Number", true, false));
                });
    }

    @RepeatedTest(10)
    void testRandomType() {
        assertTrue(env.hasType(env.randomType()));
    }
}
