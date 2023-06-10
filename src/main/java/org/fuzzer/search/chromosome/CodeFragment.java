package org.fuzzer.generator;

import java.util.Objects;

public class CodeFragment {
    private final String text;

    private final FragmentType fragmentType;

    public CodeFragment(String text, FragmentType fragmentType) {
        this.text = text;
        this.fragmentType = fragmentType;
    }

    public boolean isStructure() {
        return fragmentType == FragmentType.FUNC;
    }

    public String getText() {
        return this.text;
    }

    public Long size() {
        return (long) this.text.length();
    }

    public CodeFragment append(CodeFragment code) {
        return new CodeFragment(this.text + code.getText(), this.fragmentType);
    }

    public CodeFragment append(String snippet) {
        return new CodeFragment(this.text + snippet, this.fragmentType);
    }

    public CodeFragment extend(CodeFragment code) {
        return extend(code.getText());
    }

    public CodeFragment extend(String text) {
        CodeFragment code = append(System.lineSeparator());
        return code.append(text);
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeFragment that)) return false;

        if (!Objects.equals(text, that.text)) return false;
        return fragmentType == that.fragmentType;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (fragmentType != null ? fragmentType.hashCode() : 0);
        return result;
    }
}
