package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;

import java.util.HashMap;

public enum ASTNodeType {
    NOTHING(-1),
    TOKEN(66),
    PRODUCTION(57),
    BLOCK(78),
    ALT(74),
    SET(98),
    LITERAL(62),
    RULE_MODIFIERS(96),
    AT_LEAST_ONCE(90), // +
    UNKNOWN(80), // *
    AT_MOST_ONCE(89), // ?
    LEXER_ALT_ACTION(87);
    private final Integer id;
    private static final HashMap<Integer, ASTNodeType> nodeMap = mapFromValueList();
    ASTNodeType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public static ASTNodeType nodeFromId(Integer id) {
        return nodeMap.getOrDefault(id, ASTNodeType.NOTHING);
    }

    public static boolean isNothing(ASTNodeType node) {
        return node.id == -1;
    }

    public static boolean shouldHandle(GrammarAST nodeType) {
        return !isNothing(nodeFromId(nodeType.getType()));
    }

    private static HashMap<Integer, ASTNodeType> mapFromValueList() {
        HashMap<Integer, ASTNodeType> res = new HashMap<>();
        for (ASTNodeType n : ASTNodeType.values()) {
            res.put(n.id, n.getDeclaringClass().cast(n));
        }

        return res;
    }
}
