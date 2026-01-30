package org.example.semanticcoverage.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiService {

    private final ChatClient chat;

    public AiService(ChatClient.Builder chatBuilder) {
        this.chat = chatBuilder.build();
    }

    public String getAiSummary(String prompt, String rawSignals) {
        return chat.prompt()
            .system(prompt)
            .user(rawSignals)
            .call()
            .content();
    }
}
