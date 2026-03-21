package com.saleshub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FeishuNotifyService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送飞书群消息
     * @param webhookUrl 飞书机器人 webhook 地址
     * @param title 消息标题
     * @param names 未填报人员名单
     */
    public void sendUnfilledReminder(String webhookUrl, String title, List<String> names) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;
        if (names == null || names.isEmpty()) return;

        try {
            String nameList = String.join("、", names);
            String content = title + "\n\n未填报人员（共" + names.size() + "人）：\n" + nameList + "\n\n请尽快填报，谢谢！";

            Map<String, Object> body = Map.of(
                "msg_type", "text",
                "content", Map.of("text", content)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("飞书提醒发送成功: {} 人未填报", names.size());
        } catch (Exception e) {
            log.error("飞书提醒发送失败: {}", e.getMessage());
        }
    }
}
