package org.fuzzer.grammar;

import org.stringtemplate.v4.ST;

import javax.swing.plaf.synth.SynthRadioButtonMenuItemUI;
import java.util.Collections;
import java.util.Map;

public class RuleName {
    public static final String kotlinFile = "kotlinFile";

    public static final String ALT = "ALT";

    public static final String OPTIONAL_BLOCK = "?";

    public static final String STAR_BLOCK = "*";

    public static final String BLOCK = "BLOCK";

    private static final String shebangLine = "shebangLine";

    static final Map<String, String> parserVocabulary = Map.ofEntries(
            Map.entry("", "!1")
    );
}
