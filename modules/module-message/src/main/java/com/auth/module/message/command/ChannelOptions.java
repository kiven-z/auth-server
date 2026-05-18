package com.auth.module.message.command;

import com.auth.module.message.email.EmailChannelOptions;
import com.auth.module.message.sms.SmsChannelOptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * 渠道扩展参数
 *
 * @author Bunny
 */
@JsonSubTypes({ @JsonSubTypes.Type(value = EmailChannelOptions.class, name = EmailChannelOptions.TYPE),
		@JsonSubTypes.Type(value = SmsChannelOptions.class, name = SmsChannelOptions.TYPE) })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface ChannelOptions extends Serializable {

}
