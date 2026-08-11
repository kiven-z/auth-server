package com.auth.module.file.api.model.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 内部分页取数响应 data（异步导出回调）
 *
 * @author Bunny
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportPageDataDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 当前页数据
	 */
	private transient List<Map<String, Object>> records;

	/**
	 * 总行数
	 */
	private Long total;

}
