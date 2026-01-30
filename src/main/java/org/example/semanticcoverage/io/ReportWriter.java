package org.example.semanticcoverage.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.example.semanticcoverage.model.RequirementCoverage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportWriter {

    public void writeAll(Path outDir, List<RequirementCoverage> coverages) throws Exception {
        log.info("writing result...");

        Files.createDirectories(outDir);

        Path csv = outDir.resolve("coverage_report.csv");
        StringBuilder sb = new StringBuilder();
        sb.append("RequirementId,RequirementText,CoverageScore,Confidence,MatchedTests,Evidence\n");

        for (RequirementCoverage reqCoverage: coverages) {
            String tests = reqCoverage.matches().stream()
                .map(m -> m.getTestId() + " (" + m.getTestPath() + ") sim=" + String.format("%.3f", m.getSimilarity()))
                .collect(Collectors.joining(" ; "));

            String evidence = reqCoverage.matches().isEmpty()
                ? ""
                : (reqCoverage.matches().getFirst().getSummary() + " | " + String.join(" | ",
                reqCoverage.matches().getFirst().getEvidence()));

            sb.append(escape(reqCoverage.requirementId())).append(",")
                .append(escape(reqCoverage.requirementText())).append(",")
                .append(reqCoverage.coverageScore()).append(",")
                .append(String.format("%.3f", reqCoverage.confidence())).append(",")
                .append(escape(tests)).append(",")
                .append(escape(evidence))
                .append("\n");
        }

        Files.writeString(csv, sb.toString());

        log.info("Result is ready!");
    }

    private String escape(String s) {
        if (s == null) return "";
        String x = s.replace("\"", "\"\"");
        return "\"" + x + "\"";
    }
}
