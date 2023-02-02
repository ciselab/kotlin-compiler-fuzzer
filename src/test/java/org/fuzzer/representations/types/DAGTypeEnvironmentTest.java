package org.fuzzer.representations.types;

import org.fuzzer.utils.RandomNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DAGTypeEnvironmentTest {

    DAGTypeEnvironment emptyEnv;
    DAGTypeEnvironment simpleEnv;

    List<KClassifierType> typeList;

    List<KClassType> numberTypes;

    List<KClassType> classes;

    KType nonClassifierType;

    KClassifierType otherType;

    @BeforeEach
    void setUp() {
        RandomNumberGenerator rng1 = new RandomNumberGenerator(0);
        emptyEnv = new DAGTypeEnvironment(rng1);

        RandomNumberGenerator rng2 = new RandomNumberGenerator(0);
        simpleEnv = new DAGTypeEnvironment(rng2);

        nonClassifierType = new KFuncType("foo", new ArrayList<>(), new ArrayList<>());

        typeList = Collections.singletonList(new KInterfaceType("Any"));
        classes = Arrays.stream(new String[]{"Number", "Byte", "Short", "Int", "Long", "String", "Char", "Boolean"})
                .map(name -> new KClassType(name, true, false)).toList();


        numberTypes = classes.subList(1, 5);

        // Any
        simpleEnv.addType(new HashSet<>(), typeList.get(0));

        // Number
        simpleEnv.addType(new HashSet<>(Collections.singleton(typeList.get(0))),
                classes.get(0));

        // Number subclasses
        for (KType t : numberTypes) {
            simpleEnv.addType(new HashSet<>(Collections.singleton(classes.get(0))), t);
        }

        // Other subclasses of Any
        for (KType t : classes.subList(5, classes.size())) {
            simpleEnv.addType(new HashSet<>(Collections.singleton(typeList.get(0))), t);
        }

        otherType = new KClassType("foo", true, false);
    }

    @Test
    void populateEnvironment() {
    }

    @Test
    void hasType() {
        for (KClassifierType type : typeList) {
            assertTrue(simpleEnv.hasType(type));
            assertFalse(emptyEnv.hasType(type));
        }

        assertFalse(simpleEnv.hasType(nonClassifierType));
        assertFalse(simpleEnv.hasType(otherType));
    }

    @Test
    void isSubtypeOf() {
        KClassType boolType = classes.get(classes.size() - 1);
        KClassType numType = classes.get(0);
        for (KClassType type : classes) {
            assertTrue(simpleEnv.isSubtypeOf(type, typeList.get(0)));
            if (!type.equals(boolType)) {
                assertFalse(simpleEnv.isSubtypeOf(type, boolType));
            }
        }

        for (KClassType type : numberTypes) {
            assertTrue(simpleEnv.isSubtypeOf(type, numType));
        }
    }

    @Test
    void supertypesOf() {
        Set<KClassifierType> parents = new HashSet<>(Collections.singleton(typeList.get(0)));
        assertEquals(parents, simpleEnv.supertypesOf(typeList.get(0)));

        parents.add(classes.get(0));
        assertEquals(parents, simpleEnv.supertypesOf(classes.get(0)));

        for (KClassType type : numberTypes) {
            Set<KClassifierType> parentsCopy = new HashSet<>(parents);
            parentsCopy.add(type);

            assertEquals(parentsCopy, simpleEnv.supertypesOf(type));
        }
    }

    @Test
    void subtypesOf() {
        Set<KClassifierType> descendants = new HashSet<>(classes);
        descendants.add(typeList.get(0));
        assertEquals(descendants, simpleEnv.subtypesOf(typeList.get(0)));

        descendants = new HashSet<>(numberTypes);
        descendants.add(classes.get(0));
        assertEquals(descendants, simpleEnv.subtypesOf(classes.get(0)));

        KClassType boolType = classes.get(classes.size() - 1);
        assertEquals(new HashSet<>(Collections.singleton(boolType)), simpleEnv.subtypesOf(boolType));
    }

    @Test
    void addType() {
        assertFalse(emptyEnv.hasType(typeList.get(0)));
        emptyEnv.addType(new HashSet<>(), typeList.get(0));
        assertTrue(emptyEnv.hasType(typeList.get(0)));
    }

    @RepeatedTest(10)
    void randomType() {
        assertTrue(simpleEnv.hasType(simpleEnv.randomType()));
    }
}