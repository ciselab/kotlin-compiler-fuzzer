package org.fuzzer

import org.fuzzer.representations.context.Context
import org.jetbrains.kotlin.spec.grammar.tools.*
import org.fuzzer.utils.FileUtilities.*;
import org.fuzzer.utils.RandomNumberGenerator
import java.io.File


class KMain {
    fun foo(fileName : String): KotlinParseTree? {
        val tokens = try {
            val fileContents = fileContentToString(File(fileName));
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
    val classPath = "src/test/resources/kotlin/";
    val classes = listOf("Any.kt", "Comparable.kt", "Arrays.kt",
        "Char.kt", "CharSequence.kt", "Number.kt",
        "Primitives.kt")

    val rng : RandomNumberGenerator = RandomNumberGenerator(0)
    val ctx : Context = Context(rng)

    val fileNames = classes.map { x -> classPath + x }
    ctx.fromFileNames(fileNames)
}