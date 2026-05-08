package com.auth.service.system.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.common.mapper.UserMapper;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 审计用户展示名：基于用户表批量解析用户名。
 *
 * @author Bunny
 */
@Service
public class AuditUserDisplayServiceImpl implements AuditUserDisplayService {

    private final UserMapper userMapper;

    public AuditUserDisplayServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 从分页行中收集创建人、更新人用户 ID（去重、非空）。
     *
     * @param records 分页行
     * @param <T>     行类型
     * @return 用户主键列表
     */
    private static <T extends BaseResponse> List<Long> collectAuditUserIds(List<T> records) {
        return records.stream()
                .flatMap(vo -> Stream.of(vo.getCreateUser(), vo.getUpdateUser()).filter(Objects::nonNull))
                .distinct()
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, String> mapUsernamesByIds(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return new HashMap<>(0);
        }
        List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return new HashMap<>(0);
        }
        return userMapper.selectBatchIds(distinctIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
    }

    @Override
    public <T extends BaseResponse> void enrichAuditUsernames(IPage<T> page) {
        List<T> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return;
        }
        Map<Long, String> usernameMap = mapUsernamesByIds(collectAuditUserIds(records));
        records.forEach(vo -> {
            Long createUser = vo.getCreateUser();
            Long updateUser = vo.getUpdateUser();
            vo.setCreateUsername(usernameMap.get(createUser));
            vo.setUpdateUsername(usernameMap.get(updateUser));
        });
    }

}
