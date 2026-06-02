package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloudinaryRestService {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final ObjectMapper mapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${cloudinary.cloud-name}") private String cloudName;
    @Value("${cloudinary.api-key}") private String apiKey;
    @Value("${cloudinary.api-secret}") private String apiSecret;
    @Value("${cloudinary.folder:bankcards/images}") private String folder;
    private final CardRepository cardRepository;

    public Card uploadImage(Long cardId, MultipartFile file) throws Exception {
        APP_LOG.info("Uploading image for card: {}", cardId);

        if (cloudName == null || cloudName.isBlank()) {
            throw new IllegalStateException("cloudinary.cloud-name is not configured");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        validateImage(file);

        String publicId = "card_" + cardId;
        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, String> signParams = Map.of(
                "timestamp", String.valueOf(timestamp),
                "public_id", publicId,
                "folder", folder
        );
        String signature = generateSignature(signParams, apiSecret);

        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUri = "data:" + file.getContentType() + ";base64," + base64;

        String body = "file=" + encode(dataUri) +
                "&api_key=" + encode(apiKey) +
                "&timestamp=" + encode(String.valueOf(timestamp)) +
                "&signature=" + signature +
                "&folder=" + encode(folder) +
                "&public_id=" + encode(publicId);

        String url = "https://api.cloudinary.com/v1_1/" + cloudName.trim() + "/image/upload";
        APP_LOG.debug("Cloudinary request: POST {}", url);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res;
        try {
            res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            if (e.getCause() instanceof java.nio.channels.UnresolvedAddressException) {
                APP_LOG.error("DNS resolution failed for api.cloudinary.com. Check network/DNS settings.");
                throw new RuntimeException("Cannot connect to Cloudinary: DNS resolution failed", e);
            }
            APP_LOG.error("Network error: {}", e.getMessage());
            throw new RuntimeException("Cloudinary connection failed", e);
        }

        if (res.statusCode() != 200) {
            APP_LOG.error("Cloudinary error ({}): {}", res.statusCode(), res.body());
            throw new IOException("Upload failed: " + res.body());
        }

        Map<String, Object> json = mapper.readValue(res.body(), Map.class);
        String returnedPublicId = (String) json.get("public_id");
        card.setCloudinaryPublicId(returnedPublicId);

        APP_LOG.info("Uploaded: card={} public_id={}", cardId, returnedPublicId);
        return cardRepository.save(card);
    }

    public void deleteImage(Long cardId) throws Exception {
        APP_LOG.info("Deleting image for card: {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (card.getCloudinaryPublicId() == null) return;

        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, String> signParams = Map.of(
                "public_id", card.getCloudinaryPublicId(),
                "timestamp", String.valueOf(timestamp)
        );
        String signature = generateSignature(signParams, apiSecret);

        String body = "api_key=" + encode(apiKey) +
                "&public_id=" + encode(card.getCloudinaryPublicId()) +
                "&timestamp=" + encode(String.valueOf(timestamp)) +
                "&signature=" + signature;

        String url = "https://api.cloudinary.com/v1_1/" + cloudName.trim() + "/image/destroy";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> json = mapper.readValue(res.body(), Map.class);

        if (!"ok".equals(json.get("result"))) {
            throw new IOException("Delete failed: " + res.body());
        }

        card.setCloudinaryPublicId(null);
        cardRepository.save(card);
        APP_LOG.info("Deleted: card={}", cardId);
    }

    public String getImageUrl(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (card.getCloudinaryPublicId() == null) return "https://res.cloudinary.com/" + cloudName + "/image/upload/f_auto,q_auto/.jpg";
        return "https://res.cloudinary.com/" + cloudName + "/image/upload/f_auto,q_auto/" + card.getCloudinaryPublicId() + ".jpg";
    }

    private String generateSignature(Map<String, String> params, String apiSecret) throws Exception {
        String sorted = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String stringToSign = sorted + apiSecret;

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(stringToSign.getBytes(StandardCharsets.UTF_8));

        return String.format("%040x", new BigInteger(1, hash));
    }


    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void validateImage(MultipartFile f) {
        if (f.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (f.getSize() > 5_242_880) throw new IllegalArgumentException("Max size 5MB");
        if (f.getContentType() == null || !f.getContentType().startsWith("image/")) throw new IllegalArgumentException("Images only");
    }
}