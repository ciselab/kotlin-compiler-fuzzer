package org.fuzzer.representations.types;

import org.fuzzer.utils.KGrammarVocabulary;

/**
 * Represents a modifier. Some fields are omitted in the current implementation.
 * Below is the corresponding ANTLR4 definition.
 *
 * modifier
 *     : (classModifier
 *     | memberModifier
 *     | visibilityModifier
 *     | functionModifier
 *     | propertyModifier
 *     | inheritanceModifier
 *     | parameterModifier
 *     | platformModifier) NL*
 *     ;
 */

public record KTypeModifiers(String memberModifier,
                             String visibilityModifier,
                             String propertyModifier,
                             // TODO refactor in context extraction
                             String inheritanceModifier) {
    public boolean isVisibile() {
        return KGrammarVocabulary.visModifierPublic.equals(visibilityModifier);
    }

    public boolean isOpen() {
        return KGrammarVocabulary.inheritanceModifierOpen.equals(inheritanceModifier);
    }

    public boolean isConst() {
        return KGrammarVocabulary.propertyModifierConst.equals(propertyModifier);
    }

    public boolean overrides() {
        return KGrammarVocabulary.memberModifierOverrides.equals(memberModifier);
    }

    public boolean isAbstract() {
        return KGrammarVocabulary.memberModifierAbstract.equals(inheritanceModifier);
    }
}
