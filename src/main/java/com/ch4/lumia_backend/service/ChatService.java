package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.config.OpenAiProperties;
import com.ch4.lumia_backend.dto.ChatCompletionResponseDto;
import com.ch4.lumia_backend.exception.ChatCompletionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";

    private final RestTemplate openAiRestTemplate;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    public ChatCompletionResponseDto createCompletion(String message, String currentUserId) {
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("message는 비어 있을 수 없습니다.");
        }

        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            logger.error("OpenAI API key is not configured.");
            throw new ChatCompletionException(HttpStatus.SERVICE_UNAVAILABLE, "OpenAI API 키가 설정되지 않았습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiProperties.getApiKey().trim());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(buildRequestBody(message), headers);
        String requestedBy = StringUtils.hasText(currentUserId) ? currentUserId : "anonymous";

        try {
            logger.info("Calling OpenAI Responses API for user: {}, model: {}", requestedBy, openAiProperties.getModel());
            ResponseEntity<String> response = openAiRestTemplate.exchange(
                    OPENAI_RESPONSES_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            String reply = extractReply(response.getBody());
            if (!StringUtils.hasText(reply)) {
                logger.error("Reply was empty in OpenAI response for user: {}. body={}", requestedBy, abbreviate(response.getBody()));
                throw new ChatCompletionException(HttpStatus.BAD_GATEWAY, "OpenAI 응답을 해석하지 못했습니다.");
            }

            return new ChatCompletionResponseDto(reply.trim());
        } catch (RestClientResponseException e) {
            logger.error(
                    "OpenAI API request failed for user: {}. status={}, body={}",
                    requestedBy,
                    e.getStatusCode(),
                    abbreviate(e.getResponseBodyAsString()),
                    e
            );
            throw new ChatCompletionException(HttpStatus.BAD_GATEWAY, buildUpstreamErrorMessage(e.getResponseBodyAsString()));
        } catch (ResourceAccessException e) {
            if (isTimeoutException(e)) {
                logger.error("OpenAI API timed out for user: {}. message={}", requestedBy, e.getMessage(), e);
                throw new ChatCompletionException(HttpStatus.GATEWAY_TIMEOUT, "OpenAI 응답 시간이 초과되었습니다.");
            }

            logger.error("OpenAI API connection failed for user: {}. message={}", requestedBy, e.getMessage(), e);
            throw new ChatCompletionException(HttpStatus.BAD_GATEWAY, "OpenAI 연결에 실패했습니다.");
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse OpenAI response for user: {}. message={}", requestedBy, e.getMessage(), e);
            throw new ChatCompletionException(HttpStatus.BAD_GATEWAY, "OpenAI 응답을 해석하지 못했습니다.");
        } catch (RestClientException e) {
            logger.error("OpenAI client error for user: {}. message={}", requestedBy, e.getMessage(), e);
            throw new ChatCompletionException(HttpStatus.BAD_GATEWAY, "OpenAI 호출에 실패했습니다.");
        }
    }

    Map<String, Object> buildRequestBody(String message) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("input", message);
        requestBody.put("max_output_tokens", 300);
        return requestBody;
    }

    String extractReply(String responseBody) throws JsonProcessingException {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode outputTextNode = root.path("output_text");
        if (outputTextNode.isTextual() && StringUtils.hasText(outputTextNode.asText())) {
            return outputTextNode.asText();
        }

        JsonNode outputNode = root.path("output");
        if (!outputNode.isArray()) {
            return null;
        }

        for (JsonNode outputItem : outputNode) {
            JsonNode contentNode = outputItem.path("content");
            if (!contentNode.isArray()) {
                continue;
            }

            for (JsonNode contentItem : contentNode) {
                JsonNode textNode = contentItem.path("text");
                if (textNode.isTextual() && StringUtils.hasText(textNode.asText())) {
                    return textNode.asText();
                }
            }
        }

        return null;
    }

    private String buildUpstreamErrorMessage(String responseBody) {
        String errorMessage = extractErrorMessage(responseBody);
        if (StringUtils.hasText(errorMessage)) {
            return "OpenAI 호출에 실패했습니다: " + errorMessage;
        }
        return "OpenAI 호출에 실패했습니다.";
    }

    private String extractErrorMessage(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorMessageNode = root.path("error").path("message");
            if (errorMessageNode.isTextual()) {
                return errorMessageNode.asText();
            }
        } catch (JsonProcessingException e) {
            logger.debug("Failed to parse OpenAI error response: {}", e.getMessage());
        }

        return null;
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        int maxLength = 500;
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
