package com.payae.payae.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class RazorpaySignatureUtil {

    public static boolean verifySignature(String orderId,
                                          String paymentId,
                                          String signature,
                                          String secret) {

        try {
            String payload = orderId + "|" + paymentId;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(secret.getBytes(), "HmacSHA256");

            sha256_HMAC.init(secretKey);

            byte[] hash = sha256_HMAC.doFinal(payload.getBytes());

            String generatedSignature =
                    Base64.getEncoder().encodeToString(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }
}