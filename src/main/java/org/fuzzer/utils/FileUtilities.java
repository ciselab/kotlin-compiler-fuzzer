package org.fuzzer.utils;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.ast.GrammarAST;
import org.jetbrains.kotlin.spec.grammar.tools.KotlinParseTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.antlr.v4.runtime.CharStreams.fromFileName;

public class FileUtilities {
    public static String fileContentToString(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
