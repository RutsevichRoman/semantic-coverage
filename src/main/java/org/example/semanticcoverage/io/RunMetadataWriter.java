package org.example.semanticcoverage.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.semanticcoverage.RunConfig;
import org.example.semanticcoverage.extract.RepoScanner;
import org.example.semanticcoverage.model.Requirement;
import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.stereotype.Component;

@Component
public class RunMetadataWriter {

    private final ObjectMapper om = new ObjectMapper();

    public void write(Path outDir, RunConfig cfg, RepoScanner.ScannedFiles scannedFiles, List<Requirement> reqs, List<TestCaseDoc> tests) throws Exception {
        Files.createDirectories(outDir);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("repo", cfg.repo);
        meta.put("requirementsFile", cfg.requirements);
        meta.put("testCodeFiles", scannedFiles.testJavaFiles().size());
        meta.put("requirementsCount", reqs.size());
        meta.put("extractedTestsCount", tests.size());
        meta.put("timestampUtc", new Date().toInstant().toString());

        Path metaFile = outDir.resolve("run_meta.json");
        om.writerWithDefaultPrettyPrinter().writeValue(metaFile.toFile(), meta);
    }
}
