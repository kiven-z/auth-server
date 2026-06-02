package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.form.dept.SysDeptUpdateForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundPostPO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.admin.support.dept.DeptReferenceChecker.ROOT_PARENT_ID;

/**
 * 部门服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptService {

    private final AuditUserDisplayService auditUserDisplayService;

    private final DeptReferenceChecker deptReferenceChecker;

    private final DeptClosureMaintainer deptClosureMaintainer;

    private final DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

    private final SysDeptDetailMapper sysDeptDetailMapper;

    /**
     * 解析可比较的父部门 ID（null 视为顶级）。
     *
     * @param entity 部门实体
     * @return 父部门 ID，顶级为 {@link DeptReferenceChecker#ROOT_PARENT_ID}
     */
    private static long resolveParentId(SysDeptEntity entity) {
        return entity.getParentId() != null ? entity.getParentId() : ROOT_PARENT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SysDeptListVO> listFlat(SysDeptQuery query) {
        List<SysDeptEntity> entities = baseMapper.selectListByQuery(query);
        List<SysDeptListVO> list = SysDeptConverter.INSTANCE.toListVoList(entities);

        // 补充审计用户名
        auditUserDisplayService.enrichAuditUsernames(list);
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SysDeptListVO> pageFlat(Page<SysDeptEntity> pageParams, SysDeptQuery query) {
        IPage<SysDeptEntity> entityPage = baseMapper.selectPageByQuery(pageParams, query);
        IPage<SysDeptListVO> result = entityPage.convert(SysDeptConverter.INSTANCE::toListVo);

        // 补充审计用户名
        auditUserDisplayService.enrichAuditUsernames(result);
        return PageResponse.of(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SysDeptDetailVO getDetail(Long id) {
        SysDeptEntity entity = deptReferenceChecker.getExistingActive(id);

        SysDeptDetailVO detail = SysDeptConverter.INSTANCE.toDetailVo(entity);
        List<SysDeptBoundUserPO> boundUsers = sysDeptDetailMapper.selectBoundUsersByDeptId(id);
        detail.setBoundUsers(SysDeptConverter.INSTANCE.toBoundUserItemList(boundUsers));

        List<SysDeptBoundPostPO> boundPosts = sysDeptDetailMapper.selectBoundPostsByDeptId(id);
        detail.setBoundPosts(SysDeptConverter.INSTANCE.toBoundPostItemList(boundPosts));

        List<SysDeptBoundRolePO> boundRoles = sysDeptDetailMapper.selectBoundRolesByDeptId(id);
        detail.setBoundRoles(SysDeptConverter.INSTANCE.toBoundRoleItemList(boundRoles));

        // 补充审计用户名
        auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail));
        return detail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(SysDeptCreateForm form) {
        // 规范化父部门 ID 并校验父部门存在（顶级跳过校验）
        long parentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());

        // 校验部门编码唯一性
        String deptCode = form.getDeptCode();
        BusinessKeyAssert.requireAbsent(this,
                Wrappers.<SysDeptEntity>lambdaQuery().eq(SysDeptEntity::getDeptCode, deptCode),
                SystemAdminResultCode.DEPT_CODE_DUPLICATE, deptCode);

        SysDeptEntity entity = SysDeptConverter.INSTANCE.toEntity(form);
        entity.setDeptCode(deptCode);
        entity.setParentId(parentId);

        save(entity);

        // 维护部门闭包
        deptClosureMaintainer.insertNode(entity.getId(), parentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMeta(SysDeptUpdateForm form) {
        Long id = form.getId();
        SysDeptEntity existing = deptReferenceChecker.getExistingActive(id);

        String newDeptCode = form.getDeptCode();
        boolean codeChanged = FieldChangeSupport.codeChanged(existing.getDeptCode(), newDeptCode);
        if (codeChanged) {
            BusinessKeyAssert.requireAbsent(this,
                    Wrappers.<SysDeptEntity>lambdaQuery()
                            .eq(SysDeptEntity::getDeptCode, newDeptCode)
                            .ne(SysDeptEntity::getId, id),
                    SystemAdminResultCode.DEPT_CODE_DUPLICATE, newDeptCode);
        }

        long newParentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());
        if (resolveParentId(existing) != newParentId) {
            doMove(existing, newParentId);
        }

        SysDeptConverter.INSTANCE.applyUpdateForm(form, existing);
        existing.setDeptCode(newDeptCode);

        // 触发部门授权失效
        deptInvalidationTrigger.submitByDeptIds(List.of(id), "update:" + id);

        updateById(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Long deptId, SysDeptMoveForm form) {
        // 校验部门是否存在
        SysDeptEntity existing = deptReferenceChecker.getExistingActive(deptId);
        long newParentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());

        if (resolveParentId(existing) == newParentId) {
            return;
        }

        doMove(existing, newParentId);
    }

    /**
     * 变更父部门：落库 parent_id、维护闭包并触发移动类授权失效。
     *
     * @param existing    已加载的部门实体（将被写入新 parentId）
     * @param newParentId 规范化后的新父部门 ID
     */
    private void doMove(SysDeptEntity existing, long newParentId) {
        Long deptId = existing.getId();
        deptReferenceChecker.requireMoveTargetValid(deptId, newParentId);
        existing.setParentId(newParentId);
        updateById(existing);

        deptClosureMaintainer.moveNode(deptId, newParentId);
        deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "move:" + deptId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(Long deptId) {
        // 校验部门是否存在
        deptReferenceChecker.getExistingActive(deptId);
        // 校验部门是否有子部门
        deptReferenceChecker.requireNoActiveChildren(deptId);
        // 校验部门是否有子部门
        deptReferenceChecker.requireSubtreeUnused(deptId);

        // 先提交失效（反查依赖 dept_closure）；再清理闭包与主表
        deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "delete:" + deptId);

        // 维护部门闭包
        deptClosureMaintainer.removeClosurePaths(deptId);

        removeById(deptId);
    }

}
