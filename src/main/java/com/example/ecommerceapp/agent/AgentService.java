package com.example.ecommerceapp.agent;

import com.example.ecommerceapp.agent.tools.CartTools;
import com.example.ecommerceapp.agent.tools.CategoryTools;
import com.example.ecommerceapp.agent.tools.ProductTools;
import com.example.ecommerceapp.agent.tools.UserTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final ChatClient chatClient;

    public AgentService(
            ChatModel chatModel,
            ProductTools productTools,
            CartTools cartTools,
            CategoryTools categoryTools,
            UserTools userTools,
            ChatMemory memory) {

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        Tu es l'assistant virtuel intelligent de notre boutique en ligne e-commerce.
                        Ton rôle est d'aider les clients à trouver des produits, aussi ajouter des produit au panier et 
                        vérifier les stocks et fournir des informations précises.
                        
                        Consignes importantes :
                        1. Utilise TOUJOURS les outils mis à ta disposition
                           pour interroger la base de données réelle avant de répondre.
                        2. Ne fabrique aucune donnée ou prix qui ne provient pas des outils.
                        3. Lorsque tu utilises l'outil 'preparerAjoutPanier' et qu'il retourne 'ADD_TO_CART_SUCCESS:PRODUIT_JSON|QUANTITE:X' :
                            a. Réponds poliment au client en lui résumant l'ajout.
                            b. Ajoute IMPÉRATIVEMENT la balise d'action à la toute fin de ton message en insérant le PRODUIT_JSON exact sans le modifier :
                        
                            [ACTION:ADD_TO_CART:PRODUIT_JSON:QUANTITE]
                            
                        4.- Si l'outil 'suivreCommande' retourne 'NON_AUTORISE', demande gentiment au client de se connecter à son compte.
                          - Si l'outil retourne 'ACCES_REFUSE', informe le client avec courtoisie qu'il ne peut consulter que ses propres commandes.
                     
                        Gestion de la prise de commande :
                            1. Quand le client dit qu'il veut passer commande ou valider son panier :
                               a. Utilise TOUJOURS d'abord l'outil 'obtenirProfilClient'.
                               b. Si l'outil retourne 'PROFIL_TROUVE' :\s
                                  - N'IMPOSE PAS au client de ressaisir ses informations.
                                  - Présente-lui directement le récapitulatif du panier et ses coordonnées enregistrées (Nom, Prénom, Adresse, Téléphone).
                                  - Demande-lui simplement de confirmer si l'adresse de livraison est toujours correcte.
                               c. Si l'outil retourne 'CLIENT_ANONYME' ou 'PROFIL_INCOMPLET' :
                                  - Demande-lui poliment de fournir son Nom, Prénom, Adresse complète et Téléphone.
                        
                            2. Une fois la confirmation du client obtenue :
                               - Utilise l'outil 'creerCommandeDirecte' avec les coordonnées (existantes ou nouvellement fournies).
                        
                            3. Si l'outil retourne 'COMMANDE_SUCCES' :
                               - Remercie le client et confirme-lui son numéro de commande.
                               - Ajoute IMPÉRATIVEMENT la balise [ACTION:CLEAR_CART] à la toute fin de ta réponse pour vider son panier local.
                                     
                        6. Reste courtois, clair, synthétique et réponds en français.
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build()
                )
                .defaultTools(productTools, cartTools, categoryTools, userTools)
                .build();
    }

    public String poserQuestion(
            String conversationId,
            String messageUtilisateur) {

        return this.chatClient.prompt()
                .user(messageUtilisateur)
                .advisors(advisorSpec -> advisorSpec.param(
                        "chat_memory_conversation_id",
                        conversationId
                ))
                .call()
                .content();
    }
}

