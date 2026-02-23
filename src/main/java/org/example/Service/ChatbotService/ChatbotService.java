package org.example.Service.ChatbotService;

import org.example.Model.Chatbot.ChatMessage;
import org.example.Model.User.User;
import org.example.Model.User.UserStatus;
import org.example.Repository.UserRepository;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ChatbotService {

    private static volatile boolean schemaReady = false;

    private final Connection cnx;
    private final UserRepository userRepository;

    public ChatbotService() {
        this.cnx = MaConnexion.getInstance().getCnx();
        this.userRepository = new UserRepository();
        ensureSchema();
        seedFaq();
    }

    public String sendMessage(int userId, String message) {
        String userMsg = message == null ? "" : message.trim();
        if (userMsg.isBlank()) {
            throw new IllegalArgumentException("Message vide.");
        }

        saveMessage(userId, userMsg, "USER");
        String botReply = buildBotReply(userId, userMsg);
        saveMessage(userId, botReply, "BOT");
        return botReply;
    }

    public List<ChatMessage> getHistory(int userId) {
        String sql = "SELECT id,user_id,message,sender,created_at FROM chat_messages WHERE user_id=? ORDER BY created_at ASC, id ASC";
        List<ChatMessage> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ChatMessage(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("message"),
                            rs.getString("sender"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture historique chatbot: " + e.getMessage(), e);
        }
        return out;
    }

    private String buildBotReply(int userId, String message) {
        String m = normalize(message);

        Optional<User> u = userRepository.findById(userId);
        if (m.contains("compte bloque") || m.contains("compte bloqu") || m.contains("blocked")) {
            if (u.isPresent() && u.get().getStatus() == UserStatus.REFUSE) {
                return "Votre compte est refuse. Contactez le support ou mettez a jour vos informations KYC puis reessayez.";
            }
            if (u.isPresent() && u.get().getStatus() == UserStatus.EN_ATTENTE) {
                return "Votre compte est en attente de validation admin. Vous recevrez une notification apres verification.";
            }
            return "Votre compte semble actif. Si vous avez un probleme de connexion, utilisez 'Mot de passe oublie ?'.";
        }

        if (m.contains("changer mon email") || m.contains("changer email") || m.contains("change email")) {
            return "Pour changer votre email: Dashboard Client > Profil > modifier email > Enregistrer.";
        }

        if (m.contains("mot de passe") || m.contains("password") || m.contains("reset")) {
            return "Pour reinitialiser le mot de passe: page Connexion > 'Mot de passe oublie ?' puis suivez le code recu par email.";
        }

        FaqAnswer faq = findBestFaqAnswer(m);
        if (faq != null) {
            return faq.answer();
        }

        return "Je n'ai pas compris. Essayez: 'mot de passe', 'compte bloque', 'changer mon email', 'notifications'.";
    }

    private FaqAnswer findBestFaqAnswer(String normalizedMessage) {
        String sql = "SELECT question, answer, keywords FROM faq";
        List<FaqAnswer> answers = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                answers.add(new FaqAnswer(
                        rs.getString("question"),
                        rs.getString("answer"),
                        rs.getString("keywords")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture FAQ: " + e.getMessage(), e);
        }

        return answers.stream()
                .map(a -> new ScoredFaq(a, score(normalizedMessage, a.keywords())))
                .filter(s -> s.score > 0)
                .max(Comparator.comparingInt(s -> s.score))
                .map(s -> s.faq)
                .orElse(null);
    }

    private int score(String normalizedMessage, String keywords) {
        if (keywords == null || keywords.isBlank()) return 0;
        String[] parts = keywords.toLowerCase(Locale.ROOT).split(",");
        int score = 0;
        for (String p : parts) {
            String kw = p.trim();
            if (!kw.isEmpty() && normalizedMessage.contains(kw)) {
                score++;
            }
        }
        return score;
    }

    private void saveMessage(int userId, String message, String sender) {
        String sql = "INSERT INTO chat_messages(user_id,message,sender,created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.setString(3, sender);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur sauvegarde message chatbot: " + e.getMessage(), e);
        }
    }

    private void ensureSchema() {
        if (schemaReady) return;
        synchronized (ChatbotService.class) {
            if (schemaReady) return;
            String faqSql = """
                    CREATE TABLE IF NOT EXISTS faq (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        question VARCHAR(255) NOT NULL,
                        answer TEXT NOT NULL,
                        keywords VARCHAR(500) NOT NULL
                    )
                    """;
            String chatSql = """
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        user_id INT NOT NULL,
                        message TEXT NOT NULL,
                        sender ENUM('USER','BOT') NOT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_chat_user (user_id),
                        CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """;
            try (PreparedStatement p1 = cnx.prepareStatement(faqSql);
                 PreparedStatement p2 = cnx.prepareStatement(chatSql)) {
                p1.execute();
                p2.execute();
                schemaReady = true;
            } catch (SQLException e) {
                throw new RuntimeException("Erreur creation schema chatbot: " + e.getMessage(), e);
            }
        }
    }

    private void seedFaq() {
        String countSql = "SELECT COUNT(*) FROM faq";
        try (PreparedStatement countPs = cnx.prepareStatement(countSql);
             ResultSet rs = countPs.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification FAQ: " + e.getMessage(), e);
        }

        insertFaq("Comment reinitialiser mon mot de passe ?",
                "Allez sur la page Connexion puis cliquez sur 'Mot de passe oublie ?'.",
                "reset,password,mot de passe,oublie,reinitialiser");

        insertFaq("Comment voir mes notifications ?",
                "Dans Dashboard Client, cliquez sur 'Voir historique' dans la zone Notifications.",
                "notification,historique,message,alerte");

        insertFaq("Comment modifier mon profil ?",
                "Ouvrez Dashboard Client > Profil, modifiez les champs puis cliquez sur Modifier.",
                "profil,modifier,email,telephone,nom");

        insertFaq("Pourquoi mon acces est limite ?",
                "Votre KYC doit etre approuve pour debloquer toutes les sections (Wallet, Loan, Budget...).",
                "kyc,acces limite,refuse,en attente,approuve");
    }

    private void insertFaq(String question, String answer, String keywords) {
        String sql = "INSERT INTO faq(question,answer,keywords) VALUES(?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, question);
            ps.setString(2, answer);
            ps.setString(3, keywords);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur insertion FAQ: " + e.getMessage(), e);
        }
    }

    private String normalize(String v) {
        return v == null ? "" : v.toLowerCase(Locale.ROOT).trim();
    }

    private record FaqAnswer(String question, String answer, String keywords) {}
    private record ScoredFaq(FaqAnswer faq, int score) {}
}
