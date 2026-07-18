package com.auth.service.system.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.service.InAppMessageRecipientWriteService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 站内信收件箱写入实现
 *
 * @author Bunny
 */
@Service
public class InAppMessageRecipientWriteServiceImpl
		extends ServiceImpl<InAppMessageRecipientMapper, InAppMessageRecipientEntity>
		implements InAppMessageRecipientWriteService {

	/**
	 * 批量写入批次大小
	 */
	private static final int BATCH_SIZE = 500;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public int insertBatch(List<InAppMessageRecipientEntity> rows) {
		if (CollUtil.isEmpty(rows)) {
			return 0;
		}
		int inserted = 0;

		List<List<InAppMessageRecipientEntity>> partitions = CollUtil.split(rows, BATCH_SIZE);
		for (List<InAppMessageRecipientEntity> partition : partitions) {
			partition.forEach(row -> {
				row.setId(Objects.requireNonNullElseGet(row.getId(), IdWorker::getId));
				row.setVersion(Objects.requireNonNullElse(row.getVersion(), 0L));
			});

			inserted += baseMapper.insertIgnoreBatch(partition);
		}
		return inserted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int countByMessageId(Long messageId) {
		if (messageId == null) {
			return 0;
		}
		long count = super.count(Wrappers.<InAppMessageRecipientEntity>lambdaQuery()
			.eq(InAppMessageRecipientEntity::getMessageId, messageId));
		return Math.toIntExact(count);
	}

}
