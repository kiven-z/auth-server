package com.auth.service.system.message.api.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.api.DingTalkWorkNoticeService;
import com.auth.service.system.message.channel.dingtalk.DingTalkWorkNoticeClient;
import com.auth.service.system.message.channel.dingtalk.model.DingTalkWorkNoticeSendForm;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static com.auth.service.system.common.exception.code.message.SystemMessageResultCode.*;

/**
 * 钉钉工作通知发送实现
 *
 * @author Bunny
 */
@ConditionalOnProperty(prefix = "auth.message.ding-talk", name = "enabled", havingValue = "true")
@Service
public class DingTalkWorkNoticeServiceImpl implements DingTalkWorkNoticeService {

	private final ObjectProvider<DingTalkWorkNoticeClient> workNoticeClient;

	public DingTalkWorkNoticeServiceImpl(ObjectProvider<DingTalkWorkNoticeClient> workNoticeClient) {
		this.workNoticeClient = workNoticeClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(DingTalkWorkNoticeSendForm form) {
		if (form == null || CollUtil.isEmpty(form.getUserIds())) {
			throw new SystemBusinessException(DING_TALK_TARGET_EMPTY);
		}

		DingTalkWorkNoticeClient client = workNoticeClient.getIfAvailable();
		if (client == null) {
			throw new SystemBusinessException(DING_TALK_NOT_ENABLED);
		}

		try {
			client.sendWorkNotice(form.getUserIds(), form.getMessageType(), form.getTitle(), form.getContent());
		}
		catch (IllegalStateException | IllegalArgumentException ex) {
			throw new SystemBusinessException(DING_TALK_SEND_FAILED, ex.getMessage());
		}
	}

}
