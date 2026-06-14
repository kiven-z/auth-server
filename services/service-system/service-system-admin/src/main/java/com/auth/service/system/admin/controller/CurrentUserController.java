package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.ip.IpAddressService;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.user.SysUserChangePasswordForm;
import com.auth.service.system.admin.support.user.UserPasswordService;
import com.auth.service.system.common.model.SystemAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 当前登录用户自助接口
 *
 * @author Bunny
 */
@Tag(name = "当前用户", description = "登录用户自助操作")
@AuthenticatedApi
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class CurrentUserController {

	private final UserPasswordService userPasswordService;

	private final IpAddressService ipAddressService;

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "当前用户修改密码")
	@PutMapping("/me/password")
	public Result<String> changeOwnPassword(@Valid @RequestBody SysUserChangePasswordForm form,
			HttpServletRequest request) {
		String changeIp = ipAddressService.resolveIpInfo(request).getIpAddr();
		userPasswordService.changeOwnPassword(form, changeIp);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
