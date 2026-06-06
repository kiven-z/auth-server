package com.auth.service.system.authorization.dispatch;

/**
 * 事务提交后触发 auth 同步投递的事件载体。
 *
 * @param outboxId Outbox 主键
 * @author Bunny
 */
public record AuthorizationInvalidationDispatchEvent(Long outboxId) {

}
