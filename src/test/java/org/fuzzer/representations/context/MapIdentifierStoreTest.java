package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.callables.KIdentifierCallable;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.representations.types.TreeTypeEnvironment;
import org.fuzzer.representations.types.TypeEnvironment;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MapIdentifierStoreTest {

    private MapIdentifierStore emptyStore;

    private MapIdentifierStore store;

    private TypeEnvironment env;

    private List<String> idNames;

    KType intType;
    KType stringType;

    KType fakeType;


    @BeforeEach
    void setUp() {
        intType = new KClassType("Int", true, false);
        stringType = new KClassType("String", true, false);
        RandomNumberGenerator rng = new RandomNumberGenerator(0);

        Tree<KType> root = new Tree<>(new KClassType("Any", true, true));
        root.addChildren(Arrays.stream(new String[]{"Number", "String", "Char", "Boolean"}).map(name -> new KClassType(name, true, false)).toList());

        Optional<Tree<KType>> numberType = root.find(new KClassType("Number", true, false));
        numberType.get().addChildren(Arrays.stream(new String[]{"Byte", "Short", "Int", "Long"}).map(name -> new KClassType(name, true, false)).toList());

        env = new TreeTypeEnvironment(root, rng);

        emptyStore = new MapIdentifierStore(env, rng);

        store = new MapIdentifierStore(env, rng);
        store.addIdentifier("x", new KIdentifierCallable("x", intType));
        store.addIdentifier("y", new KIdentifierCallable("y", stringType));
        store.addIdentifier("z", new KIdentifierCallable("z", stringType));

        fakeType = new KClassType("String", true, false);
    }

    @Test
    void isEmpty() {
        assertTrue(emptyStore.isEmpty());
        assertFalse(store.isEmpty());
    }

    @Test
    void identifiersOfType() {
        List<KCallable> numCallables = store.identifiersOfType(new KClassType("Number", true, false) {
        });

        assertEquals(1, numCallables.size());
        assertInstanceOf(KIdentifierCallable.class, numCallables.get(0));

        List<KCallable> stringCallables = store.identifiersOfType(stringType);
        assertEquals(2, stringCallables.size());
        assertInstanceOf(KIdentifierCallable.class, stringCallables.get(0));
        assertInstanceOf(KIdentifierCallable.class, stringCallables.get(1));

        assertTrue(store.identifiersOfType(new KClassType("Boolean", true, false)).isEmpty());
    }

    @Test
    void hasIdentifier() {
        assertTrue(store.hasIdentifier("x"));
        assertFalse(emptyStore.hasIdentifier("x"));
        assertFalse(store.hasIdentifier("w"));
    }

    @RepeatedTest(10)
    void randomIdentifier() {
        assertTrue(store.hasIdentifier(store.randomIdentifier()));
    }

    @Test
    void addIdentifier() {
        String id = "x";
        emptyStore.addIdentifier(id, new KIdentifierCallable(id, stringType));
        assertTrue(emptyStore.hasIdentifier(id));
    }

    @Test
    void addIdentifierException() {
        String id = "x";
        assertThrows(IllegalArgumentException.class,
                () -> {
                    store.addIdentifier(id, new KIdentifierCallable(id, intType));
                });
    }

    @Test
    void updateIdentifier() {
        String id = "x";
        KType newType = new KClassType("SubInt", true, false);
        env.addType(intType, newType);
        KIdentifierCallable newCallable = new KIdentifierCallable(id, newType);
        store.updateIdentifier(id, newCallable);

        assertTrue(store.hasIdentifier(id));
        assertInstanceOf(KIdentifierCallable.class, store.getIdentifier(id));
    }

    @Test
    void updateIdentifierInvalidId() {
        String id = "x";
        KIdentifierCallable newCallable = new KIdentifierCallable(id, stringType);
        assertThrows(IllegalArgumentException.class,
                () -> {
                    emptyStore.updateIdentifier(id, newCallable);
                });
    }

    @Test
    void updateIdentifierInvalidType() {
        String id = "x";
        KIdentifierCallable newCallable = new KIdentifierCallable(id, stringType);
        assertThrows(IllegalArgumentException.class,
                () -> {
                    store.updateIdentifier(id, newCallable);
                });
    }

    @Test
    void typeOfIdentifier() {
        assertEquals(intType, store.typeOfIdentifier("x"));
        assertEquals(stringType, store.typeOfIdentifier("y"));
        assertEquals(stringType, store.typeOfIdentifier("z"));
    }

    @Test
    void typeOfIdentifierException() {
        assertThrows(IllegalArgumentException.class,
                () -> {
                    emptyStore.typeOfIdentifier("x");
                });
    }

    @Test
    void callablesOfType() {
        assertEquals(1, store.callablesOfType(intType).size());
        assertEquals(3, store.callablesOfType(new KClassType("Any", true, true)).size());

        for (KCallable callable : store.callablesOfType(stringType)) {
            assertInstanceOf(KIdentifierCallable.class, callable);
        }

        assertEquals(0, emptyStore.callablesOfType(intType).size());

        KIdentifierCallable newCallable = new KIdentifierCallable("y", intType);
        store.addIdentifier("w", newCallable);
        List<KCallable> intCallables = store.callablesOfType(intType);

        assertEquals(2, intCallables.size());
    }
}