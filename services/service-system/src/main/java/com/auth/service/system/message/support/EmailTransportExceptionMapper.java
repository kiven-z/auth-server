package com.auth.service.system.message.support;

import com.auth.module.message.sender.modules.email.transport.EmailTransportException;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.experimental.UtilityClass;

import static com.auth.service.system.common.exception.code.common.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static com.auth.service.system.common.exception.code.message.SystemEmailResultCode.*;

/**
 * Maps
 *
 * @author Bunny
 */
@UtilityClass
public class EmailTransportExceptionMapper {

	/**
	 * @param exception transport-layer failure
	 * @return business exception with i18n result code
	 */
	public static SystemBusinessException toBusinessException(EmailTransportException exception) {
		return switch (exception.getReason()) {
			case SEND_DTO_NULL -> new SystemBusinessException(EMAIL_SEND_DTO_NULL);
			case TO_EMPTY -> new SystemBusinessException(EMAIL_TO_EMPTY);
			case SUBJECT_EMPTY -> new SystemBusinessException(EMAIL_SUBJECT_EMPTY);
			case BODY_EMPTY -> new SystemBusinessException(EMAIL_BODY_EMPTY);
			case ATTACHMENT_FILENAME_EMPTY -> new SystemBusinessException(EMAIL_ATTACHMENT_FILENAME_EMPTY);
			case ATTACHMENT_BASE64_FAILED ->
				new SystemBusinessException(EMAIL_ATTACHMENT_BASE64_FAILED, exception.getMessage());
			case ATTACHMENT_HTTP_STATUS ->
				new SystemBusinessException(EMAIL_ATTACHMENT_HTTP_STATUS, exception.getMessage());
			case ATTACHMENT_DOWNLOAD_FAILED ->
				new SystemBusinessException(EMAIL_ATTACHMENT_DOWNLOAD_FAILED, exception.getMessage());
			case SEND_FAILED -> new SystemBusinessException(SERVICE_UNAVAILABLE, exception.getMessage());
		};
	}

}
