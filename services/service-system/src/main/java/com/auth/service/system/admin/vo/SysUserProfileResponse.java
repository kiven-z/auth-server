package com.auth.service.system.admin.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户档案聚合（基本信息 + 部门 + 岗位）
 *
 * @author Bunny
 */
@Schema(name = "SysUserProfileResponse", title = "用户档案")
@Data
public class SysUserProfileResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonStringFormat
    @Schema(title = "用户ID")
    private Long id;

    @Schema(title = "登录账号")
    private String username;

    @Schema(title = "昵称")
    private String nickname;

    @Schema(title = "邮箱")
    private String email;

    @Schema(title = "手机号")
    private String phone;

    @Schema(title = "头像URL")
    private String avatar;

    @Schema(title = "状态(0=正常,1=禁用,2=锁定)")
    private Integer status;

    @Schema(title = "性别(0=未知,1=男,2=女)")
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(title = "生日")
    private LocalDate birthday;

    @Schema(title = "个人简介")
    private String introduction;

    @Schema(title = "最后登录IP")
    private String lastLoginIp;

    @Schema(title = "最后登录地区")
    private String lastLoginRegion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(title = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(title = "所属部门列表")
    private List<SysUserDeptProfileItemResponse> deptList = new ArrayList<>();

    @Schema(title = "岗位列表")
    private List<SysUserPostProfileItemResponse> postList = new ArrayList<>();

}
