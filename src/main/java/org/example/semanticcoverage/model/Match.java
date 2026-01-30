package org.example.semanticcoverage.model;

import java.util.List;

import lombok.Getter;

@Getter
public class Match {

    private final String testId;
    private final String testPath;
    private final double similarity;   // 0..1
    private final double confidence;   // 0..1
    private final String summary;
    private final List<String> evidence;

    public Match(String testId, String testPath, double similarity, double confidence, String summary, List<String> evidence) {
        this.testId = testId;
        this.testPath = testPath;
        this.similarity = similarity;
        this.confidence = confidence;
        this.summary = summary;
        this.evidence = evidence;
    }
}
