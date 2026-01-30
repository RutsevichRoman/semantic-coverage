package org.example.semanticcoverage.extract;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.stereotype.Service;

@Service
public class FileWriter {

    private final ObjectMapper om = new ObjectMapper();

    public void writeToResources(Path repo, List<TestCaseDoc> tests) throws IOException {
        Path outDir = repo.resolve("src").resolve("main").resolve("resources");

        final Path cacheTestDir = outDir.resolve("cache").resolve("test-ai");
        Files.createDirectories(cacheTestDir);

        for (TestCaseDoc testDoc : tests) {
            final Path cached = cacheTestDir.resolve(testDoc.methodName + ".json");
            if (Files.notExists(cached)) {
                om.writerWithDefaultPrettyPrinter().writeValue(cached.toFile(), testDoc);
            }
        }
    }
}
