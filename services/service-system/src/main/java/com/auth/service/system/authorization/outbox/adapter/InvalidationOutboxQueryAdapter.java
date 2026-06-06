package com.auth.service.system.authorization.outbox.adapter;

import com.auth.service.system.authorization.outbox.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.outbox.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.outbox.port.InvalidationOutboxQueryPort;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

/**
 * 授权失效 Outbox 只读查询适配器
 *
 * @author Bunny
 */
@Repository
public class InvalidationOutboxQueryAdapter implements InvalidationOutboxQueryPort {

    private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

    public InvalidationOutboxQueryAdapter(SysAuthorizationInvalidationOutboxMapper outboxMapper) {
        this.outboxMapper = outboxMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<AuthorizationInvalidationOutboxPageRowPO> selectPage(
            Page<AuthorizationInvalidationOutboxPageRowPO> page, AuthorizationInvalidationOutboxQuery query) {
        return outboxMapper.selectListByPage(page, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationInvalidationOutboxDetailRowPO selectDetailById(Long id) {
        return outboxMapper.selectDetailById(id);
    }

}
