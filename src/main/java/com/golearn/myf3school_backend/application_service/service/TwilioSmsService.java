package com.golearn.myf3school_backend.application_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Gửi SMS qua Twilio REST API (không cần SDK, chỉ dùng java.net.http).
 *
 * application.properties cần thêm:
 *   twilio.account-sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   twilio.auth-token=your_auth_token
 *   twilio.from-number=+1234567890
 */
@Slf4j
@Service
public class TwilioSmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    public void send(String toPhone, String message) {
        try {
            // Twilio yêu cầu số E.164: +84xxxxxxxxx
            String normalizedTo = normalizePhone(toPhone);

            String url  = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            String body = "To="  + encode(normalizedTo)
                        + "&From=" + encode(fromNumber)
                        + "&Body=" + encode(message);

            String credentials = Base64.getEncoder().encodeToString(
                    (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 201) {
                log.info("SMS sent to {}", normalizedTo);
            } else {
                log.error("Twilio error {}: {}", res.statusCode(), res.body());
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhone, e.getMessage());
        }
    }

    /** Chuyển 09xx → +8x9xx (Vietnam). Nếu đã có + giữ nguyên. */
    private String normalizePhone(String phone) {
        phone = phone.replaceAll("[\\s\\-]", "");
        if (phone.startsWith("+")) return phone;
        if (phone.startsWith("0"))  return "+84" + phone.substring(1);
        return "+" + phone;
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}