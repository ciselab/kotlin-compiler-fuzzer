package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.types.*;
import org.fuzzer.utils.KGrammarVocabulary;
import org.fuzzer.utils.RandomNumberGenerator;
import org.jetbrains.kotlin.spec.grammar.tools.KotlinParseTree;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Context {
    private final IdentifierStore idStore;

    private final Set<KCallable> callables;
    private final TypeEnvironment typeHierarchy;

    private final RandomNumberGenerator rng;

    public Context(RandomNumberGenerator rng) {
        this.callables = new HashSet<>();

        this.typeHierarchy = new TreeTypeEnvironment(rng);
        this.idStore = new MapIdentifierStore(typeHierarchy, rng);
        this.rng = rng;
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

    public Optional<KCallable> randomCallableOfType(KType type, Predicate<KCallable> condition) throws CloneNotSupportedException {
        Set<KType> subtypes = typeHierarchy.subtypesOf(type);
        List<KCallable> alternatives = new ArrayList<>(callables.stream().filter(kCallable -> subtypes.contains(kCallable.getReturnType())).filter(condition).toList());

        alternatives.addAll(identifiersOfType(type));

        if (alternatives.isEmpty()) return Optional.empty();
        KCallable selected = alternatives.get(rng.fromUniformDiscrete(0, alternatives.size() - 1));
        return Optional.of((KCallable) selected.clone());
    }

    public Optional<KCallable> randomTerminalCallableOfType(KType type) throws CloneNotSupportedException {
        return randomCallableOfType(type, kCallable -> kCallable.getInputTypes().isEmpty());
    }

    public Optional<KCallable> randomConsumerCallable(KType type) throws CloneNotSupportedException {
        return randomCallableOfType(type, kCallable -> !kCallable.getInputTypes().isEmpty());
    }

    public boolean containsIdentifier(String identifier) {
        return idStore.hasIdentifier(identifier);
    }

    public void addType(KType parent, KType newType) {
        typeHierarchy.addType(parent, newType);
    }

    public void addIdentifier(String id, KCallable callable) {
        idStore.addIdentifier(id, callable);
    }

    public KType getRandomType() {
        return typeHierarchy.randomType();
    }

    public void fromParseTree(KotlinParseTree parseTree) {
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

            KClassifierType classType = getClassAndMembers(decl, new ArrayList<>(), new ArrayList<>());
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
    public KClassifierType getClassAndMembers(KotlinParseTree classDeclNode, List<KType> members, List<String> memberNames) {
        if (!KGrammarVocabulary.classDecl.equals(classDeclNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + classDeclNode.getName() + " does not is not a class declaration.");
        }

        KotlinParseTree idNode = classDeclNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.simpleId.equals(n.getName()))
                .toList()
                .get(0);

        String name = getIdentifierName(idNode);

        KotlinParseTree classBody = classDeclNode.getChildren().stream()
                .filter(n -> KGrammarVocabulary.classBody.equals(n.getName()))
                .toList()
                .get(0);

        members = getClassMembers(classBody);

        KTypeModifiers classModifiers = null;

        List<KotlinParseTree> modifierNodeList = classDeclNode.getChildren().stream()
                .filter(x -> KGrammarVocabulary.modifiers.equals(x.getName()))
                .toList();

        if (!modifierNodeList.isEmpty()) {
            classModifiers = getModifiers(modifierNodeList.get(0));
        }

        boolean isClass = classDeclNode.getChildren().stream()
                .anyMatch(x -> KGrammarVocabulary.className.equals(x.getName()));

        if (classModifiers != null) {
            return isClass ?
                    new KClassType(name, classModifiers.isOpen(), classModifiers.isAbstract()) :
                    new KInterfaceType(name);
        } else {
            return isClass ?
                    new KClassType(name, true, false) :
                    new KInterfaceType(name);
        }
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
            memberTypes.add(getClassMemberDecl(classMemberDecl));
        }

        return memberTypes;
    }

    public KType getClassMemberDecl(KotlinParseTree memberDeclNode) {
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

                if (!(nestedDecl.getName().equals(KGrammarVocabulary.funcDecl))) {
                    throw new UnsupportedOperationException(nestedDecl.getName() + " not yet supported during parsing.");
                }

                return getFuncDeclaration(nestedDecl);
            }

            case KGrammarVocabulary.companionObject -> {
                throw new UnsupportedOperationException("Cannot yet parse companion object nodes.");
            }

            case KGrammarVocabulary.anonymousInitializer -> {
                throw new UnsupportedOperationException("Cannot yet parse anonymous initializer nodes.");
            }

            case KGrammarVocabulary.secondaryConstructor -> {
                throw new UnsupportedOperationException("Cannot yet parse secondary constructor nodes.");
            }

            default -> {
                throw new IllegalArgumentException("Unexpected class declaration " + declNode);
            }
        }
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
        List<String> parameterTypes = null;
        String returnType = null;

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
                    returnType = getType(child);
                }
            }
        }

        // TODO handle inheritance and abstraction
        List<KType> inputTypes = parameterTypes.stream()
                .map(name -> (KType) new KClassType(name, true, false))
                .toList();

        if (returnType == null) {
            return new KFuncType(funcName, new ArrayList<>(), inputTypes);
        } else {
            return new KFuncType(funcName, new ArrayList<>(), inputTypes, new KClassType(returnType, true, true));
        }
    }

    private List<String> getFuncValueParams(KotlinParseTree funcParamsNode) {
        if (!KGrammarVocabulary.functionValueParameters.equals(funcParamsNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + funcParamsNode + " is not a function params value node.");
        }

        List<String> parameterTypeNames = new ArrayList<>();

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
    private String getFuncValueParam(KotlinParseTree funcParamNode) {
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
    private String getParameter(KotlinParseTree paramNode) {
        if (!KGrammarVocabulary.parameter.equals(paramNode.getName())) {
            throw new IllegalArgumentException("Parse tree " + paramNode + " is not a parameter node.");
        }

        String parameterName = null;
        String type = null;

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
    private String getType(KotlinParseTree typeNode) {
        if (!(
                KGrammarVocabulary.type.equals(typeNode.getName()) ||
                        KGrammarVocabulary.userType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.typeReference.equals(typeNode.getName()) ||
                        KGrammarVocabulary.simpleUserType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.parenthesizedType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.functionType.equals(typeNode.getName()) ||
                        KGrammarVocabulary.nullableType.equals(typeNode.getName())
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
                return getIdentifierName(typeNode.getChildren().get(0));
            }
            // TODO
            case KGrammarVocabulary.functionType -> {
                throw new UnsupportedOperationException("Cannot yet parse function types");
            }
            default -> {
                throw new IllegalArgumentException("Cannot parse type node of type: " + typeNode);
            }
        }
    }
}
