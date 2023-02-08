package org.fuzzer

import org.fuzzer.representations.context.Context
import org.jetbrains.kotlin.spec.grammar.tools.*
import org.fuzzer.utils.FileUtilities.*;
import org.fuzzer.utils.RandomNumberGenerator
import java.io.File


class KMain {
    fun foo(): KotlinParseTree? {
        val tokens = try {
//            val fileName : String = "src/test/resources/kotlin/Any.kt"
            val fileName : String = "src/test/resources/kotlin/Comparable.kt"
            val fileContents = fileContentToString(File(fileName));
            tokenizeKotlinCode("val x = foo() + 10;")
            tokenizeKotlinCode(fileContents);
        } catch (e: KotlinLexerException) {
            println("Tokenization the code fails")
            return null
        }
        val parseTree = try {
            parseKotlinCode(tokens)
        } catch (e: KotlinParserException) {
            println("Parsing the code fails")
            return null
        }

        return parseTree
    }
}

fun main(args: Array<String>) {
    val tree : KotlinParseTree? = KMain().foo()
    print("done")
    val rng : RandomNumberGenerator = RandomNumberGenerator(0);
    val ctx : Context = Context(rng);

    ctx.fromParseTree(tree);
}