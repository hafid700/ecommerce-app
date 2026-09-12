package com.example.ecommerceapp.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
// 👈 On accepte à la fois /api/images et /images
@RequestMapping({"/api/images", "/images"})
@CrossOrigin(origins = "*")
public class ImageController {

    // Crée le chemin du dossier uploads à la racine du projet Spring Boot
    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");

    public ImageController() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("📁 Dossier uploads créé à : " + uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossier uploads : " + e.getMessage());
        }
    }

    // 📤 UPLOAD D'IMAGE LOCAL
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Aucun fichier reçu");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Génération d'un nom unique avec UUID pour éviter d'écraser des fichiers du même nom
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }

            String newFileName = UUID.randomUUID().toString() + extension;
            Path destination = uploadDir.resolve(newFileName);

            // Copie physique du fichier dans le dossier uploads
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Image sauvegardée : " + destination.toAbsolutePath());

            // Réponse JSON retournée à Angular
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", "/api/images/" + newFileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Affiche la stacktrace complète dans la console IDE
            Map<String, String> err = new HashMap<>();
            err.put("error", "Erreur lors de la sauvegarde : " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    // 📥 LECTURE / AFFICHAGE D'IMAGE
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Path filePath = uploadDir.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Déterminer automatiquement le type MIME de la photo (png, jpg, etc.)
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}