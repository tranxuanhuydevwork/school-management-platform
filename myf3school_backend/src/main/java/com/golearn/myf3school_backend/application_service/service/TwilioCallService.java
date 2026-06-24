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
 * Gọi điện thoại qua Twilio Voice REST API (không cần SDK).
 *
 * Twilio Voice hoạt động được tại Việt Nam (+84).
 * Nội dung được đọc bằng Google TTS tiếng Việt qua TwiML <Say>.
 *
 * application.properties cần thêm:
 *   twilio.account-sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   twilio.auth-token=your_auth_token
 *   twilio.from-number=+1234567890   ← số Twilio loại Voice-capable
 *
 * Lưu ý:
 *   - Twilio Voice hỗ trợ tiếng Việt qua engine "Google" (language="vi-VN").
 *   - Tính phí theo phút, xem giá tại console.twilio.com/voice/pricing.
 */
@Slf4j
@Service
public class TwilioCallService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    /**
     * Thực hiện cuộc gọi thoại và đọc nội dung {@code message} bằng TTS.
     *
     * @param toPhone số điện thoại người nhận (E.164 hoặc 09xx VN)
     * @param message nội dung cần đọc (tối đa ~1000 ký tự)
     */
    public void call(String toPhone, String message) {
        try {
            String normalizedTo = normalizePhone(toPhone);
            String twiml = buildTwiml(message);

            String url = "https://api.twilio.com/2010-04-01/Accounts/"
                    + accountSid + "/Calls.json";

            // Dùng Twiml inline — không cần host TwiML server riêng
            String body = "To="    + encode(normalizedTo)
                        + "&From=" + encode(fromNumber)
                        + "&Twiml=" + encode(twiml);

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

            // Twilio trả 201 khi cuộc gọi được khởi tạo thành công
            if (res.statusCode() == 201) {
                log.info("[TwilioCall] Call initiated to {}", normalizedTo);
            } else {
                log.error("[TwilioCall] Error {}: {}", res.statusCode(), res.body());
            }

        } catch (Exception e) {
            log.error("[TwilioCall] Failed to call {}: {}", toPhone, e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Tạo TwiML dùng Google TTS tiếng Việt.
     * <Say language="vi-VN" voice="Google"> — yêu cầu tài khoản Twilio đã bật
     * Neural TTS (mặc định bật trên tài khoản mới).
     * Thêm <Pause> 1 giây cuối để người nghe không bị cúp máy đột ngột.
     */
    private String buildTwiml(String message) {
        String safe = escapeXml(message);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             + "<Response>"
             +   "<Say language=\"vi-VN\" voice=\"Google\">" + safe + "</Say>"
             +   "<Pause length=\"1\"/>"
             + "</Response>";
    }

    /** Chuẩn hóa số VN: 09xx → +849xx; đã có + thì giữ nguyên. */
    private String normalizePhone(String phone) {
        phone = phone.replaceAll("[\\s\\-]", "");
        if (phone.startsWith("+")) return phone;
        if (phone.startsWith("0"))  return "+84" + phone.substring(1);
        return "+" + phone;
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** Escape ký tự XML đặc biệt trong nội dung TwiML. */
    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&apos;");
    }
}