package com.auth.service.auth.session.domain.model;

/**
 * 在线会话列表所需的用户展示快照
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param nickname 昵称
 * @author Bunny
 */
public record UserOnlineBrief(Long id, String username, String nickname) {

}
