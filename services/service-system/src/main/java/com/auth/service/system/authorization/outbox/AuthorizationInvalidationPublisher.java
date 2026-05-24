package com.auth.service.system.authorization.outbox;

import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.service.system.authorization.outbox.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.outbox.model.SysAuthorizationInvalidationOutboxEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 授权失效 Outbox 写入：与业务表同一事务插入 PENDING 记录。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class AuthorizationInvalidationPublisher {

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final InvalidationOutboxPayloadCodec payloadCodec;

	/**
	 * 在调用方事务内写入 Outbox（status=PENDING）。
	 * @param request 失效请求
	 * @param sourceModule 触发模块，如 SYS_ROLE
	 * @param sourceBizId 业务主键，如 roleCode
	 * @return 新 Outbox 主键
	 */
	@Transactional(rollbackFor = Exception.class)
	public Long enqueue(AuthorizationInvalidateRequest request, String sourceModule, String sourceBizId) {
		LocalDateTime now = LocalDateTime.now();
		SysAuthorizationInvalidationOutboxEntity record = new SysAuthorizationInvalidationOutboxEntity();
		record.setId(IdWorker.getId());
		record.setEventId(request.eventId());
		record.setChangeKind(request.kind().name());
		record.setPayload(payloadCodec.serialize(request.payload()));
		record.setStatus(AuthorizationInvalidationOutboxStatus.PENDING.name());
		record.setRetryCount(0);
		record.setMaxRetry(5);
		record.setNextRetryAt(now);
		record.setSourceModule(sourceModule);
		record.setSourceBizId(sourceBizId);
		record.setCreateTime(now);
		record.setUpdateTime(now);
		record.setVersion(0L);
		outboxMapper.insertPending(record);
		return record.getId();
	}

}
