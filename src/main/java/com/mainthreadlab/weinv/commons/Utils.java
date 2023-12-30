package com.mainthreadlab.weinv.commons;

import io.micrometer.core.instrument.util.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class Utils {

    private Utils() {
    }

    public static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public static String getResponseBody(CloseableHttpResponse response) throws IOException {
        try (InputStream is = response.getEntity() == null ? null : response.getEntity().getContent()) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    public static String readFileFromResource(String fileName) {
        ClassLoader classLoader = Utils.class.getClassLoader();
        InputStream resource = classLoader.getResourceAsStream(fileName);
        if (resource == null) {
            throw new IllegalStateException("FIle not found! " + fileName);
        } else {
            return IOUtils.toString(resource, StandardCharsets.UTF_8);
        }
    }

    public static String checkTransactionId(String transactionId) {
        // checking: allow only value UUID-4 for transactionId
        if (transactionId == null) {
            return UUID.randomUUID().toString();
        } else {
            UUID.fromString(transactionId);
            return transactionId;
        }
    }

    public static String getFormattedDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String getFormattedTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(date);
    }

    public static LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static boolean isSourceDateBeforeTargetDate(Date source, Date target) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate sourceLocalDate = LocalDate.parse(getFormattedDate(source), dtf);
        LocalDate targetLocalDate = LocalDate.parse(getFormattedDate(target), dtf);
        return sourceLocalDate.isBefore(targetLocalDate);
    }
}
