package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.DeptReferenceMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 部门写操作前的存在性与引用校验。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptReferenceChecker {

    /**
     * 顶级部门的 parent_id
     */
    public static final long ROOT_PARENT_ID = 0L;

    private final SysDeptMapper sysDeptMapper;

    private final DeptReferenceMapper deptReferenceMapper;

    /**
     * 加载未删除部门，不存在则抛业务异常。
     *
     * @param deptId 部门 ID
     */
    public void requireActiveDept(Long deptId) {
        if (sysDeptMapper.countActiveById(deptId) == 0) {
            throw new SystemBusinessException(SystemAdminResultCode.DEPT_NOT_FOUND, deptId);
        }
    }

    /**
     * 父部门须存在且未删除；顶级父 ID 为 0 时跳过。
     *
     * @param parentId 父部门 ID
     */
    public void requireActiveParent(Long parentId) {
        if (parentId == null || parentId <= ROOT_PARENT_ID) {
            return;
        }
        if (sysDeptMapper.countActiveById(parentId) == 0) {
            throw new SystemBusinessException(SystemAdminResultCode.DEPT_PARENT_NOT_FOUND, parentId);
        }
    }

    /**
     * 移动目标不能是自身或自身后代。
     *
     * @param deptId      被移动部门
     * @param newParentId 新父部门
     */
    public void requireMoveTargetValid(Long deptId, Long newParentId) {
        if (newParentId != null && newParentId.equals(deptId)) {
            throw new SystemBusinessException(SystemAdminResultCode.DEPT_MOVE_TO_SELF, deptId);
        }
        if (newParentId != null && newParentId > ROOT_PARENT_ID
                && deptReferenceMapper.countDescendantRelation(deptId, newParentId) > 0) {
            throw new SystemBusinessException(SystemAdminResultCode.DEPT_MOVE_TO_DESCENDANT, deptId, newParentId);
        }
    }

    /**
     * 逻辑删除前：无未删除的直接子部门。
     *
     * @param deptId 部门 ID
     */
    public void requireNoActiveChildren(Long deptId) {
        if (sysDeptMapper.countActiveDirectChildren(deptId) > 0) {
            throw new SystemBusinessException(SystemAdminResultCode.DEPT_HAS_CHILDREN, deptId);
        }
    }

    /**
     * 逻辑删除前：子树内无用户、授权、岗位引用。
     *
     * @param deptId 部门 ID
     */
    public void requireSubtreeUnused(Long deptId) {
        if (deptReferenceMapper.countUserDeptInSubtree(deptId) > 0
                || deptReferenceMapper.countGrantDeptSubjectInSubtree(deptId) > 0
                || deptReferenceMapper.countPostInSubtree(deptId) > 0) {
            throw new SystemBusinessException(SystemAdminResultCode.DEPT_IN_USE, deptId);
        }
    }

}
