package com.auth.module.message.api.command;

import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * 渠道扩展参数
 *
 * @author Bunny
 */
@JsonSubTypes({ @JsonSubTypes.Type(value = EmailChannelOptions.class, name = EmailChannelOptions.TYPE),
		@JsonSubTypes.Type(value = DingTalkChannelOptions.class, name = DingTalkChannelOptions.TYPE) })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface ChannelOptions extends Serializable {

}
