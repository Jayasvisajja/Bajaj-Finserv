package com.example.webhookapp.service;

import com.example.webhookapp.model.Solution;
import com.example.webhookapp.repo.SolutionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final SolutionRepository solutionRepository;

    @Value("${app.generate-webhook-url}")
    private String generateWebhookUrl;

    @Value("${app.submit-webhook-url}")
    private String submitWebhookUrl;

    @Value("${app.person.name}")
    private String personName;

    @Value("${app.person.regNo}")
    private String regNo;

    @Value("${app.person.email}")
    private String personEmail;

    public WebhookService(SolutionRepository solutionRepository) {
        this.solutionRepository = solutionRepository;
    }

    public void performFullFlow() {
        try {
            logger.info("Starting webhook generation flow for regNo={}", regNo);

            Map<String, String> payload = new HashMap<>();
            payload.put("name", personName);
            payload.put("regNo", regNo);
            payload.put("email", personEmail);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            logger.info("Calling generateWebhook: {}", generateWebhookUrl);
            ResponseEntity<String> resp = restTemplate.postForEntity(generateWebhookUrl, request, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                logger.error("generateWebhook failed: status={}, body={}", resp.getStatusCode(), resp.getBody());
                return;
            }

            JsonNode root = mapper.readTree(resp.getBody());
            String webhookUrl = root.has("webhook") ? root.get("webhook").asText() : null;
            String accessToken = root.has("accessToken") ? root.get("accessToken").asText() : null;

            logger.info("Received webhookUrl={}, accessTokenPresent={}", webhookUrl, accessToken != null);

            if (webhookUrl == null || accessToken == null) {
                logger.error("Missing webhook or accessToken. Response: {}", resp.getBody());
                return;
            }

            String lastTwo = getLastTwoDigits(regNo);
            int lastTwoInt = Integer.parseInt(lastTwo);
            boolean isOdd = (lastTwoInt % 2 == 1);
            String questionId = isOdd ? "QUESTION_1" : "QUESTION_2";

            logger.info("Selected question: {}", questionId);

            String finalQuery = buildFinalQueryForQuestion(questionId);

            if (finalQuery == null || finalQuery.trim().isEmpty()) {
                logger.error("finalQuery empty. Abort.");
                return;
            }

            // persist
            Solution s = new Solution(regNo, questionId, finalQuery);
            solutionRepository.save(s);
            logger.info("Saved solution id={} to DB", s.getId());

            // submit to webhook returned by server
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            // Use accessToken exactly as returned (server said it will be a JWT)
            submitHeaders.set("Authorization", accessToken);

            Map<String, String> submitBody = new HashMap<>();
            submitBody.put("finalQuery", finalQuery);

            HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(submitBody, submitHeaders);

            logger.info("Submitting finalQuery to webhook URL: {}", webhookUrl);
            ResponseEntity<String> submitResp = restTemplate.exchange(webhookUrl, HttpMethod.POST, submitRequest, String.class);

            logger.info("Submission response: status={}, body={}", submitResp.getStatusCode(), submitResp.getBody());

        } catch (Exception ex) {
            logger.error("Error during full flow", ex);
        }
    }

    private String getLastTwoDigits(String regNo) {
        String trimmed = regNo == null ? "" : regNo.trim();
        if (trimmed.length() < 2) return "0";
        String lastTwo = trimmed.substring(trimmed.length() - 2);
        if (!lastTwo.chars().allMatch(Character::isDigit)) {
            String digits = trimmed.replaceAll("\\D+", "");
            if (digits.length() >= 2) {
                return digits.substring(digits.length() - 2);
            } else if (digits.length() == 1) {
                return "0" + digits;
            } else {
                return "0";
            }
        }
        return lastTwo;
    }

    private String buildFinalQueryForQuestion(String questionId) {
        if ("QUESTION_1".equals(questionId)) {
            // final SQL derived from uploaded PDF (Question 1). See file: SQL Question 1 JAVA .pdf. :contentReference[oaicite:1]{index=1}
            return "SELECT \n" +
                   "  e.emp_id AS EMP_ID,\n" +
                   "  e.first_name AS FIRST_NAME,\n" +
                   "  e.last_name AS LAST_NAME,\n" +
                   "  d.department_name AS DEPARTMENT_NAME,\n" +
                   "  (\n" +
                   "    SELECT COUNT(*) \n" +
                   "    FROM employee e2 \n" +
                   "    WHERE e2.department = e.department\n" +
                   "      AND e2.dob > e.dob\n" +
                   "  ) AS YOUNGER_EMPLOYEES_COUNT\n" +
                   "FROM employee e\n" +
                   "JOIN department d ON e.department = d.department_id\n" +
                   "ORDER BY e.emp_id DESC;";
        } else {
            // QUESTION_2 placeholder (not used for your regNo)
            return "/* Question 2 SQL placeholder */";
        }
    }
}
