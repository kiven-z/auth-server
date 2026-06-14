package com.auth.service.auth.session.domain.model;

import com.auth.module.security.contract.api.UserSessionIndex;

import java.util.List;

/**
 * 全局在线会话分页切片（Repository 与应用层之间的传输对象）
 *
 * @param total 符合条件的会话总数
 * @param sessions 当前页会话列表
 * @author Bunny
 */
public record OnlineSessionPageSlice(long total, List<UserSessionIndex> sessions) {

}
