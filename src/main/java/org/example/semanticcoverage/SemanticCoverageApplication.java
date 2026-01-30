package org.example.semanticcoverage;

import java.nio.file.Path;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.example.semanticcoverage.enrich.EnrichService;
import org.example.semanticcoverage.extract.FileWriter;
import org.example.semanticcoverage.extract.RepoScanner;
import org.example.semanticcoverage.extract.TestExtractor;
import org.example.semanticcoverage.io.ReportWriter;
import org.example.semanticcoverage.io.RequirementsLoader;
import org.example.semanticcoverage.io.RunMetadataWriter;
import org.example.semanticcoverage.match.CoverageEngine;
import org.example.semanticcoverage.model.Requirement;
import org.example.semanticcoverage.model.RequirementCoverage;
import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class SemanticCoverageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemanticCoverageApplication.class, args);
    }

    @Bean
    CommandLineRunner run(
        RunConfig cfg,
        RepoScanner repoScanner,
        RequirementsLoader requirementsLoader,
        TestExtractor testExtractor,
        EnrichService enrichService,
        CoverageEngine engine,
        ReportWriter reportWriter,
        RunMetadataWriter metadataWriter,
        FileWriter fileWriter
    ) {
        return args -> {
            cfg.loadFromArgs(args);

            Path repo = Path.of(cfg.repo);
            Path outDir = Path.of(cfg.out);
            List<Requirement> reqs = requirementsLoader.load(Path.of(cfg.requirements));

            RepoScanner.ScannedFiles scannedFiles = repoScanner.scan(repo, cfg.filesAmount);

            List<TestCaseDoc> tests = testExtractor.extract(repo, scannedFiles.testJavaFiles());

            // AI: summarize + embeddings (через Spring AI/Ollama)
            enrichService.enrich(tests, reqs);

            fileWriter.writeToResources(repo, tests);

            final List<RequirementCoverage> coverages = engine.compute(reqs, tests);

            reportWriter.writeAll(outDir, coverages);
            metadataWriter.write(outDir, cfg, scannedFiles, reqs, tests);

            log.info("Done. Output: {}", outDir.toAbsolutePath());
        };
    }

}
