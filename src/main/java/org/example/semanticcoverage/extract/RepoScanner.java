package org.example.semanticcoverage.extract;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class RepoScanner {

    public ScannedFiles scan(Path repoRoot, int testLimit) throws Exception {
        final List<Path> testFiles = scanDir(repoRoot, "src/test/java", testLimit);
        return new ScannedFiles(testFiles);
    }

    private List<Path> scanDir(Path repoRoot, String subDir, int limit) throws Exception {
        Path dir = repoRoot.resolve(subDir);
        if (!Files.exists(dir)) return List.of();

        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .limit(limit)
                .map(repoRoot::relativize) // относительные пути
                .toList();
        }
    }

    public record ScannedFiles(List<Path> testJavaFiles) {}
}
