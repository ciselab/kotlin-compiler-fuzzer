package org.fuzzer.generator;

import java.util.Objects;

public class CodeFragment {
    private String text;

    private String structureName;

    public CodeFragment() {
        this.text = "";
        this.structureName = null;
    }

    public CodeFragment(String text) {
        this.text = text;
        this.structureName = null;
    }

    public void setName(String name) {
        this.structureName = name;
    }

    public String getName() {
        return structureName;
    }

    public boolean isStructure() {
        return structureName != null;
    }

    public String getText() {
        return this.text;
    }

    public Long size() {
        return (long) this.text.length();
    }

    public void appendToText(CodeFragment code) {
        appendToText(code.getText());
    }

    public void appendToText(String snippet) {
        this.text += snippet;
    }

    public void extend(CodeFragment code) {
        extend(code.getText());
    }

    public void extend(String text) {
        appendToText(System.lineSeparator());
        appendToText(text);
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
        return Objects.equals(structureName, that.structureName);
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (structureName != null ? structureName.hashCode() : 0);
        return result;
    }
}
