package org.example.semanticcoverage.model;

import java.util.List;

public class TestCaseDoc {

    public final String filePath;   // relative to repo root
    public final String className;
    public final String methodName;
    public final List<String> evidenceLines;

    public String summary;  // from LLM
    public float[] embedding;

    public TestCaseDoc(String filePath, String className, String methodName, List<String> evidenceLines) {
        this.filePath = filePath;
        this.className = className;
        this.methodName = methodName;
        this.evidenceLines = evidenceLines;
    }

    public String id() {
        return className + "#" + methodName;
    }

    public String rawSignalsForAi() {
        StringBuilder sb = new StringBuilder();
        sb.append("Test: ").append(id()).append("\n");
        sb.append("File: ").append(filePath).append("\n");
        sb.append("Evidence:\n");
        for (String line : evidenceLines) sb.append("- ").append(line).append("\n");
        return sb.toString();
    }
}
