package com.saleshub.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saleshub.entity.SysDict;
import com.saleshub.mapper.SysDictMapper;
import com.saleshub.service.FeishuNotifyService;
import com.saleshub.service.RecordService;
import com.saleshub.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnfilledReminderScheduler {

    private final RecordService recordService;
    private final ServiceRecordService serviceRecordService;
    private final FeishuNotifyService feishuNotifyService;
    private final SysDictMapper dictMapper;
    private final ObjectMapper objectMapper;

    /** 每天上午 9:00 第一次提醒 */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Shanghai")
    public void firstReminder() {
        log.info("执行 9:00 未填报提醒");
        sendReminders("【首次提醒】");
    }

    /** 每天上午 10:00 第二次提醒 */
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Shanghai")
    public void secondReminder() {
        log.info("执行 10:00 未填报提醒");
        sendReminders("【二次提醒】");
    }

    private void sendReminders(String prefix) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 运营/合伙人业绩未填提醒
        List<String> recordWebhooks = getWebhookUrls("record_unfilled");
        if (!recordWebhooks.isEmpty()) {
            List<Map<String, Object>> unfilled = recordService.getUnfilledUsers(yesterday);
            List<String> names = unfilled.stream()
                .map(m -> (String) m.get("name"))
                .toList();
            feishuNotifyService.sendUnfilledReminder(
                recordWebhooks,
                prefix + " " + yesterday + " 运营业绩未填报提醒",
                names,
                "/data-entry"
            );
        }

        // 客服业绩未填提醒
        List<String> serviceWebhooks = getWebhookUrls("service_record_unfilled");
        if (!serviceWebhooks.isEmpty()) {
            List<Map<String, Object>> unfilled = serviceRecordService.getUnfilledUsers(yesterday);
            List<String> names = unfilled.stream()
                .map(m -> (String) m.get("name"))
                .toList();
            feishuNotifyService.sendUnfilledReminder(
                serviceWebhooks,
                prefix + " " + yesterday + " 客服业绩未填报提醒",
                names,
                "/service-entry"
            );
        }
    }

    private List<String> getWebhookUrls(String code) {
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "webhook")
                .eq(SysDict::getCode, code)
                .eq(SysDict::getStatus, "active")
        );
        if (dict == null || dict.getLabel() == null || dict.getLabel().isBlank()) {
            return Collections.emptyList();
        }
        String label = dict.getLabel().trim();
        if (label.startsWith("[")) {
            try {
                return objectMapper.readValue(label, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("解析 webhook urls 失败: {}", e.getMessage());
                return Collections.emptyList();
            }
        }
        // 兼容旧格式
        return List.of(label);
    }
}
