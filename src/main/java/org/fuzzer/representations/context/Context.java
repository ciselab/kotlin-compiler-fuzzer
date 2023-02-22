package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.types.*;
import org.fuzzer.utils.KGrammarVocabulary;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.StringUtilities;
import org.jetbrains.kotlin.spec.grammar.tools.*;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.fuzzer.utils.StringUtilities.removeGeneric;
import static org.jetbrains.kotlin.spec.grammar.tools.KotlinGrammarToolsKt.parseKotlinCode;
import static org.jetbrains.kotlin.spec.grammar.tools.KotlinGrammarToolsKt.tokenizeKotlinCode;

public class Context implements Cloneable, Serializable {
    private IdentifierStore idStore;

    private HashMap<KType, Set<KCallable>> callablesByOwner;

    private HashMap<KType, Set<KCallable>> callablesByReturnType;
    private TypeEnvironment typeHierarchy;

    private RandomNumberGenerator rng;

    private KScope scope;

    public Context(RandomNumberGenerator rng) {
        this.callablesByOwner = new HashMap<>();
        this.callablesByReturnType = new HashMap<>();

        this.typeHierarchy = new DAGTypeEnvironment(rng);
        this.idStore = new MapIdentifierStore(typeHierarchy, rng);
        this.rng = rng;
        this.scope = KScope.GLOBAL_SCOPE;
    }

    public KScope getScope() {
        return scope;
    }

    public void updateScope(KScope scope) {
        this.scope = scope;
    }
    public Boolean hasAnyVariables() {
        return !idStore.isEmpty();
    }

    public List<KCallable> identifiersOfType(KType type) {
        return idStore.callablesOfType(type);
    }

    public String randomIdentifier() {
        return idStore.randomIdentifier();
    }

    public KType typeOfIdentifier(String id) {
        return idStore.typeOfIdentifier(id);
    }

    public Boolean isSubtypeOf(KType subtype, KType supertype) {
        return typeHierarchy.isSubtypeOf(subtype, supertype);
    }

    public KCallable randomCallableOfType(KType type, Predicate<KCallable> condition) throws CloneNotSupportedException {
        if (type instanceof KClassifierType) {
            Set<KType> subtypes = typeHierarchy.subtypesOf(type);
            List<KCallable> alternatives = new ArrayList<>(callablesByReturnType
                    .entrySet().stream()
                    // Get callables that match the return type
                    .filter(entry -> subtypes.contains(entry.getKey()))
                    // Keep only the callables
                    .map(Map.Entry::getValue)
                    // Add all the sets together
                    .reduce(new HashSet<>(), (acc, newSet) -> {
                        acc.addAll(newSet);
                        return acc;
                    })
                    .stream()
                    // Filter on the condition
                    .filter(condition).toList());
            // callablesByReturnType.entrySet().stream().filter(entry -> subtypes.contains(entry.getKey())).map(Map.Entry::getValue).reduce(new HashSet<>(), (acc, newSet) -> {acc.addAll(newSet); return acc;}).stream().filter(condition).toList()

            alternatives.addAll(identifiersOfType(type).stream().filter(condition).toList());

            if (alternatives.isEmpty()) {
                throw new IllegalArgumentException("Cannot sample callable of type " + type + " under condition " + condition);
            }
            KCallable selected = alternatives.get(rng.fromUniformDiscrete(0, alternatives.size() - 1));
            KCallable clone = (KCallable) selected.clone();
            return clone;
        } else {
            // TODO handle function types
            throw new UnsupportedOperationException("Cannot yet sample function subtypes.");
        }
    }

    public KCallable randomTerminalCallableOfType(KType type) throws CloneNotSupportedException {
        KCallable callable = randomCallableOfType(type, KCallable::isTerminal);
        return callable;
    }

    public KCallable randomConsumerCallable(KType type) throws CloneNotSupportedException {
        // TODO handle function inputs
        Predicate<KCallable> noFunctionInputs = kCallable -> kCallable.getInputTypes().stream().noneMatch(input -> input instanceof KFuncType);
        return randomCallableOfType(type, kCallable -> !kCallable.getInputTypes().isEmpty() && noFunctionInputs.test(kCallable));
    }

    public boolean containsIdentifier(String identifier) {
        return idStore.hasIdentifier(identifier);
    }

    public void addDefaultValue(KType type, String representation) {
        callablesByReturnType.putIfAbsent(type, new HashSet<>());
        callablesByReturnType.get(type).add(new KAnonymousCallable(representation, type));

        callablesByOwner.putIfAbsent(new KVoid(), new HashSet<>());
        callablesByOwner.get(new KVoid()).add(new KAnonymousCallable(representation, type));
    }

    public void addType(Set<KType> parents, KType newType) {
        typeHierarchy.addType(parents, newType);
    }

    public KType getTypeByName(String typeName) {
        return typeHierarchy.getTypeByName(typeName);
    }

    public void addIdentifier(String id, KCallable callable) {
        idStore.addIdentifier(id, callable);
    }

    public KType getRandomType() {
        return typeHierarchy.randomType();
    }

    public String getNewIdentifier() {
        String newId;

        do {
            newId = StringUtilities.randomIdentifier();
        } while (containsIdentifier(newId));

        return newId;
    }

    public void addIdentifier(String identifier, KCallable callable, KCallable owner) {
        addIdentifier(identifier, callable);
        callablesByOwner.get(owner.getReturnType()).add(callable);
        callablesByReturnType.get(callable.getReturnType()).add(callable);
    }

    public void fromFileNames(List<String> fileNames) {
        HashMap<String, KClassifierType> classes = new HashMap<>();
        HashMap<String, List<KType>> extractedTypes = new HashMap<>();
        HashMap<String, List<KTypeWrapper>> parents = new HashMap<>();

        for (String fileName : fileNames) {
            KotlinParseTree parseTree = null;
            try {
                String fileContents = FileUtilities.fileContentToString(new File(fileName));
                KotlinTokensList tokens = tokenizeKotlinCode(fileContents);
                parseTree = parseKotlinCode(tokens);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            fromParseTree(parseTree, classes, extractedTypes, parents);
        }


        if (!classes.containsKey("Any")) {
            throw new IllegalStateException("Could not find root of type hierarchy in environment.");
        }

        // Topologically add all the extracted types to the environment
        Set<String> addedClasses = new HashSet<>();
        Set<String> classesToAdd = new HashSet<>(classes.keySet());

//        addType(new HashSet<>(), classes.get("Any"));
        addType(new HashSet<>(), new KClassType("Any", false, false));
        addedClasses.add("Any");
        classesToAdd.remove("Any");

        while (addedClasses.size() < classes.size()) {
            List<String> canAddNext = parents.entrySet().stream()
                    .filter(entry -> classesToAdd.contains(entry.getKey()))
                    //.filter(entry -> entry.getValue().stream().map(KTypeWrapper::name).allMatch(addedClasses::contains))
                    .filter(entry -> entry.getValue().stream().map(kTypeWrapper -> removeGeneric(kTypeWrapper.name())).allMatch(addedClasses::contains))
                    .map(Map.Entry::getKey)
                    .toList();

            if (canAddNext.isEmpty()) {
                throw new IllegalStateException("No new classes can be topologically added.");
            }

            KClassifierType nextAddition = classes.get(canAddNext.get(0));
            Set<KType> parentsOfAddition = parents.get(nextAddition.name()).stream()
                    .map(wrapper -> typeHierarchy.getRootTypeByName(wrapper.name()))
                    .collect(Collectors.toSet());

            // If there are no parents, it is a descendant of Any
            if (parentsOfAddition.isEmpty()) {
                parentsOfAddition.add(getTypeByName("Any"));
            }

            addType(parentsOfAddition, nextAddition);

            classesToAdd.remove(nextAddition.name());
            addedClasses.add(removeGeneric(nextAddition.name()));
        }

        // Add callables
        // TODO use type wrappers instead
        for (Map.Entry<String, List<KType>> entry : extractedTypes.entrySet()) {

            // TODO make sure owners are classifiers.
            KClassifierType ownerType = (KClassifierType) getTypeByName(entry.getKey());

            if (ownerType instanceof KInterfaceType) {
                // TODO handle interfaces.
                continue;
            }

            KClassType classOwnerType = (KClassType) ownerType;

            for (KType type : entry.getValue()) {
                KCallable extractedCallable;

                if (type instanceof KFuncType) {

                    // ...
                    if ("constructor".equals(type.name())) {
                        extractedCallable = new KConstructor(classOwnerType, type.getInputTypes());
                    } else {
                        extractedCallable = new KMethod(ownerType, type.name(),
                                type.getInputTypes(),
                                type.getReturnType());
                    }
                } else {
                    // TODO: handle other class properties
                    continue;
                }

                // Store the callables
                callablesByOwner.putIfAbsent(classOwnerType, new HashSet<>());
                callablesByReturnType.putIfAbsent(extractedCallable.getReturnType(), new HashSet<>());

                callablesByOwner.get(classOwnerType).add(extractedCallable);
                callablesByReturnType.get(extractedCallable.getReturnType()).add(extractedCallable);
            }
        }
    }

    public void fromParseTree(KotlinParseTree parseTree,
                              HashMap<String, KClassifierType> classNames,
                              HashMap<String, List<KType>> extractedTypes,
                              HashMap<String, List<KTypeWrapper>> parents) {
        if (!KGrammarVocabulary.kotlinFile.equals(parseTree.getName())) {
            throw new IllegalArgumentException("Parse tree " + parseTree.getName() + " is not a file.");
        }

        List<KotlinParseTree> topLevelObjectNodes = parseTree.getChildren().stream()
                .filter(n -> KGrammarVocabulary.topLevelObject.equals(n.getName()))
                .toList();

        if (topLevelObjectNodes.isEmpty()) {
            return;
        }

        /*
        topLevelObject
            : declaration semis?
            ;
         */
        for (KotlinParseTree topLevelObject : topLevelObjectNodes) {

            // topLevelObject -> declaration -> classDeclaration
            KotlinParseTree decl = topLevelObject.getChildren().get(0).getChildren().get(0);

            if (!KGrammarVocabulary.classDecl.equals(decl.getName())) {
                continue;
            }
            List<KType> types = new ArrayList<>();
            List<KTypeWrapper> parentList = new ArrayList<>();

            KClassifierType classType = getClassAndMembers(decl, types, parentList);

            classNames.put(classType.name(), classType);
            extractedTypes.put(classType.name(), types);
            parents.put(classType.name(), parentList);
        }
    }

    /**
     * classDeclaration
     * : modifiers? (CLASS | INTERFACE) NL* simpleIdentifier
     * (NL* typeParameters)? (NL* primaryConstructor)?
     * (NL* COLON NL* delegationSpecifiers)?
     * (NL* typeConstraints)?
     * (NL* classBody | NL* enumClassBody)?
     * ;
     */
    public KClassifierType getClassAndMembers(KotlinParseTree classDeclNode, List<KType> members, List<KTypeWrapper> parents) {
        if (!KGrammarVocabulary.classDecl.equals(classDeclNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + classDeclNode.getName() + " does not is not a class declaration.");
        }

        // Handle class name
        KotlinParseTree idNode = classDeclNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.simpleId.equals(n.getName()))
                .toList()
                .get(0);

        String name = getIdentifierName(idNode);

        // Handle class members
        KotlinParseTree classBody = classDeclNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.classBody.equals(n.getName()))
                .toList()
                .get(0);

        members.addAll(getClassMembers(classBody));

        // Handle class modifiers
        KTypeModifiers classModifiers = null;

        List<KotlinParseTree> modifierNodeList = classDeclNode.getChildren().stream()
                .filter(x -> KGrammarVocabulary.modifiers.equals(x.getName()))
                .toList();

        if (!modifierNodeList.isEmpty()) {
            classModifiers = getModifiers(modifierNodeList.get(0));
        }

        // Handle class type parameters
        List<KotlinParseTree> classTypeParamsNodeList = classDeclNode.getChildren().stream()
                .filter(x -> KGrammarVocabulary.typeParameters.equals(x.getName()))
                .toList();

        List<KGenericType> genericTypes = new ArrayList<>();


        if (!classTypeParamsNodeList.isEmpty()) {
            genericTypes = getTypeParams(classTypeParamsNodeList.get(0));
        }

        // Handle inheritance
        List<KotlinParseTree> delegationSpecifierNodes = classDeclNode.getChildren().stream()
                .filter(x -> KGrammarVocabulary.delegationSpecifiers.equals(x.getName()))
                .toList();

        if (!delegationSpecifierNodes.isEmpty()) {
            parents.addAll(getDelegationSpecifiers(delegationSpecifierNodes.get(0)));
        }

        boolean isClass = classDeclNode.getChildren().stream()
                .anyMatch(x -> KGrammarVocabulary.className.equals(x.getName()));

        if (classModifiers != null) {
            return isClass ?
                    new KClassType(name, genericTypes, classModifiers.isOpen(), classModifiers.isAbstract()) :
                    new KInterfaceType(name, genericTypes);
        } else {
            return isClass ?
                    new KClassType(name, genericTypes, false, false) :
                    new KInterfaceType(name, genericTypes);
        }
    }

    /**
     * delegationSpecifiers
     * : annotatedDelegationSpecifier (NL* COMMA NL* annotatedDelegationSpecifier)*
     * ;
     */
    private List<KTypeWrapper> getDelegationSpecifiers(KotlinParseTree delegationSpecifierNode) {
        if (!(KGrammarVocabulary.delegationSpecifiers.equals(delegationSpecifierNode.getName()) ||
                KGrammarVocabulary.delegationSpecifier.equals(delegationSpecifierNode.getName()) ||
                KGrammarVocabulary.annotatedDelegationSpecifier.equals(delegationSpecifierNode.getName()))) {
            throw new IllegalArgumentException("Parse tree " + delegationSpecifierNode.getName() + " does not is not a class declaration.");
        }

        /*
        delegationSpecifier
            : constructorInvocation
            | explicitDelegation
            | userType
            | functionType
            ;
         */
        switch (delegationSpecifierNode.getName()) {
            case KGrammarVocabulary.delegationSpecifier -> {
                KotlinParseTree child = delegationSpecifierNode.getChildren().get(0);
                switch (child.getName()) {
                    case KGrammarVocabulary.userType -> {
                        List<KTypeWrapper> types = new ArrayList<>();
                        types.add(getType(child));

                        return types;
                    }
                    /*
                    constructorInvocation
                        : userType valueArguments
                        ;
                     */
                    case KGrammarVocabulary.constructorInvocation -> {
                        List<KTypeWrapper> types = new ArrayList<>();
                        KTypeWrapper type = getType(child.getChildren().get(0));
                        types.add(type);

                        return types;
                    }
                    default -> {
                        throw new UnsupportedOperationException("Cannot yet parse delegation specifier of type: " + child);
                    }
                }
            }
            /*
            annotatedDelegationSpecifier
                : annotation* NL* delegationSpecifier
                ;
             */
            case KGrammarVocabulary.annotatedDelegationSpecifier -> {
                List<KotlinParseTree> children = delegationSpecifierNode.getChildren();

                // Ignore annotation for now
                return getDelegationSpecifiers(children.get(children.size() - 1));
            }

            /*
            delegationSpecifiers
                : annotatedDelegationSpecifier (NL* COMMA NL* annotatedDelegationSpecifier)*
                ;
             */
            case KGrammarVocabulary.delegationSpecifiers -> {
                List<KotlinParseTree> annotatedDelSpecs = delegationSpecifierNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.annotatedDelegationSpecifier.equals(n.getName()))
                        .toList();

                List<KTypeWrapper> types = new ArrayList<>();

                for (KotlinParseTree child : annotatedDelSpecs) {
                    types.addAll(getDelegationSpecifiers(child));
                }

                return types;
            }
            default -> {
                throw new IllegalArgumentException("Parse tree " + delegationSpecifierNode.getName() + " does not is not a class declaration.");
            }
        }
    }

    /**
     * typeParameters
     * : LANGLE NL* typeParameter (NL* COMMA NL* typeParameter)* NL* RANGLE
     * ;
     */
    public List<KGenericType> getTypeParams(KotlinParseTree typeParamsNode) {
        if (!KGrammarVocabulary.typeParameters.equals(typeParamsNode.getName())) {
            throw new IllegalArgumentException("Parse tree" + typeParamsNode.getName() + " is not a type parameter node.");
        }

        // Select "typeParameter" nodes and skip syntax
        List<KotlinParseTree> typeParamChildren = typeParamsNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.typeParameter.equals(n.getName()))
                .toList();

        List<KGenericType> types = new ArrayList<>();

        for (KotlinParseTree child : typeParamChildren) {
            types.add(new KGenericType(getTypeParam(child)));
        }

        return types;
    }

    /**
     * typeParameter
     * : typeParameterModifiers? NL* simpleIdentifier (NL* COLON NL* type)?
     * ;
     */
    public String getTypeParam(KotlinParseTree typeParamNode) {
        if (!KGrammarVocabulary.typeParameter.equals(typeParamNode.getName())) {
            throw new IllegalArgumentException("Parse tree" + typeParamNode.getName() + " is not a type parameter node.");
        }

        // Ignore modifiers and types
        List<KotlinParseTree> idNode = typeParamNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.simpleId.equals(n.getName()))
                .toList();

        return getIdentifierName(idNode.get(0));
    }

    public KTypeModifiers getModifiers(KotlinParseTree modifiersNode) {
        if (!modifiersNode.getName().equals(KGrammarVocabulary.modifiers)) {
            throw new IllegalArgumentException("Parse tree " + modifiersNode.getName() + " does not contain modifiers.");
        }

        String memberModifier = "", visibilityModifier = "", propertyModifier = "", inheritanceModifier = "";

        for (KotlinParseTree modifierNode : modifiersNode.getChildren()) {
            // Get the concrete modifier content
            // Example: modifiers -> modifier (i-th value) -> visibilityModifier -> PUBLIC
            // ConcreteModifierNode in this case is "visibilityModifier" above
            KotlinParseTree concereteModifierNode = modifierNode.getChildren().get(0);

            switch (concereteModifierNode.getName()) {
                case KGrammarVocabulary.memberModifier -> {
                    memberModifier = concereteModifierNode.getChildren().get(0).getText();
                }
                case KGrammarVocabulary.visibilityModifier -> {
                    visibilityModifier = concereteModifierNode.getChildren().get(0).getText();
                }
                case KGrammarVocabulary.propertyModifier -> {
                    propertyModifier = concereteModifierNode.getChildren().get(0).getText();
                }
                case KGrammarVocabulary.inheritanceModifier -> {
                    inheritanceModifier = concereteModifierNode.getChildren().get(0).getText();
                }
                default -> {
                    continue;
                }
            }
        }

        return new KTypeModifiers(memberModifier, visibilityModifier, propertyModifier, inheritanceModifier);
    }

    public String getIdentifierName(KotlinParseTree identifierNode) {
        if (!identifierNode.getName().equals(KGrammarVocabulary.simpleId)) {
            throw new IllegalArgumentException("Parse tree " + identifierNode + " is not an identifier.");
        }

        // simpleIdentifier -> Identifier (text = class name)
        return identifierNode.getChildren().get(0).getText();
    }

    public List<KType> getClassMembers(KotlinParseTree classBodyNode) {
        if (!classBodyNode.getName().equals(KGrammarVocabulary.classBody)) {
            throw new IllegalArgumentException("Parse tree " + classBodyNode + " is not a class body.");
        }

        List<KType> memberTypes = new ArrayList<>();

        // Get the declarations
        KotlinParseTree declarationsNode = classBodyNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.classMemberDeclarations.equals(n.getName()))
                .toList()
                .get(0);

        List<KotlinParseTree> classMemberDecls = declarationsNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.classMemberDeclaration.equals(n.getName()))
                .toList();

        // For each declaration
        for (KotlinParseTree classMemberDecl : classMemberDecls) {
            memberTypes.addAll(getClassMemberDecl(classMemberDecl));
        }

        return memberTypes;
    }

    public List<KType> getClassMemberDecl(KotlinParseTree memberDeclNode) {
        if (!KGrammarVocabulary.classMemberDeclaration.equals(memberDeclNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + memberDeclNode + " is not a class member declaration.");
        }

        // Get the declaration node
        KotlinParseTree declNode = memberDeclNode.getChildren().get(0);
        switch (declNode.getName()) {

            // Nested declaration
            case KGrammarVocabulary.decl -> {

                // Get the nested declaration node
                // e.g.: declaration -> functionDeclaration
                KotlinParseTree nestedDecl = declNode.getChildren().get(0);

                switch (nestedDecl.getName()) {
                    case KGrammarVocabulary.funcDecl -> {
                        List<KType> res = new ArrayList<>();
                        res.add(getFuncDeclaration(nestedDecl));
                        return res;
                    }
                    case KGrammarVocabulary.propertyDecl -> {
                        return getPropertyDeclaration(nestedDecl);
                    }
                    default -> {
                        throw new UnsupportedOperationException(nestedDecl.getName() + " not yet supported during parsing.");
                    }
                }
            }

            case KGrammarVocabulary.companionObject -> {
                List<KotlinParseTree> classBodyNodeList = declNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.classBody.equals(n.getName()))
                        .toList();
                if (classBodyNodeList.isEmpty()) {
                    throw new UnsupportedOperationException("Cannot yet parse empty companion object nodes.");
                }

                // Ignore companion object and treat its attributes as the class' atributes
                return getClassMembers(classBodyNodeList.get(0));
            }

            case KGrammarVocabulary.anonymousInitializer -> {
                throw new UnsupportedOperationException("Cannot yet parse anonymous initializer nodes.");
            }

            /*
            secondaryConstructor
                : modifiers? CONSTRUCTOR NL* functionValueParameters (NL* COLON NL* constructorDelegationCall)? NL* block?
                ;
             */
            case KGrammarVocabulary.secondaryConstructor -> {
                KotlinParseTree funcValParams = declNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.functionValueParameters.equals(n.getName()))
                        .toList().get(0);

                List<KTypeWrapper> parameterTypes = getFuncValueParams(funcValParams);

                // TODO handle constructor
                KFuncType func = new KFuncType("constructor", new ArrayList<>(), parameterTypes.stream().map(KTypeWrapper::toType).toList());
                List<KType> res = new ArrayList<>();
                res.add(func);

                return res;
            }

            default -> {
                throw new IllegalArgumentException("Unexpected class declaration " + declNode);
            }
        }
    }

    private List<KType> getPropertyDeclaration(KotlinParseTree propertyDecl) {
        if (!KGrammarVocabulary.propertyDecl.equals(propertyDecl.getName())) {
            throw new IllegalArgumentException("Parse tree " + propertyDecl + " is not a property declaraiton.");
        }

        // Handle modifiers
        Optional<KotlinParseTree> modifiersNode = KGrammarVocabulary.modifiers
                .equals(propertyDecl.getChildren().get(0).getName()) ?
                Optional.of(propertyDecl.getChildren().get(0)) :
                Optional.empty();

        final Optional<KTypeModifiers> modifiers = modifiersNode.map(this::getModifiers);

        // Handle declaration(s)
        KotlinParseTree varDeclNode = propertyDecl.getChildren().stream()
                .filter(n ->
                        KGrammarVocabulary.varDecl.equals(n.getName()) ||
                                KGrammarVocabulary.multiVarDecl.equals(n.getName()))
                .toList()
                .get(0);

        List<KTypeWrapper> varTypes = new ArrayList<>();
        switch (varDeclNode.getName()) {
            case KGrammarVocabulary.multiVarDecl -> {
                varTypes = getMultiVarDecl(varDeclNode);
            }
            case KGrammarVocabulary.varDecl -> {
                varTypes.add(getVarDecl(varDeclNode));
            }
        }

        return varTypes.stream()
                .map(wrapper -> modifiers.isEmpty() ? wrapper.toType() : wrapper.toType(modifiers.get()))
                .toList();
    }

    /**
     * multiVariableDeclaration
     * : LPAREN NL* variableDeclaration (NL* COMMA NL* variableDeclaration)* NL* RPAREN
     * ;
     */
    private List<KTypeWrapper> getMultiVarDecl(KotlinParseTree multiVarDeclNode) {
        if (!KGrammarVocabulary.propertyDecl.equals(multiVarDeclNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + multiVarDeclNode + " is not a multi variable declaration.");
        }

        List<KotlinParseTree> varDeclNodes = multiVarDeclNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.varDecl.equals(n.getName()))
                .toList();

        return varDeclNodes.stream().map(this::getVarDecl).toList();
    }

    /**
     * variableDeclaration
     * : annotation* NL* simpleIdentifier (NL* COLON NL* type)?
     * ;
     */
    private KTypeWrapper getVarDecl(KotlinParseTree varDeclNode) {
        if (!KGrammarVocabulary.varDecl.equals(varDeclNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + varDeclNode + " is not a variable declaration.");
        }

        String varName = null;
        KTypeWrapper type = null;

        for (KotlinParseTree child : varDeclNode.getChildren()) {
            switch (child.getName()) {
                case KGrammarVocabulary.simpleId -> {
                    varName = getIdentifierName(child);
                }
                case KGrammarVocabulary.type -> {
                    type = getType(child);
                }
            }
        }

        // Disregard variable name for now
        return type;
    }


    /**
     * Extracts a function callable from the definition below:
     * functionDeclaration
     * : modifiers?
     * FUN (NL* typeParameters)? (NL* receiverType NL* DOT)? NL* simpleIdentifier
     * NL* functionValueParameters
     * (NL* COLON NL* type)?
     * (NL* typeConstraints)?
     * (NL* functionBody)?
     * ;
     */
    private KFuncType getFuncDeclaration(KotlinParseTree funcDeclNode) {
        if (!KGrammarVocabulary.funcDecl.equals(funcDeclNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + funcDeclNode + " is not a function declaration.");
        }

        KTypeModifiers modifiers = null;
        String funcName = null;
        List<KTypeWrapper> parameterTypes = null;
        KType returnType = null;

        for (KotlinParseTree child : funcDeclNode.getChildren()) {
            switch (child.getName()) {
                case (KGrammarVocabulary.modifiers) -> {
                    modifiers = getModifiers(child);
                }
                case (KGrammarVocabulary.simpleId) -> {
                    funcName = getIdentifierName(child);
                }
                case (KGrammarVocabulary.functionValueParameters) -> {
                    parameterTypes = getFuncValueParams(child);
                }
                case KGrammarVocabulary.type -> {
                    // TODO: nested functions
                    returnType = getType(child).toClass(false, false);
                }
            }
        }

        if (returnType == null) {
            return new KFuncType(funcName, new ArrayList<>(), parameterTypes.stream().map(KTypeWrapper::toType).toList());
        } else {
            return new KFuncType(funcName, new ArrayList<>(), parameterTypes.stream().map(KTypeWrapper::toType).toList(), returnType);
        }
    }

    private List<KTypeWrapper> getFuncValueParams(KotlinParseTree funcParamsNode) {
        if (!KGrammarVocabulary.functionValueParameters.equals(funcParamsNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + funcParamsNode + " is not a function params value node.");
        }

        List<KTypeWrapper> parameterTypeNames = new ArrayList<>();

        for (KotlinParseTree funcValueParam : funcParamsNode.getChildren()) {

            // Skip everything but "functionValueParameter" nodes
            if (!KGrammarVocabulary.functionValueParameter.equals(funcValueParam.getName())) {
                continue;
            }

            parameterTypeNames.add(getFuncValueParam(funcValueParam));
        }

        return parameterTypeNames;
    }

    /**
     * functionValueParameter
     * : parameterModifiers? parameter (NL* ASSIGNMENT NL* expression)?
     * ;
     */
    private KTypeWrapper getFuncValueParam(KotlinParseTree funcParamNode) {
        if (!KGrammarVocabulary.functionValueParameter.equals(funcParamNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + funcParamNode + " is not a function value parameter node.");
        }

        // Extract parameter node
        KotlinParseTree paramNode = funcParamNode.getChildren()
                .stream().filter(n -> KGrammarVocabulary.parameter.equals(n.getName()))
                .toList()
                .get(0);

        // return the type of the parameter
        return getParameter(paramNode);
    }

    /**
     * parameter
     * : simpleIdentifier NL* COLON NL* type
     * ;
     */
    private KTypeWrapper getParameter(KotlinParseTree paramNode) {
        if (!KGrammarVocabulary.parameter.equals(paramNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + paramNode + " is not a parameter node.");
        }

        String parameterName = null;
        KTypeWrapper type = null;

        for (KotlinParseTree child : paramNode.getChildren()) {
            switch (child.getName()) {
                case KGrammarVocabulary.simpleId -> {
                    parameterName = getIdentifierName(child);
                }
                case KGrammarVocabulary.type -> {
                    type = getType(child);
                }
            }
        }

        if (type == null) {
            throw new IllegalStateException("Parameter node " + paramNode + " does not include a type.");
        }

        // Currently disregard parameterName
        return type;
    }

    /**
     * type
     * : typeModifiers?
     * ( parenthesizedType
     * | nullableType
     * | typeReference
     * | functionType)
     * ;
     */
    private KTypeWrapper getType(KotlinParseTree typeNode) {
        if (!(
                KGrammarVocabulary.type.equals(typeNode.getName()) ||
                        KGrammarVocabulary.userType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.typeReference.equals(typeNode.getName()) ||
                        KGrammarVocabulary.simpleUserType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.parenthesizedType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.functionType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.nullableType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.typeProjection.equals(typeNode.getName())
        )) {
            throw new IllegalArgumentException("Parse tree " + typeNode + " is not a type node.");
        }

        switch (typeNode.getName()) {
            case KGrammarVocabulary.type -> {
                List<KotlinParseTree> children = typeNode.getChildren();

                // If there are modifiers, ignore them
                if (children.size() > 1) {
                    return getType(children.get(1));
                } else {
                    return getType(children.get(0));
                }
            }
            case KGrammarVocabulary.parenthesizedType -> {
                KotlinParseTree nestedType = typeNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.type.equals(n.getName()))
                        .toList()
                        .get(0);
                return getType(nestedType);
            }
            case KGrammarVocabulary.nullableType -> {
                KotlinParseTree nestedType = typeNode.getChildren().stream()
                        .filter(n ->
                                KGrammarVocabulary.typeReference.equals(n.getName()) ||
                                        KGrammarVocabulary.parenthesizedType.equals(n.getName()))
                        .toList()
                        .get(0);
                return getType(nestedType);
            }
            case KGrammarVocabulary.typeReference -> {
                KotlinParseTree nestedType = typeNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.userType.equals(n.getName()))
                        .toList()
                        .get(0);
                return getType(nestedType);
            }
            case KGrammarVocabulary.userType -> {
                List<KotlinParseTree> nestedTypes = typeNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.simpleUserType.equals(n.getName()))
                        .toList();

                if (nestedTypes.size() > 1) {
                    throw new UnsupportedOperationException("Nested user types not yet supported.");
                }

//                for (KotlinParseTree simpleUserType : nestedTypes) {
//                    getType(simpleUserType);
//                }
                return getType(nestedTypes.get(0));
            }
            case KGrammarVocabulary.simpleUserType -> {
                // Assume classes. Should refactor.
                String id = getIdentifierName(typeNode.getChildren().get(0));

                List<KotlinParseTree> typeArgumentNodes = typeNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.typeArguments.equals(n.getName()))
                        .toList();

                List<KGenericType> generics = new ArrayList<>();
                for (KotlinParseTree typeArg : typeArgumentNodes) {

                    // Get the projections
                    /*
                    typeProjection
                        : typeProjectionModifiers? type | MULT
                        ;
                     */
                    List<KotlinParseTree> typeProjections = typeArg.getChildren().stream()
                            .filter(n -> KGrammarVocabulary.typeProjection.equals(n.getName()))
                            .toList();

                    List<KTypeWrapper> res = new ArrayList<>();

                    for (KotlinParseTree child : typeProjections) {
                        res.add(getType(child));
                    }

                    generics.addAll(res.stream()
                            .map(wrapper -> new KGenericType(wrapper.name()))
                            .toList());
                }

                return new KTypeWrapper(KTypeIndicator.CLASS, id, generics);
            }
            /*
            typeArguments
                : LANGLE NL* typeProjection (NL* COMMA NL* typeProjection)* NL* RANGLE
                ;
             */
            /*
            typeProjection
                : typeProjectionModifiers? type | MULT
                ;
             */
            case KGrammarVocabulary.typeProjection -> {
                // Either a type or "*"
                KotlinParseTree lastChild = typeNode.getChildren().get(typeNode.getChildren().size() - 1);
                if (KGrammarVocabulary.MULT.equals(lastChild.getName())) {
                    return new KTypeWrapper(KTypeIndicator.GENERIC, lastChild.getName());
                }

                return getType(lastChild);
            }
            /*
            functionType
                : (receiverType NL* DOT NL*)? functionTypeParameters NL* ARROW NL* type
                ;
             */
            case KGrammarVocabulary.functionType -> {
                KotlinParseTree functionParameters = typeNode.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.functionTypeParameters.equals(n.getName()))
                        .toList()
                        .get(0);

                // Select the type and parameter
                /*
                functionTypeParameters
                    : LPAREN NL* (parameter | type)? (NL* COMMA NL* (parameter | type))* NL* RPAREN
                    ;
                 */
                List<KotlinParseTree> nestedTypesOrParams = functionParameters.getChildren().stream()
                        .filter(n -> KGrammarVocabulary.type.equals(n.getName()) ||
                                KGrammarVocabulary.parameter.equals(n.getName()))
                        .toList();

                List<KTypeWrapper> inputTypes = new ArrayList<>();

                for (KotlinParseTree typeOrParam : nestedTypesOrParams) {
                    KTypeWrapper extractedType = switch (typeOrParam.getName()) {
                        case KGrammarVocabulary.parameter -> getParameter(typeOrParam);
                        case KGrammarVocabulary.type -> getType(typeOrParam);
                        default ->
                                throw new IllegalArgumentException("Unexpected input of type: " + typeOrParam.getName());
                    };
                    inputTypes.add(extractedType);
                }

                KotlinParseTree returnTypeNode = typeNode.getChildren().get(typeNode.getChildren().size() - 1);

                KTypeWrapper returnType = getType(returnTypeNode);

                return new KTypeWrapper(new ArrayList<>(), KTypeIndicator.FUNCTION, "", new ArrayList<>(), inputTypes, returnType);
            }
            default -> {
                throw new IllegalArgumentException("Cannot parse type node of type: " + typeNode);
            }
        }
    }


    @Override
    public Context clone() {
        try {
            Context clone = (Context) super.clone();
            // TODO: in the future, clone type environment as well
            clone.typeHierarchy = typeHierarchy;
            clone.callablesByOwner = (HashMap<KType, Set<KCallable>>) callablesByOwner.clone();
            clone.callablesByReturnType = (HashMap<KType, Set<KCallable>>) callablesByReturnType.clone();
            clone.rng = rng;
            clone.idStore = new MapIdentifierStore(clone.typeHierarchy, clone.rng);
            clone.scope = scope;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
