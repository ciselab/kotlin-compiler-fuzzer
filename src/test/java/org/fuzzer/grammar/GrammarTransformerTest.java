package org.fuzzer.grammar;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.utils.FileUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class GrammarTransformerTest {

    private LexerGrammar lexerGrammar;

    private Grammar parserGrammar;

    private GrammarTransformer transformer;

    @BeforeEach
    void setUp() throws RecognitionException, IOException {
        String lexerText = FileUtilities.fileContentToString(new File("src/main/resources/KotlinLexer.g4"));
        String parserText = FileUtilities.fileContentToString(new File("src/main/resources/KotlinParser.g4"));

        lexerGrammar = new LexerGrammar(lexerText);
        parserGrammar = new Grammar(parserText);

        transformer = new GrammarTransformer(lexerGrammar, parserGrammar);
    }

    @Test
    void testGrammarTransformer() {
        ASTNode node = transformer.transformGrammar();
    }

}