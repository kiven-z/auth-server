package com.auth.service.system.message.api;

import com.auth.service.system.message.channel.dingtalk.model.DingTalkWorkNoticeSendForm;

/**
 * 钉钉工作通知发送服务（无模板，直接发送正文）
 *
 * @author Bunny
 */
public interface DingTalkWorkNoticeService {

    /**
     * 发送工作通知
     *
     * @param form 发送参数
     */
    void send(DingTalkWorkNoticeSendForm form);

}
