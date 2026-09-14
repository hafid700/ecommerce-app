package com.example.ecommerceapp.agent;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    public record QuestionRequest(String conversationId, String question) {}

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> poserQuestion(@RequestBody QuestionRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Veuillez poser une question valide.");
            return ResponseEntity.badRequest().body(err);
        }

        String conversationId = (request.conversationId() != null && !request.conversationId().isBlank())
                ? request.conversationId()
                : "default-session";

        try {
            String reponse = agentService.poserQuestion(conversationId, request.question());
            Map<String, String> response = new HashMap<>();
            response.put("reponse", reponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Erreur lors du traitement par l'Agent IA : " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

}
