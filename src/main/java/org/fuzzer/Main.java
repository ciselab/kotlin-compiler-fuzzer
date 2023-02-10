package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.generator.CodeGenerator;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.representations.context.Context;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException, CloneNotSupportedException {
        File lexerFile = new File("./src/main/resources/KotlinLexer.g4");
        File parserFile = new File("./src/main/resources/KotlinParser.g4");

        LexerGrammar lexerGrammar = new LexerGrammar(FileUtilities.fileContentToString(lexerFile));
        Grammar parserGrammar = new Grammar(FileUtilities.fileContentToString(parserFile));

        RuleHandler ruleHandler = new RuleHandler(lexerGrammar, parserGrammar);

        RandomNumberGenerator rng = new RandomNumberGenerator(0);

        String classPath = "src/test/resources/kotlin/";
        List<String> classes = new ArrayList<>(List.of(new String[]{"Any.kt", "Comparable.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"}));

        Context ctx = new Context(rng);

        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();
        ctx.fromFileNames(fileNames);

        int numGenerations = 100;
        Context[] contexts = new Context[numGenerations];
        // Add some dummy values to the context
        ctx.addDefaultValue(ctx.getTypeByName("Byte"), "(0x48 as Byte)");
        ctx.addDefaultValue(ctx.getTypeByName("Float"), "1.0f");
        ctx.addDefaultValue(ctx.getTypeByName("Double"), "2.0");
        ctx.addDefaultValue(ctx.getTypeByName("Int"), "3");
        ctx.addDefaultValue(ctx.getTypeByName("Short"), "(4.toShort())");
        ctx.addDefaultValue(ctx.getTypeByName("Long"), "(5.toLong())");
        ctx.addDefaultValue(ctx.getTypeByName("Boolean"), "true");
        ctx.addDefaultValue(ctx.getTypeByName("Boolean"), "false");
        ctx.addDefaultValue(ctx.getTypeByName("String"), "\"fooBar\"");
        ctx.addDefaultValue(ctx.getTypeByName("Char"), "'w'");

        for (int j = 0; j < numGenerations; j ++) {
            contexts[j] = ctx.clone();
            CodeFragment code = new CodeFragment();
            CodeGenerator generator = new CodeGenerator(ruleHandler, rng, 3, contexts[j]);
            for (int i = 0; i < 10; i++) {
                CodeFragment newCode = generator.sampleAssignment();
                code.extend(newCode);
            }
            String randomFilename = "src/main/java/org/fuzzer/" + UUID.randomUUID();

            String text = "fun main(args: Array<String>) {\n";
            text += code.getText();
            text += "\n}";

            BufferedWriter writer = new BufferedWriter(new FileWriter(randomFilename + ".kt"));
            writer.write(text);

            writer.close();

        }
    }
}
