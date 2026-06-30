package com.auth.service.system.admin.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.system.admin.model.form.me.MeUserPreferenceUpsertForm;
import com.auth.service.system.admin.model.vo.me.MeUserPreferenceListVO;
import com.auth.service.system.admin.service.me.CurrentUserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户 UI 偏好配置接口
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "当前用户偏好", description = "登录用户 UI 偏好配置")
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/system/me")
@RestController
public class CurrentUserPreferenceController {

	private final CurrentUserPreferenceService currentUserPreferenceService;

	@Operation(summary = "查询当前用户 UI 偏好配置")
	@GetMapping("/preferences")
	public Result<MeUserPreferenceListVO> listMyPreferences() {
		MeUserPreferenceListVO data = currentUserPreferenceService.listMyPreferences();
		return Result.success(data);
	}

	@Operation(summary = "更新当前用户 UI 偏好配置项")
	@PutMapping("/preferences")
	public Result<Void> upsertMyPreference(@Valid @RequestBody MeUserPreferenceUpsertForm form) {
		currentUserPreferenceService.upsertMyPreference(form);
		return Result.success();
	}

	@Operation(summary = "清空当前用户 UI 偏好配置")
	@DeleteMapping("/preferences")
	public Result<Void> clearMyPreferences() {
		currentUserPreferenceService.clearMyPreferences();
		return Result.success();
	}

}
