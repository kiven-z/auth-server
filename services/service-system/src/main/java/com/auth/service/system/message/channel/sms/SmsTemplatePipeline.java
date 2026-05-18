package com.auth.service.system.message.channel.sms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.command.ChannelOptionsSupport;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.config.properties.SmsBlendProperties;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.SmsChannelOptions;
import com.auth.service.system.admin.mapper.SmsTemplateMapper;
import com.auth.service.system.admin.model.entity.SmsTemplateEntity;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplateRequireFieldRow;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.channel.sms.model.SmsPreparedMessage;
import com.auth.service.system.message.shared.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 短信模板加载、变量校验与 templateParam 组装
 *
 * @author Bunny
 */
@Component
public class SmsTemplatePipeline {

    private final SmsTemplateMapper smsTemplateMapper;

    private final SmsProperties smsProperties;

    public SmsTemplatePipeline(SmsTemplateMapper smsTemplateMapper, SmsProperties smsProperties) {
        this.smsTemplateMapper = smsTemplateMapper;
        this.smsProperties = smsProperties;
    }

    /**
     * 构建模板参数
     *
     * @param requireFields 必填字段
     * @param variables     变量
     * @return 模板参数
     */
    private static String buildTemplateParam(List<EmailTemplateRequireFieldRow> requireFields,
                                             Map<String, Object> variables) {
        // 构建模板参数
        Map<String, String> paramMap = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(requireFields)) {
            for (EmailTemplateRequireFieldRow field : requireFields) {
                Object value = variables.get(field.getKey());
                paramMap.put(field.getKey(), value == null ? "" : String.valueOf(value));
            }
        } else {
            variables.forEach((key, value) -> paramMap.put(key, value == null ? "" : String.valueOf(value)));
        }

        return JSONUtil.toJsonStr(paramMap);
    }

    /**
     * 准备短信发送参数
     *
     * @param command 模板发送命令
     * @return 厂商 API 就绪参数
     */
    public SmsPreparedMessage prepare(TemplateMessageCommand command) {
        // 获取模板代码
        String templateCode = command.getTemplateCode();
        // 查询短信模板
        SmsTemplateEntity template = smsTemplateMapper.selectByTemplateCode(templateCode);
        // 如果短信模板不存在，则抛出异常
        if (template == null) {
            throw new SystemBusinessException(SMS_TEMPLATE_NOT_FOUND, templateCode);
        }

        // 获取变量
        Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);
        // 获取必填字段
        List<EmailTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
                .parse(template.getRequireFields());
        // 校验变量是否存在
        MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables,
                SMS_TEMPLATE_VARS_MISSING);

        // 解析短信配置ID
        String smsConfigId = resolveSmsConfigId(template, command);
        // 解析签名
        String signName = resolveSignName(template, smsConfigId);
        // 构建模板参数
        String templateParam = buildTemplateParam(requireFields, variables);

        // 返回准备好的短信发送参数
        return new SmsPreparedMessage(template.getProviderTemplateCode(), signName, templateParam, smsConfigId);
    }

    /**
     * 解析短信配置ID
     *
     * @param template 短信模板
     * @param command  模板发送命令
     * @return 短信配置ID
     */
    private String resolveSmsConfigId(SmsTemplateEntity template, TemplateMessageCommand command) {
        // 获取短信配置ID
        SmsChannelOptions options = ChannelOptionsSupport.asSms(command.getOptions());
        // 如果短信配置ID不为空，则返回短信配置ID
        if (options != null && CharSequenceUtil.isNotBlank(options.getSmsConfigId())) {
            return options.getSmsConfigId();
        }

        // 如果短信模板配置ID不为空，则返回短信模板配置ID
        String smsConfigId = template.getSmsConfigId();
        if (CharSequenceUtil.isNotBlank(smsConfigId)) {
            return smsConfigId;
        }

        // 如果短信配置ID为空，则返回默认短信配置ID
        return smsProperties.getDefaultConfigId();
    }

    /**
     * 解析签名
     *
     * @param template    短信模板
     * @param smsConfigId 短信配置ID
     * @return 签名
     */
    private String resolveSignName(SmsTemplateEntity template, String smsConfigId) {
        if (CharSequenceUtil.isNotBlank(template.getSignName())) {
            return template.getSignName();
        }

        String configId = CharSequenceUtil.blankToDefault(smsConfigId, smsProperties.getDefaultConfigId());
        SmsBlendProperties blend = smsProperties.getBlends().get(configId);
        if (blend != null && CharSequenceUtil.isNotBlank(blend.getSignature())) {
            return blend.getSignature();
        }

        throw new SystemBusinessException(SMS_SIGN_NAME_NOT_CONFIGURED);
    }

}
