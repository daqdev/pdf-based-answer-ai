package ar.daq.aispring.service;

import jakarta.websocket.server.ServerEndpoint;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfChatClient {
    private final VectorStore vectorStore;
    private ChatClient chatClient;

    PdfChatClient (VectorStore vectorStore, ChatClient chatClient) {

        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    String chat(String message) {

        var prompt = """
					You are assistant.
					Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
					If the answer is not present in the DOCUMENTS section state that you don´t know.
										
										
					DOCUMENTS:
					{documents}
										
					""";
        var listOfSimilarDocs = vectorStore.similaritySearch(message);
        var docs = listOfSimilarDocs.stream().map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
        var systemMessage = new SystemPromptTemplate(prompt)
                .createMessage(Map.of("documents", docs));
        var userMessage = new UserMessage(message);
        var promptList = new Prompt(List.of(systemMessage, userMessage));
        var aiResponse = this.chatClient.call(promptList);
        return aiResponse.getResult().getOutput().getContent();
    }

}

