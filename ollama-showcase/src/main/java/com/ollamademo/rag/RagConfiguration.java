package com.ollamademo.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

    @Value("classpath:runbook.pdf")
    private Resource pdfResource;

    @Bean
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        // Creates the in-memory database
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public CommandLineRunner ingestPdf(VectorStore vectorStore) {
        return args -> {
            log.info("Starting document ingestion... This may take a few seconds.");

            // 1. Read the PDF
            PagePdfDocumentReader documentReader = new PagePdfDocumentReader(pdfResource);
            List<Document> documents = documentReader.get();

            // 2. Split text into manageable chunks
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);

            // Add this safety check!
            if (splitDocuments.isEmpty()) {
                log.error("No text could be extracted from the PDF! Skipping ingestion.");
                return;
            }

            // 3. Save to Vector Store (Ollama generates embeddings here)
            vectorStore.add(splitDocuments);

            log.info("Document ingested successfully! Vector store is ready.");
        };
    }
}