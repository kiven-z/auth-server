package com.auth.service.auth.support.integration;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.email.EmailSendDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 system 服务通用邮件发送接口
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemEmailSend", path = "/api/system/email")
public interface SystemMessageSendFeignClient {

    /**
     * 按模板发送邮件
     *
     * @param dto 发送请求
     * @return 统一结果
     */
    @PostMapping("/send")
    Result<String> sendEmail(@RequestBody EmailSendDTO dto);

}
