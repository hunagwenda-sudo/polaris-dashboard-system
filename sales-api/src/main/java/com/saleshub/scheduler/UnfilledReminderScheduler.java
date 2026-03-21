package com.saleshub.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        String recordWebhook = getWebhookUrl("record_unfilled");
        if (recordWebhook != null && !recordWebhook.isBlank()) {
            List<Map<String, Object>> unfilled = recordService.getUnfilledUsers(yesterday);
            List<String> names = unfilled.stream()
                .map(m -> (String) m.get("name"))
                .toList();
            feishuNotifyService.sendUnfilledReminder(
                recordWebhook,
                prefix + " " + yesterday + " 运营业绩未填报提醒",
                names
            );
        }

        // 客服业绩未填提醒
        String serviceWebhook = getWebhookUrl("service_record_unfilled");
        if (serviceWebhook != null && !serviceWebhook.isBlank()) {
            List<Map<String, Object>> unfilled = serviceRecordService.getUnfilledUsers(yesterday);
            List<String> names = unfilled.stream()
                .map(m -> (String) m.get("name"))
                .toList();
            feishuNotifyService.sendUnfilledReminder(
                serviceWebhook,
                prefix + " " + yesterday + " 客服业绩未填报提醒",
                names
            );
        }
    }

    private String getWebhookUrl(String code) {
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "webhook")
                .eq(SysDict::getCode, code)
                .eq(SysDict::getStatus, "active")
        );
        return dict != null ? dict.getLabel() : null;
    }
}
