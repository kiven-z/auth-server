package com.auth.service.system.message.service.impl;

import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageCategoryMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_MESSAGE_CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link InAppMessageCategoryServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppMessageCategoryServiceImpl 站内信分类只读")
@ExtendWith(MockitoExtension.class)
class InAppMessageCategoryServiceImplTest {

	@Mock
	private InAppMessageCategoryMapper inAppMessageCategoryMapper;

	private InAppMessageCategoryServiceImpl service;

	private static InAppMessageCategoryEntity major(Long id, String code, String name, int sort) {
		InAppMessageCategoryEntity entity = new InAppMessageCategoryEntity();
		entity.setId(id);
		entity.setParentId(null);
		entity.setCode(code);
		entity.setName(name);
		entity.setSortOrder(sort);
		entity.setStatus(Boolean.TRUE);
		return entity;
	}

	private static InAppMessageCategoryEntity child() {
		InAppMessageCategoryEntity entity = major(11L, "SERVICE_INSPECTION", "巡检报告", 20);
		entity.setParentId(1L);
		return entity;
	}

	@BeforeEach
	void setUp() {
		// ServiceImpl.baseMapper 由 Spring 字段注入；单测手动挂上
		service = new InAppMessageCategoryServiceImpl();
		ReflectionTestUtils.setField(service, "baseMapper", inAppMessageCategoryMapper);
	}

	@Test
	@DisplayName("listMajors：仅返回启用中的大类")
	void listMajors_shouldReturnEnabledMajorsOrdered() {
		// 大类列表：委托 Mapper，保持 VO 映射
		InAppMessageCategoryEntity serviceCat = major(1L, "SERVICE", "服务消息", 10);
		InAppMessageCategoryEntity activityCat = major(2L, "ACTIVITY", "活动消息", 20);
		when(inAppMessageCategoryMapper.selectEnabledMajors()).thenReturn(List.of(serviceCat, activityCat));

		List<InAppMessageCategoryVO> result = service.listMajors();

		assertThat(result).extracting(InAppMessageCategoryVO::getCode).containsExactly("SERVICE", "ACTIVITY");
		assertThat(result.get(0).getName()).isEqualTo("服务消息");
		assertThat(result.get(0).getChildren()).isNull();
		verify(inAppMessageCategoryMapper).selectEnabledMajors();
	}

	@Test
	@DisplayName("listChildren：按大类 code 返回启用中的小类")
	void listChildren_shouldReturnEnabledChildrenByParentCode() {
		// 先定位大类，再查其子类
		InAppMessageCategoryEntity parent = major(1L, "SERVICE", "服务消息", 10);
		InAppMessageCategoryEntity child = child();
		when(inAppMessageCategoryMapper.selectEnabledMajorByCode("SERVICE")).thenReturn(parent);
		when(inAppMessageCategoryMapper.selectEnabledChildrenByParentId(1L)).thenReturn(List.of(child));

		List<InAppMessageCategoryVO> result = service.listChildren("SERVICE");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getCode()).isEqualTo("SERVICE_INSPECTION");
		assertThat(result.get(0).getName()).isEqualTo("巡检报告");
		verify(inAppMessageCategoryMapper).selectEnabledChildrenByParentId(1L);
	}

	@Test
	@DisplayName("listChildren：父 code 不存在时抛分类不存在")
	void listChildren_shouldThrowWhenParentMissing() {
		// 未知大类码直接失败，避免空列表掩盖配置错误
		when(inAppMessageCategoryMapper.selectEnabledMajorByCode("UNKNOWN")).thenReturn(null);

		assertThatThrownBy(() -> service.listChildren("UNKNOWN")).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_MESSAGE_CATEGORY_NOT_FOUND);
	}

	@Test
	@DisplayName("listChildren：空白父 code 时抛分类不存在")
	void listChildren_shouldThrowWhenParentCodeBlank() {
		// 空白入参不查库，直接按不存在处理
		assertThatThrownBy(() -> service.listChildren("  ")).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_MESSAGE_CATEGORY_NOT_FOUND);
	}

}
