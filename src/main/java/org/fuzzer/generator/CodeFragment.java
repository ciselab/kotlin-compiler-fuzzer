package org.fuzzer.generator;

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
}
