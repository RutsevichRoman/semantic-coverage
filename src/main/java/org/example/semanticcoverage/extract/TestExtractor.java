package org.example.semanticcoverage.extract;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.stereotype.Component;

@Component
public class TestExtractor {

    private static final Pattern TEST_METHOD = Pattern.compile("\\bvoid\\s+(test\\w+|should\\w+|given\\w+|when\\w+|then\\w+)\\s*\\(");
    private static final Pattern ASSERT_LINE = Pattern.compile("(assert|assertThat|Assertions\\.|status\\(\\)\\.is|/api/|/auth|/login|forbidden|unauthorized|bad request|not found)", Pattern.CASE_INSENSITIVE);

    public List<TestCaseDoc> extract(Path repo, List<Path> scopedFiles) throws Exception {
        List<TestCaseDoc> out = new ArrayList<>();
        for (Path rel : scopedFiles) {
            if (!rel.toString().contains("src/test/")) continue;
            Path abs = repo.resolve(rel);
            if (!Files.exists(abs) || !abs.toString().endsWith(".java")) continue;

            List<String> lines = Files.readAllLines(abs);
            String className = abs.getFileName().toString().replace(".java", "");

            //если в файле несколько @Test — создаём на каждый TestCaseDoc
            String currentMethod = null;
            List<String> evidence = new ArrayList<>();
            for (String line : lines) {
                var m = TEST_METHOD.matcher(line);
                if (m.find()) {
                    // flush предыдущего
                    if (currentMethod != null) {
                        out.add(new TestCaseDoc(rel.toString(), className, currentMethod, evidence));
                    }
                    currentMethod = m.group(1);
                    evidence = new ArrayList<>();
                    continue;
                }
                if (currentMethod != null && ASSERT_LINE.matcher(line).find()) {
                    evidence.add(line.trim());
                    if (evidence.size() > 8) evidence = evidence.subList(0, 8);
                }
            }
            if (currentMethod != null) {
                out.add(new TestCaseDoc(rel.toString(), className, currentMethod, evidence));
            }
        }
        return out;
    }

}
