package com.example.ecommerceapp.agent.tools;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeTools {

    private final VectorStore vectorStore;

    public KnowledgeTools(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(
            name = "rechercherDocumentation",
            description = "Recherche des informations détaillées dans la base de connaissances (politiques de retour, garanties, livraison AMANA, FAQ, etc.)."
    )
    public String rechercherDocumentation(String question) {
        System.out.println("🤖 [Tool Call] Recherche RAG dans le VectorStore pour : " + question);

        // 1. Construction de la requête de recherche sémantique
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(3)
                .build();

        // 2. Exécution de la recherche vectorielle
        List<Document> resultats = vectorStore.similaritySearch(request);

        if (resultats == null || resultats.isEmpty()) {
            return "DOCUMENTATION_INTROUVABLE: Aucune information pertinente trouvée dans la base de connaissances.";
        }

        // 3. Extraction du texte via getText()
        return resultats.stream()
                .map(Document::getText) // 👈 Utilisez doc.getText() ou doc.getContent()
                .collect(Collectors.joining("\n---\n"));
    }
}