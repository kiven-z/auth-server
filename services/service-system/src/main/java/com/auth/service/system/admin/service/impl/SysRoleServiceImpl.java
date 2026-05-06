package com.auth.service.system.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.SysRoleCreateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.service.SysRoleService;
import com.auth.service.system.admin.support.SysRolePageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.feign.AuthenticationInnerFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统角色服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    private final AuthenticationInnerFeignClient authenticationInnerFeignClient;

    public SysRoleServiceImpl(AuthenticationInnerFeignClient authenticationInnerFeignClient) {
        this.authenticationInnerFeignClient = authenticationInnerFeignClient;
    }

    /**
     * 分页查询角色
     *
     * @param pageParams 分页
     * @param query      条件
     * @return 分页结果
     */
    @Override
    public PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query) {
        String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
        IPage<SysRolePageResponse> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
        return PageResponse.of(page);
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Override
    public SysRoleDetailVO getDetail(Long id) {
        SysRoleEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new SystemBusinessException(SystemResultCodeEnum.ROLE_NOT_FOUND, id);
        }

        SysRoleDetailVO vo = BeanUtil.copyProperties(entity, SysRoleDetailVO.class);
        vo.setRoleTypeDesc(RoleTypeEnum.getDesc(entity.getRoleType()));
        return vo;
    }

    /**
     * 新增角色
     *
     * @param form 新增表单
     */
    @Override
    public void create(SysRoleCreateForm form) {
        SysRoleEntity entity = BeanUtil.copyProperties(form, SysRoleEntity.class);
        save(entity);
    }

    /**
     * 查询角色下拉选项
     *
     * @param roleName 角色名称（可选模糊）
     * @param roleCode 角色编码（可选模糊）
     * @return 下拉选项列表
     */
    @Override
    public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
        return baseMapper.selectRoleOptions(roleName, roleCode);
    }
}
