package com.auth.service.auth.authorization.domain.datascope;

/**
 * 数据权限授权片段（领域读模型）：与持久层查询列语义对齐，供 {@link DataScopeBuilder} 做生效合并。
 *
 * @param subjectType 授权主体类型，如 USER / ROLE
 * @param scopeResourceCode 数据范围资源键
 * @param dimensionType 数据维度类型，如 DEPT / POST
 * @param scopeType 范围类型（库内存量字符串）
 * @param scopeSubjectIds scope_subject_ids 的 JSON 或等价序列化字符串
 * @author Bunny
 */
public record DataScopeGrantPiece(String subjectType, String scopeResourceCode, String dimensionType, String scopeType,
		String scopeSubjectIds) {
}
