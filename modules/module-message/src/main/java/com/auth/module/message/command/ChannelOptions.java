package com.auth.module.message.command;

import com.auth.module.message.modules.email.EmailChannelOptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * 渠道扩展参数
 *
 * @author Bunny
 */
@JsonSubTypes({ @JsonSubTypes.Type(value = EmailChannelOptions.class, name = EmailChannelOptions.TYPE) })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface ChannelOptions extends Serializable {

}
