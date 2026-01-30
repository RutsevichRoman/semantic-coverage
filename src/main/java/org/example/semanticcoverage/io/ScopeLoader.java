package org.example.semanticcoverage.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ScopeLoader {

    public List<Path> load(Path repoRoot, Path scopeFile) throws Exception {
        List<String> lines = Files.readAllLines(scopeFile);
        List<Path> paths = new ArrayList<>();
        for (String l : lines) {
            String s = l.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            paths.add(Path.of(s));
        }
        // можно дополнительно проверить существование
        List<Path> missing = paths.stream().filter(p -> !Files.exists(repoRoot.resolve(p))).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            System.out.println("WARN: Some scoped files do not exist: " + missing.size());
        }
        return paths;
    }
}
