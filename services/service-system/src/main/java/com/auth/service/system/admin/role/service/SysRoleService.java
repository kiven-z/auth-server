package com.auth.service.system.admin.role.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.form.SysRoleCreateForm;
import com.auth.service.system.admin.role.model.form.SysRoleUpdateForm;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRoleDetailVO;
import com.auth.service.system.admin.role.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
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
     * 修改角色
     *
     * @param form 修改表单
     */
    void update(SysRoleUpdateForm form);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 切换角色状态
     *
     * @param id     角色ID
     * @param status 目标状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 查询角色下拉选项
     *
     * @param roleName 角色名称（可选模糊）
     * @param roleCode 角色编码（可选模糊）
     * @return 下拉选项列表
     */
    List<SysRoleOptionVO> listOptions(String roleName, String roleCode);

}
