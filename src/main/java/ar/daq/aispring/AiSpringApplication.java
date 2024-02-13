package ar.daq.aispring;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@SpringBootApplication
public class AiSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiSpringApplication.class, args);
	}

	@Component
	static class LinuxAiClient {
		private final VectorStore vectorStore;
		private final ChatClient chatClient;

		LinuxAiClient(VectorStore vectorStore, ChatClient chatClient) {
			this.vectorStore = vectorStore;
			this.chatClient = chatClient;
		}

		String chat(String message) {

			var promt = """
					You are assistant with questions about the Linux Operative System.
					       		Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
					       		If the answer is not present in the DOCUMENTS section state that you don´t know.
										
										
					DOCUMENTS:
					{documents}
										
					""";
			var listOfSimilarDocs = vectorStore.similaritySearch(message);
			var docs = listOfSimilarDocs.stream().map(Document::getContent)
					.collect(Collectors.joining(System.lineSeparator()));
			var systemMessage = new SystemPromptTemplate(promt)
					.createMessage(Map.of("documents", docs));
			var userMessage = new UserMessage(message);
			var promptList = new Prompt(List.of(systemMessage, userMessage));
			var aiResponse = this.chatClient.call(promptList);
			return aiResponse.getResult().getOutput().getContent();
		}

	}

	@Bean
	ApplicationRunner applicationRunner(
			VectorStore vectorStore,
			@Value("classpath:dnudesregulacion.pdf") Resource pdf,
			JdbcTemplate template,
			ChatClient client,
			LinuxAiClient  linuxAiClient) {
		return args -> {

//			setup(vectorStore, pdf, template);
			System.out.println(
					linuxAiClient.chat("que menciona el documento acerca de los clubes deportivos?")
			);
		};
	}

	private static void setup(VectorStore vectorStore, Resource pdf, JdbcTemplate template) {
		template.update("delete from vector_store");

		var config = PdfDocumentReaderConfig
			.builder()
			.withPageExtractedTextFormatter(
				new ExtractedTextFormatter
				.Builder()
				.withNumberOfBottomTextLinesToDelete(3)
				.build())
			.build();
		var reader = new PagePdfDocumentReader(pdf, config);

		var textSplitter = new TokenTextSplitter();

		var docs = textSplitter.apply(reader.get());

		vectorStore.accept(docs);
	}


}

