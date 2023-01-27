package org.fuzzer.generator;

public class CodeFragment {
    private String text;

    public CodeFragment() {
        this.text = "";
    }

    public CodeFragment(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void appendToText(String snippet) {
        this.text += snippet;
    }

    public void extend(CodeFragment code) {
        appendToText("\n");
        appendToText(code.getText());
    }

    @Override
    public String toString() {
        return text;
    }
}
