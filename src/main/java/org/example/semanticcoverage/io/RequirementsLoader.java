package org.example.semanticcoverage.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.example.semanticcoverage.model.Requirement;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequirementsLoader {

    private static final Pattern REQ = Pattern.compile("^(REQ-\\d+)\\s*:\\s*(.+)$");

    public List<Requirement> load(Path requirementsMd) throws Exception {
        final List<Requirement> out = new ArrayList<>();
        for (String line : Files.readAllLines(requirementsMd)) {
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            Matcher m = REQ.matcher(s);
            if (m.find()) out.add(new Requirement(m.group(1), m.group(2)));
        }
        if (out.size() < 5) {
            log.warn("WARN: requirements < 5. Found: {}", out.size());
        }
        return out;
    }
}
