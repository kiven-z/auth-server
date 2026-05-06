package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.SysRoleCreateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.SysRolePageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 系统角色服务
 *
 * @author Bunny
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     *
     * @param pageParams 分页参数
     * @param query      查询条件
     * @return 分页数据
     */
    PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    SysRoleDetailVO getDetail(Long id);

    /**
     * 新增角色
     *
     * @param form 新增表单
     */
    void create(SysRoleCreateForm form);

    /**
     * 查询角色下拉选项
     *
     * @param roleName 角色名称（可选模糊）
     * @param roleCode 角色编码（可选模糊）
     * @return 下拉选项列表
     */
    List<SysRoleOptionVO> listOptions(String roleName, String roleCode);

}
