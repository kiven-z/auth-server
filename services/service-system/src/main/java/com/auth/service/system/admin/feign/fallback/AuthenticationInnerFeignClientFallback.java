package com.auth.service.system.admin.feign.fallback;

import java.util.List;

import org.springframework.stereotype.Component;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.feign.AuthenticationInnerFeignClient;
import com.auth.service.system.admin.feign.dto.EffectiveCodesVO;
import com.auth.service.system.admin.feign.dto.GrantSubjectInnerRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Bunny
 */
@Slf4j
@Component
public class AuthenticationInnerFeignClientFallback implements AuthenticationInnerFeignClient {

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 * @return 刷新结果
	 */
	@Override
	public Result<String> refreshUserCache(List<Long> userIds) {
		log.error("AuthenticationInnerFeignClientFallback-刷新用户缓存 失败，用户IDs：{}", userIds);

		// 方案A：返回失败结果，让调用方处理
		return Result.error("刷新缓存服务暂不可用");
	}

	@Override
	public Result<EffectiveCodesVO> getEffectiveCodes(Long userId) {
		log.error("AuthInnerPrincipalFeignClientFallback: getEffectiveCodes failed, userId={}", userId);
		return Result.success(new EffectiveCodesVO());
	}

	/**
	 * 查询持有指定角色的所有用户 ID（熔断降级，返回空列表）
	 * @param roleCode 角色编码
	 * @return 空列表
	 */
	@Override
	public Result<List<Long>> listUserIdsByRoleCode(String roleCode) {
		log.error("AuthenticationInnerFeignClientFallback-查询角色用户ID 失败，roleCode：{}", roleCode);
		return Result.success(List.of());
	}

	@Override
	public Result<List<Long>> listUserIdsByRoleCodes(List<String> roleCodes) {
		log.error("AuthenticationInnerFeignClientFallback-listUserIdsByRoleCodes 失败，size={}",
				roleCodes == null ? null : roleCodes.size());
		return Result.success(List.of());
	}

	@Override
	public Result<List<Long>> listUserIdsByPermissionCodes(List<String> permissionCodes) {
		log.error("AuthenticationInnerFeignClientFallback-listUserIdsByPermissionCodes 失败");
		return Result.success(List.of());
	}

	@Override
	public Result<List<Long>> listUserIdsByPolicyCodes(List<String> policyCodes) {
		log.error("AuthenticationInnerFeignClientFallback-listUserIdsByPolicyCodes 失败");
		return Result.success(List.of());
	}

	@Override
	public Result<List<Long>> listUserIdsByDeptIds(List<Long> deptIds) {
		log.error("AuthenticationInnerFeignClientFallback-listUserIdsByDeptIds 失败");
		return Result.success(List.of());
	}

	@Override
	public Result<List<Long>> listUserIdsByPostIds(List<Long> postIds) {
		log.error("AuthenticationInnerFeignClientFallback-listUserIdsByPostIds 失败");
		return Result.success(List.of());
	}

	@Override
	public Result<List<Long>> listUserIdsByGrantSubjects(List<GrantSubjectInnerRequest> subjects) {
		log.error("AuthenticationInnerFeignClientFallback-listUserIdsByGrantSubjects 失败");
		return Result.success(List.of());
	}

}
