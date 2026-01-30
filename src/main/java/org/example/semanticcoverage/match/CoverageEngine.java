package org.example.semanticcoverage.match;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.example.semanticcoverage.ai.RequirementsTestSummaryMatcher;
import org.example.semanticcoverage.model.Match;
import org.example.semanticcoverage.model.Requirement;
import org.example.semanticcoverage.model.RequirementCoverage;
import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoverageEngine {

    private static final int RERANK_K = 10;

    private final RequirementsTestSummaryMatcher requirementsTestSummaryMatcher;

    public List<RequirementCoverage> compute(List<Requirement> reqs, List<TestCaseDoc> tests) {
        final List<RequirementCoverage> out = new ArrayList<>();

        for (Requirement req : reqs) {
            List<TestCandidate> candidates = tests.stream()
                .map(testCase -> new TestCandidate(testCase, SimilarityUtils.cosine(req.getEmbedding(), testCase.embedding)))
                .sorted((a,b) -> Double.compare(b.sim, a.sim))
                .limit(RERANK_K)
                .toList();

            List<Match> matches = new ArrayList<>();
            for (TestCandidate testCandidate: candidates) {
                var verdict = requirementsTestSummaryMatcher.classifyMatch(req, testCandidate.test);

                double finalScore = testCandidate.sim;
                double finalConf = clamp01(0.2 + 0.8 * verdict.confidence()); // или своя формула

                // можно “обнулить” NO
                if ("NO".equalsIgnoreCase(verdict.verdict())) {
                    finalScore *= 0.2;
                    finalConf *= 0.3;
                } else if ("PARTIAL".equalsIgnoreCase(verdict.verdict())) {
                    finalScore *= 0.7;
                } // MATCH оставляем как есть

                matches.add(new Match(
                    testCandidate.test.id(),
                    testCandidate.test.filePath,
                    finalScore,
                    finalConf,
                    testCandidate.test.summary + " | LLM: " + verdict.verdict() + " (" + verdict.reason() + ")",
                    testCandidate.test.evidenceLines.size() > 5 ? testCandidate.test.evidenceLines.subList(0, 5) : testCandidate.test.evidenceLines
                ));
            }

            // финальная сортировка после rerank
            matches.sort((a,b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

            Match best = matches.isEmpty() ? null : matches.getFirst();

            int coverageScore = 0;
            double confidence = 0.0;

            if (best != null) {
                String verdict = extractVerdict(best.getSummary()); // MATCH / PARTIAL / NO
                double sim = best.getSimilarity();

                if ("MATCH".equals(verdict)) {
                    coverageScore = (int) Math.round(sim * 100);       // 70–100
                    confidence = Math.min(1.0, best.getConfidence() + 0.3);
                } else if ("PARTIAL".equals(verdict)) {
                    coverageScore = (int) Math.round(sim * 70);        // 30–70
                    confidence = Math.min(1.0, best.getConfidence());
                } else { // NO
                    coverageScore = (int) Math.round(sim * 30);        // 0–30
                    confidence = Math.max(0.1, best.getConfidence() * 0.5);
                }
            }

            out.add(new RequirementCoverage(
                req.getId(),
                req.getText(),
                coverageScore,
                confidence,
                matches
            ));
        }

        return out;
    }

    private record TestCandidate(TestCaseDoc test, double sim) {}

    private static double clamp01(double x) { return Math.max(0.0, Math.min(1.0, x)); }

    private static String extractVerdict(String summary) {
        if (summary == null) return "NO";
        if (summary.contains("MATCH")) return "MATCH";
        if (summary.contains("PARTIAL")) return "PARTIAL";
        if (summary.contains("NO")) return "NO";
        return "PARTIAL";
    }
}
