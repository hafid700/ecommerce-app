package com.example.ecommerceapp.agent;

// 🔑 Assurez-vous de bien importer Document de Spring AI et NON de javax.swing.text !
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class VectorStoreInitializer implements CommandLineRunner {

    private final VectorStore vectorStore;

    // Injection de la ressource PDF
    @Value("classpath:docs/Politiques_Livraison_COD_Maroc.pdf")
    private Resource pdfResource;

    public VectorStoreInitializer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
        try {
            // 1. Vérification de l'existence du fichier via getInputStream() ou exists()
            if (!pdfResource.exists()) {
                System.err.println("❌ Fichier PDF introuvable");
                return;
            }

            System.out.println("📄 Lecture du document PDF RAG : " + pdfResource.getFilename());

            // 2. Configuration du reader PDF (PagePdfDocumentReader accepte la Resource ou son chemin)
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .build();

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource, config);

            // 3. Extraction des documents Spring AI (List<org.springframework.ai.document.Document>)
            List<Document> documents = pdfReader.get();

            // 4. Découpage du texte (Utilisation du constructeur standard par défaut)
            TokenTextSplitter splitter = TokenTextSplitter.builder().build();
            List<Document> chunks = splitter.apply(documents);

            // 5. Sauvegarde dans le VectorStore
            vectorStore.add(chunks);

            System.out.println("✅ Succès : " + chunks.size() + " fragments extraits du PDF et stockés dans PgVector !");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation du VectorStore : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
