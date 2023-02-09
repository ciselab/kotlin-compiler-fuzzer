package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.generator.CodeGenerator;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KInterfaceType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.RandomNumberGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException, CloneNotSupportedException {
//        File lexerFile = new File("./src/main/resources/KotlinLexer.g4");
//        File parserFile = new File("./src/main/resources/KotlinParser.g4");
//
//        LexerGrammar lexerGrammar = new LexerGrammar(FileUtilities.fileContentToString(lexerFile));
//        Grammar parserGrammar = new Grammar(FileUtilities.fileContentToString(parserFile));
//
//        RuleHandler ruleHandler = new RuleHandler(lexerGrammar, parserGrammar);
//
//        RandomNumberGenerator rng = new RandomNumberGenerator(0);
//
//        for (int j = 0; j < 100; j ++) {
//            CodeFragment code = new CodeFragment();
//            CodeGenerator generator = new CodeGenerator(ruleHandler, rng, 3);
//            for (int i = 0; i < 15; i++) {
//                CodeFragment newCode = generator.sampleAssignment();
//                code.extend(newCode);
//            }
//            String randomFilename = UUID.randomUUID().toString();
//
//            String text = "fun main(args: Array<String>) {\n";
//            text += code.getText();
//            text += "\n}";
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(randomFilename + ".kt"));
//            writer.write(text);
//
//            writer.close();
//
//        }
    }
}
