package com.auth.service.system.admin.role.service.impl;

import com.auth.common.core.model.query.SortDirection;
import com.auth.common.core.model.query.SortSpec;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.feign.AuthenticationInnerFeignClient;
import com.auth.service.system.admin.role.mapper.SysRoleMapper;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.form.SysRoleCreateForm;
import com.auth.service.system.admin.role.model.form.SysRoleUpdateForm;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRoleDetailVO;
import com.auth.service.system.admin.role.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.role.support.SysRolePageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色服务单元测试
 *
 * @author Bunny
 */
@SuppressWarnings("unchecked")
@DisplayName("SysRoleServiceImpl")
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private AuthenticationInnerFeignClient authInnerFeignClient;

    @Mock
    private UserMapper userMapper;

    private SysRoleServiceImpl sysRoleService;

    @BeforeEach
    void setUp() {
        sysRoleService = new SysRoleServiceImpl(authInnerFeignClient);
        ReflectionTestUtils.setField(sysRoleService, "baseMapper", sysRoleMapper);
    }

    // ==================== 分页 ====================

    @Test
    @DisplayName("sort 为空时 ORDER BY 使用默认片段")
    void getPageUsesDefaultOrderWhenSortEmpty() {
        ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
        when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
                .thenAnswer(invocation -> {
                    Page<SysRoleEntity> page = invocation.getArgument(0);
                    page.setTotal(0L);
                    page.setRecords(Collections.emptyList());
                    return page;
                });

        SysRoleQuery query = new SysRoleQuery();
        query.setSort(null);

        PageResponse<SysRolePageResponse> page = sysRoleService.getPage(new Page<>(1, 10), query);

        assertThat(page.getList()).isEmpty();
        assertThat(orderCaptor.getValue()).isEqualTo(SysRolePageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
    }

    @Test
    @DisplayName("非法排序字段被忽略后回退默认 ORDER BY")
    void getPageFallsBackWhenOnlyInvalidSortFields() {
        when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), anyString()))
                .thenAnswer(invocation -> {
                    Page<SysRoleEntity> page = invocation.getArgument(0);
                    page.setTotal(0L);
                    page.setRecords(Collections.emptyList());
                    return page;
                });

        SysRoleQuery query = new SysRoleQuery();
        SortSpec bad = new SortSpec();
        bad.setField("dropTable");
        bad.setDirection(SortDirection.ASC);
        query.setSort(List.of(bad));

        sysRoleService.getPage(new Page<>(1, 10), query);
    }

    @Test
    @DisplayName("白名单字段 orderNum ASC 生成对应列排序")
    void getPageMapsWhitelistFieldToColumn() {
        ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
        when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
                .thenAnswer(invocation -> {
                    Page<SysRoleEntity> page = invocation.getArgument(0);
                    page.setTotal(0L);
                    page.setRecords(Collections.emptyList());
                    return page;
                });

        SysRoleQuery query = new SysRoleQuery();
        SortSpec spec = new SortSpec();
        spec.setField("orderNum");
        spec.setDirection(SortDirection.ASC);
        query.setSort(List.of(spec));

        sysRoleService.getPage(new Page<>(1, 10), query);

        assertThat(orderCaptor.getValue()).isEqualTo("r.order_num ASC");
    }

    @Test
    @DisplayName("多字段排序按传入顺序拼接")
    void getPageJoinsMultipleSortSegments() {
        ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
        when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
                .thenAnswer(invocation -> {
                    Page<SysRoleEntity> page = invocation.getArgument(0);
                    page.setTotal(0L);
                    page.setRecords(Collections.emptyList());
                    return page;
                });

        SysRoleQuery query = new SysRoleQuery();
        SortSpec first = new SortSpec();
        first.setField("orderNum");
        first.setDirection(SortDirection.ASC);
        SortSpec second = new SortSpec();
        second.setField("createTime");
        second.setDirection(SortDirection.DESC);
        query.setSort(List.of(first, second));

        sysRoleService.getPage(new Page<>(1, 10), query);

        assertThat(orderCaptor.getValue()).isEqualTo("r.order_num ASC, r.create_time DESC");
    }

    @Test
    @DisplayName("查询条件传入后原样交给 Mapper")
    void getPagePassesQueryToMapper() {
        when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), anyString()))
                .thenAnswer(invocation -> {
                    Page<SysRoleEntity> page = invocation.getArgument(0);
                    page.setTotal(0L);
                    page.setRecords(Collections.emptyList());
                    return page;
                });

        SysRoleQuery query = new SysRoleQuery();
        query.setRoleCode("ADM");
        query.setRoleName("管理");
        query.setStatus(0);
        query.setRoleType(1);

        sysRoleService.getPage(new Page<>(1, 20), query);

        verify(sysRoleMapper).selectListByPage(any(Page.class), eq(query), anyString());
    }

    // ==================== 详情 ====================

    @Test
    @DisplayName("getDetail 返回正常数据")
    void getDetailSuccess() {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(1L);
        entity.setRoleCode("ADMIN");
        entity.setRoleName("管理员");
        entity.setStatus(0);
        entity.setRoleType(1);
        entity.setOrderNum(1);

        when(sysRoleMapper.selectById(1L)).thenReturn(entity);

        SysRoleDetailVO vo = sysRoleService.getDetail(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getRoleCode()).isEqualTo("ADMIN");
        assertThat(vo.getRoleTypeDesc()).isEqualTo("系统角色");
    }

    @Test
    @DisplayName("getDetail 角色不存在抛异常")
    void getDetailNotFound() {
        when(sysRoleMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> sysRoleService.getDetail(999L))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_NOT_FOUND);
    }

    // ==================== 新增 ====================

    @Test
    @DisplayName("create 正常新增")
    void createSuccess() {
        SysRoleCreateForm form = new SysRoleCreateForm();
        form.setRoleCode("NEW_ROLE");
        form.setRoleName("新角色");
        form.setStatus(0);
        form.setRoleType(2);
        form.setOrderNum(10);

        when(sysRoleMapper.insert(any(SysRoleEntity.class))).thenReturn(1);

        sysRoleService.create(form);

        ArgumentCaptor<SysRoleEntity> captor = ArgumentCaptor.forClass(SysRoleEntity.class);
        verify(sysRoleMapper).insert(captor.capture());

        SysRoleEntity entity = captor.getValue();
        assertThat(entity.getRoleCode()).isEqualTo("NEW_ROLE");
        assertThat(entity.getRoleName()).isEqualTo("新角色");
        assertThat(entity.getStatus()).isZero();
        assertThat(entity.getRoleType()).isEqualTo(2);
        assertThat(entity.getOrderNum()).isEqualTo(10);
    }

    // ==================== 修改（不改 code） ====================

    @Test
    @DisplayName("update 不改 role_code 时正常更新并刷新缓存")
    void updateWithoutCodeChange() {
        SysRoleEntity oldEntity = new SysRoleEntity();
        oldEntity.setId(1L);
        oldEntity.setRoleCode("ADMIN");
        oldEntity.setRoleName("管理员");
        oldEntity.setRoleType(2);
        oldEntity.setStatus(0);

        when(sysRoleMapper.selectById(1L)).thenReturn(oldEntity);
        when(authInnerFeignClient.listUserIdsByRoleCode("ADMIN"))
                .thenReturn(Result.success(List.of(101L, 102L)));
        when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

        SysRoleUpdateForm form = new SysRoleUpdateForm();
        form.setId(1L);
        form.setRoleCode("ADMIN");
        form.setRoleName("管理员-v2");
        form.setRoleType(2);
        form.setStatus(0);

        sysRoleService.update(form);

        // 验证 perm_version +1
        verify(sysRoleMapper).batchIncrementPermVersion(List.of(101L, 102L));
        // 验证刷新缓存
        verify(authInnerFeignClient).refreshUserCache(List.of(101L, 102L));
    }

    // ==================== 修改（改 code） ====================

    @Test
    @DisplayName("update 改 role_code 时级联更新并刷新新旧用户")
    void updateWithCodeChange() {
        SysRoleEntity oldEntity = new SysRoleEntity();
        oldEntity.setId(1L);
        oldEntity.setRoleCode("OLD_CODE");
        oldEntity.setRoleName("旧角色");
        oldEntity.setRoleType(2);
        oldEntity.setStatus(0);

        when(sysRoleMapper.selectById(1L)).thenReturn(oldEntity);
        when(authInnerFeignClient.listUserIdsByRoleCode("OLD_CODE"))
                .thenReturn(Result.success(List.of(101L)));
        when(authInnerFeignClient.listUserIdsByRoleCode("NEW_CODE"))
                .thenReturn(Result.success(List.of(102L)));

        SysRoleUpdateForm form = new SysRoleUpdateForm();
        form.setId(1L);
        form.setRoleCode("NEW_CODE");
        form.setRoleName("新角色");
        form.setRoleType(2);
        form.setStatus(0);

        sysRoleService.update(form);

        // 验证级联更新
        verify(sysRoleMapper).cascadeUpdateRoleCode("OLD_CODE", "NEW_CODE");
        // 验证 perm_version +1（新旧去重后）
        verify(sysRoleMapper).batchIncrementPermVersion(argThat(ids -> ids.containsAll(List.of(101L, 102L))));
        // 验证 Feign 刷新
        verify(authInnerFeignClient).refreshUserCache(argThat(ids -> ids.containsAll(List.of(101L, 102L))));
    }

    @Test
    @DisplayName("update 系统角色禁止修改 role_type")
    void updateSystemRoleCannotChangeType() {
        SysRoleEntity oldEntity = new SysRoleEntity();
        oldEntity.setId(1L);
        oldEntity.setRoleCode("SYSTEM");
        oldEntity.setRoleType(1);

        when(sysRoleMapper.selectById(1L)).thenReturn(oldEntity);

        SysRoleUpdateForm form = new SysRoleUpdateForm();
        form.setId(1L);
        form.setRoleCode("SYSTEM");
        form.setRoleType(2);
        form.setStatus(0);

        assertThatThrownBy(() -> sysRoleService.update(form))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_CHANGE_TYPE);
    }

    // ==================== 删除（单条） ====================

    @Test
    @DisplayName("deleteById 正常删除并刷新缓存")
    void deleteByIdSuccess() {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(1L);
        entity.setRoleCode("NORMAL_ROLE");
        entity.setRoleType(2);

        when(sysRoleMapper.selectById(1L)).thenReturn(entity);
        when(sysRoleMapper.countGrantTableRefByRoleCode("NORMAL_ROLE")).thenReturn(0);
        when(authInnerFeignClient.listUserIdsByRoleCode("NORMAL_ROLE"))
                .thenReturn(Result.success(List.of(101L)));

        sysRoleService.deleteById(1L);

        verify(sysRoleMapper).logicDeleteById(1L);
        verify(sysRoleMapper).batchIncrementPermVersion(List.of(101L));
        verify(authInnerFeignClient).refreshUserCache(List.of(101L));
    }

    @Test
    @DisplayName("deleteById 系统角色不可删除")
    void deleteByIdSystemRole() {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(1L);
        entity.setRoleCode("SYSTEM");
        entity.setRoleType(1);

        when(sysRoleMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> sysRoleService.deleteById(1L))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_DELETE);
    }

    @Test
    @DisplayName("deleteById 引用中的角色不可删除")
    void deleteByIdRoleInUse() {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(1L);
        entity.setRoleCode("IN_USE_ROLE");
        entity.setRoleType(2);

        when(sysRoleMapper.selectById(1L)).thenReturn(entity);
        when(sysRoleMapper.countGrantTableRefByRoleCode("IN_USE_ROLE")).thenReturn(5);

        assertThatThrownBy(() -> sysRoleService.deleteById(1L))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_IN_USE);
    }

    @Test
    @DisplayName("deleteById 角色不存在抛异常")
    void deleteByIdNotFound() {
        when(sysRoleMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> sysRoleService.deleteById(999L))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_NOT_FOUND);
    }

    // ==================== 状态切换 ====================

    @Test
    @DisplayName("updateStatus 正常切换并刷新缓存")
    void updateStatusSuccess() {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(1L);
        entity.setRoleCode("NORMAL");
        entity.setRoleType(2);

        when(sysRoleMapper.selectById(1L)).thenReturn(entity);
        when(authInnerFeignClient.listUserIdsByRoleCode("NORMAL"))
                .thenReturn(Result.success(List.of(101L)));

        sysRoleService.updateStatus(1L, 1);

        verify(sysRoleMapper).updateStatusById(1L, 1);
        verify(sysRoleMapper).batchIncrementPermVersion(List.of(101L));
        verify(authInnerFeignClient).refreshUserCache(List.of(101L));
    }

    @Test
    @DisplayName("updateStatus 系统角色不可禁用")
    void updateStatusSystemRoleCannotDisable() {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(1L);
        entity.setRoleCode("SYSTEM");
        entity.setRoleType(1);

        when(sysRoleMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> sysRoleService.updateStatus(1L, 1))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_SYSTEM_CANNOT_DISABLE);
    }

    @Test
    @DisplayName("updateStatus 角色不存在抛异常")
    void updateStatusNotFound() {
        when(sysRoleMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> sysRoleService.updateStatus(999L, 0))
                .isInstanceOf(SystemBusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", SystemResultCodeEnum.ROLE_NOT_FOUND);
    }

    // ==================== 下拉选项 ====================

    @Test
    @DisplayName("listOptions 返回正常数据")
    void listOptionsSuccess() {
        SysRoleEntity e1 = new SysRoleEntity();
        e1.setId(1L);
        e1.setRoleCode("ADMIN");
        e1.setRoleName("管理员");

        SysRoleEntity e2 = new SysRoleEntity();
        e2.setId(2L);
        e2.setRoleCode("USER");
        e2.setRoleName("普通用户");

        when(sysRoleMapper.selectList(any())).thenReturn(List.of(e1, e2));

        List<SysRoleOptionVO> options = sysRoleService.listOptions(null, null);

        assertThat(options).hasSize(2);
        assertThat(options.get(0).getRoleCode()).isEqualTo("ADMIN");
        assertThat(options.get(1).getRoleCode()).isEqualTo("USER");
    }
}
