package org.example.semanticcoverage.ai;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.semanticcoverage.model.Requirement;
import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementsTestSummaryMatcher {

    private static final String SYSTEM_PROMPT = """
        Ты классификатор соответствия автотеста бизнес-требованию.

        Тебе дано:
        - текст требования
        - краткое summary теста
        - фрагменты evidence (строки assert/endpoint/status)

        Задача: определить, покрывает ли тест требование.

        Классы:
        - MATCH: тест явно проверяет это требование
        - PARTIAL: проверяет часть, или рядом, но не полностью/неоднозначно
        - NO: не относится к требованию

        Правила:
        - Опирайся ТОЛЬКО на requirement, summary и evidence. Не додумывай.
        - Если данных недостаточно, выбирай PARTIAL с низкой уверенностью.
        - Верни СТРОГО JSON без markdown и без лишнего текста:

        { "verdict": "MATCH|PARTIAL|NO", "confidence": 0.0-1.0, "reason": "кратко 1 фраза" }
        """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AiService aiService;

    public record LlmVerdict(String verdict, double confidence, String reason) {}

    public LlmVerdict classifyMatch(Requirement r, TestCaseDoc t) {
        log.info("Classifying match for {} and {}", r.getId(), t.id());
        String prompt = """
            REQUIREMENT:
            %s

            TEST_SUMMARY:
            %s

            TEST_EVIDENCE:
            %s
            """.formatted(
            safe(r.getText()),
            safe(t.summary),
            toBullets(t.evidenceLines, 8)
        );

        String json = aiService.getAiSummary(SYSTEM_PROMPT, prompt);

        String normalized = extractJsonObject(json);
        try {
            log.info("Classified match for {} and {}", r.getId(), t.id());
            return objectMapper.readValue(normalized, LlmVerdict.class);
        } catch (Exception e) {
            // fail-safe: если LLM сломал JSON — считаем PARTIAL с низкой уверенностью
            log.error("ERROR in classifying match for {} and {}", r.getId(), t.id());
            return new LlmVerdict("PARTIAL", 0.2, "invalid_json_from_llm");
        }
    }

    private static String toBullets(List<String> lines, int limit) {
        if (lines == null || lines.isEmpty()) return "- (none)";
        return lines.stream().limit(limit).map(s -> "- " + s).collect(Collectors.joining("\n"));
    }

    private static String safe(String s) {return s == null ? "" : s;}

    private static String extractJsonObject(String s) {
        int a = s.indexOf('{');
        int b = s.lastIndexOf('}');
        if (a >= 0 && b > a) return s.substring(a, b + 1);
        return s;
    }

}
