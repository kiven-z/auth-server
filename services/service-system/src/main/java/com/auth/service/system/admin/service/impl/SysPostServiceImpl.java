package com.auth.service.system.admin.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysPostConverter;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostCreateForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.admin.service.SysPostService;
import com.auth.service.system.admin.support.SysPostPageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统岗位服务实现
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {

    private final AuditUserDisplayService auditUserDisplayService;

    public SysPostServiceImpl(AuditUserDisplayService auditUserDisplayService) {
        this.auditUserDisplayService = auditUserDisplayService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SysPostPageResponse> getPage(Page<SysPostEntity> pageParams, SysPostQuery query) {
        String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
        IPage<SysPostPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
        IPage<SysPostPageResponse> voPage = page.convert(SysPostConverter.INSTANCE::toPageResponse);
        auditUserDisplayService.enrichAuditUsernames(voPage);
        return PageResponse.of(voPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(SysPostCreateForm form) {
        if (baseMapper.countAssignableDept(form.getDeptId()) == 0) {
            throw new SystemBusinessException(SystemResultCodeEnum.POST_DEPT_NOT_FOUND, form.getDeptId());
        }

        String postCode = CharSequenceUtil.trim(form.getPostCode());
        long exists = count(Wrappers.<SysPostEntity>lambdaQuery()
                .eq(SysPostEntity::getDeptId, form.getDeptId())
                .eq(SysPostEntity::getPostCode, postCode));
        if (exists > 0) {
            throw new SystemBusinessException(SystemResultCodeEnum.POST_CODE_DUPLICATE_IN_DEPT, form.getDeptId(),
                    postCode);
        }

        SysPostEntity entity = SysPostConverter.INSTANCE.toEntity(form);
        entity.setPostCode(postCode);
        if (entity.getOrderNum() == null) {
            entity.setOrderNum(0);
        }

        save(entity);
    }

}
