package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.RuleName;
import org.fuzzer.representations.callables.KFunction;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.context.KScope;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.List;

public class FunctionDecl extends ASTNode {
    public FunctionDecl(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        switch (ctx.getScope()) {
            // A simple function declaration
            case GLOBAL_SCOPE -> {
                CodeFragment code = new CodeFragment();
                code.appendToText(RuleName.fun + " ");
                String funcName = ctx.getNewIdentifier();
                code.appendToText(funcName + " (");

                // Sample a return type
                KType returnType = ctx.getRandomType();

                // Sample some parameters
                int numberOfParams = rng.fromGeometric();
                ParameterNode parameterNode = new ParameterNode(this.antlrNode);
                List<KType> sampledTypes = new ArrayList<>();
                List<String> sampledIds = new ArrayList<>();

                for (int i = 0; i < numberOfParams; i++) {
                    CodeFragment sampledParam = parameterNode.getSample(rng, ctx);

                    // Cache the sample parameters
                    sampledTypes.add(parameterNode.getSampledType());
                    sampledIds.add(parameterNode.getSampledId());
                    code.appendToText(sampledParam);
                }

                ctx.addIdentifier(funcName, new KFunction(funcName, sampledTypes, returnType));

                // Clone after adding the function to allow for recursion
                Context clone = ctx.clone();

                clone.addIdentifier(funcName, new KFunction(funcName, sampledTypes, returnType));

                // Change the scope to a function
                clone.updateScope(KScope.FUNCTION_SCOPE);

                code.appendToText(") : " + returnType.toString() + " {" + System.lineSeparator());

                // Sample some expressions in the function body
                int numberOfStatements = rng.fromGeometric();

                ExpressionNode expr = new ExpressionNode(antlrNode, 3);

                for (int i = 0; i < numberOfParams; i++) {
                    CodeFragment sampleExpr = expr.getSample(rng, ctx);
                    code.extend(sampleExpr);
                }

                // Sample a consistently-types return statement
                String returnStatement = expr.getSampleOfType(rng, ctx, returnType).getText();
                code.extend("return " + returnStatement);

                code.extend(new CodeFragment("}"));

                return code;
            }
            default -> {
                throw new UnsupportedOperationException("Cannot yet sample from " + ctx.getScope() + " scopes.");
            }
        }
    }

    @Override
    public void invariant() {

    }
}
