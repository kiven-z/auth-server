package com.auth.service.auth.authorization.infrastructure.persistence.adapter;

import com.auth.service.auth.authorization.admin.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.authorization.admin.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.authorization.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.authorization.domain.invalidation.port.InvalidationEventQueryPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

/**
 * 授权失效幂等事件只读查询适配器
 *
 * @author Bunny
 */
@Repository
public class InvalidationEventQueryAdapter implements InvalidationEventQueryPort {

    private final AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

    public InvalidationEventQueryAdapter(AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper) {
        this.authorizationInvalidationEventMapper = authorizationInvalidationEventMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<AuthorizationInvalidationEventPageRowPO> selectPage(Page<AuthorizationInvalidationEventPageRowPO> page,
                                                                     AuthorizationInvalidationEventQuery query) {
        return authorizationInvalidationEventMapper.selectListByPage(page, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationInvalidationEventDetailRowPO selectDetailById(Long id) {
        return authorizationInvalidationEventMapper.selectDetailById(id);
    }

}
