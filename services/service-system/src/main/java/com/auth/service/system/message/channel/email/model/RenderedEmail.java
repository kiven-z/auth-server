package com.auth.service.system.message.channel.email.model;

/**
 * 邮件模板渲染结果
 *
 * @param subject 渲染后的主题
 * @param body 渲染后的正文
 * @author Bunny
 */
public record RenderedEmail(String subject, String body) {
}
