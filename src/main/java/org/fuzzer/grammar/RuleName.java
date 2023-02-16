package org.fuzzer.grammar;

import java.util.Map;

public class RuleName {
    public static final String kotlinFile = "kotlinFile";

    public static final String ALT = "ALT";

    public static final String OPTIONAL_BLOCK = "?";

    public static final String STAR_BLOCK = "*";

    public static final String BLOCK = "BLOCK";

    private static final String shebangLine = "shebangLine";

    public static final String SEMIS = "semis";

    public static final String SEMI = "semi";

    public static final String DECLARATION = "declaration";

    public static final String TOP_LEVEL_OBJ = "topLevelObject";

    public static final String FUNC_DECL = "functionDeclaration";

    public static final String fun = "fun";

    static final Map<String, String> parserVocabulary = Map.ofEntries(
            Map.entry("", "!1")
    );
}
