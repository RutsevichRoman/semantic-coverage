package org.example.semanticcoverage.enrich;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.example.semanticcoverage.utils.Utils.getTestFileName;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.semanticcoverage.ai.AiService;
import org.example.semanticcoverage.model.Requirement;
import org.example.semanticcoverage.model.TestCaseDoc;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrichService {

    private static final String PROMPT_FOR_TESTS =
        """
            Ты QA-анализатор. По сигналам теста сформируй СТРОГО такую структуру (одной строкой):
            ACTION: <что делает тест>
            SUBJECT: <над какой сущностью/ресурсом>
            INTERFACE: <API/endpoint/метод или "unit">
            ASSERT: <что проверяется>

            Правила:
            - Используй ТОЛЬКО предоставленные строки (не додумывай).
            - Если endpoint/сущность не видны — напиши UNKNOWN.
            - Никакого лишнего текста.
            """;
//        """
//        Ты QA-анализатор. Суммируй, что проверяет автотест.
//        Правила:
//        - Используй ТОЛЬКО предоставленные строки (не додумывай).
//        - 1–2 предложения.
//        - Если данных недостаточно — так и скажи.
//        """;

    private final EmbeddingModel embedding;
    private final AiService aiService;

    public void enrich(Path cacheTestDir, List<TestCaseDoc> tests, List<Requirement> reqs) {
        enrichTestsWithSummariesAndEmbeddings(cacheTestDir, tests);
        enrichRequirementsWithEmbeddings(reqs);
    }

    private void enrichTestsWithSummariesAndEmbeddings(Path cacheTestDir, List<TestCaseDoc> tests) {
        for (TestCaseDoc t : tests) {
            if (Files.exists(getTestFileName(cacheTestDir, t))) {
                continue;
            }
            log.info("Enrich test {} with summary...", t.methodName);
            String summary = aiService.getAiSummary(PROMPT_FOR_TESTS, t.rawSignalsForAi());
            t.summary = summary;
            t.embedding = toFloatArray(embedding.embed(summary));
            log.info("Enrich finished for {} .", t.methodName);
        }
    }

    private void enrichRequirementsWithEmbeddings(List<Requirement> reqs) {
        for (Requirement r : reqs) {
            r.setEmbedding(toFloatArray(embedding.embed(r.getText())));
        }
    }

    private float[] toFloatArray(float[] doubles) {
        float[] v = new float[doubles.length];
        System.arraycopy(doubles, 0, v, 0, doubles.length);
        return v;
    }
}
