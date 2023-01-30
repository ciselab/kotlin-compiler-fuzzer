package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KAnonymousCallable;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.callables.KIdentifierCallable;
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
        intType = new KType("Int");
        stringType = new KType("String");
        RandomNumberGenerator rng = new RandomNumberGenerator(0);

        Tree<KType> root = new Tree<>(new KType("Any"));
        root.addChildren(Arrays.stream(new String[]{"Number", "String", "Char", "Boolean"}).map(KType::new).toList());

        Optional<Tree<KType>> numberType = root.find(new KType("Number"));
        numberType.get().addChildren(Arrays.stream(new String[]{"Byte", "Short", "Int", "Long"}).map(KType::new).toList());

        env = new TreeTypeEnvironment(root, rng);

        emptyStore = new MapIdentifierStore(env, rng);

        store = new MapIdentifierStore(env, rng);
        store.addIdentifier("x", new KAnonymousCallable(intType, "42"));
        store.addIdentifier("y", new KAnonymousCallable(stringType,  "foo"));
        store.addIdentifier("z", new KAnonymousCallable(stringType,  "bar"));

        fakeType = new KType("Fake");
    }
    @Test
    void isEmpty() {
        assertTrue(emptyStore.isEmpty());
        assertFalse(store.isEmpty());
    }

    @Test
    void identifiersOfType() {
        List<KCallable> numCallables =  store.identifiersOfType(new KType("Number"));

        assertEquals(1, numCallables.size());
        assertInstanceOf(KAnonymousCallable.class, numCallables.get(0));

        List<KCallable> stringCallables =  store.identifiersOfType(stringType);
        assertEquals(2, stringCallables.size());
        assertInstanceOf(KAnonymousCallable.class, stringCallables.get(0));
        assertInstanceOf(KAnonymousCallable.class, stringCallables.get(1));

        assertTrue(store.identifiersOfType(new KType("Boolean")).isEmpty());
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
        emptyStore.addIdentifier(id, new KAnonymousCallable(intType, ""));
        assertTrue(emptyStore.hasIdentifier(id));
    }

    @Test
    void addIdentifierException() {
        String id = "x";
        assertThrows(IllegalArgumentException.class,
                () -> {
                    store.addIdentifier(id, new KAnonymousCallable(intType, ""));
                });
    }

    @Test
    void updateIdentifier() {
        String id = "x";
        KType newType = new KType("SubInt");
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
        assertEquals(3, store.callablesOfType(new KType("Any")).size());

        for (KCallable callable : store.callablesOfType(stringType)) {
            assertInstanceOf(KAnonymousCallable.class, callable);
        }

        assertEquals(0, emptyStore.callablesOfType(intType).size());

        KIdentifierCallable newCallable = new KIdentifierCallable("y", intType);
        store.addIdentifier("w", newCallable);
        List<KCallable> intCallables = store.callablesOfType(intType);

        assertEquals(2, intCallables.size());
    }
}