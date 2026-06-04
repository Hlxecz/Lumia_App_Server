package com.ch4.lumia_backend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ContentFilterService {

    private static final Logger logger = LoggerFactory.getLogger(ContentFilterService.class);
    
    private final RestTemplate restTemplate;

    @Value("${content-filter.enabled:true}")
    private boolean contentFilterEnabled;

    @Value("${content-filter.url:http://3.39.239.196:8000/filter_post}")
    private String contentFilterUrl;

    public ContentFilterService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * 게시글 또는 댓글 본문의 욕설/부적절 단어 여부를 필터링 서버를 통해 검사합니다.
     *
     * @param title   제목 (댓글 등의 경우 빈 문자열 또는 null)
     * @param content 본문/댓글 내용
     * @return 필터링 결과 Map (blocked: boolean, reason: String 등 포함). 필터링이 비활성화된 경우 null 반환.
     * @throws IllegalStateException 외부 필터링 서버 통신에 실패한 경우 발생
     */
    public Map<String, Object> filterContent(String title, String content) {
        if (!contentFilterEnabled) {
            logger.debug("Content filtering is disabled. Skipping check.");
            return null;
        }

        Map<String, String> filterRequest = new HashMap<>();
        filterRequest.put("title", title != null ? title : "");
        filterRequest.put("content", content != null ? content : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(filterRequest, headers);

        try {
            logger.info("Sending content filter request to URL: {}", contentFilterUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(contentFilterUrl, entity, Map.class);
            Map body = response.getBody();
            return body == null ? null : new HashMap<String, Object>(body);
        } catch (RestClientException e) {
            logger.warn("Content filter request failed. url={}, error={}", contentFilterUrl, e.getMessage());
            throw new IllegalStateException("Content filter request failed", e);
        }
    }
}
