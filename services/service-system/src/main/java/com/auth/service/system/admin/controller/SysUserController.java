package com.auth.service.system.admin.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.service.SysUserProfileService;
import com.auth.service.system.admin.vo.SysUserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统用户接口
 *
 * @author Bunny
 */
@Tag(name = "系统用户", description = "用户档案")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class SysUserController {

    private final SysUserProfileService sysUserProfileService;

    /**
     * 查询用户档案（基本信息、部门、岗位）
     *
     * @param userId 用户ID
     * @return 聚合数据
     */
    @Operation(summary = "用户档案")
    @PreAuthorize("@auth.decide('sysUser:query')")
    @GetMapping("/{userId}/profile")
    public Result<SysUserProfileResponse> profile(@PathVariable("userId") Long userId) {
        return Result.success(sysUserProfileService.getProfile(userId));
    }

}
