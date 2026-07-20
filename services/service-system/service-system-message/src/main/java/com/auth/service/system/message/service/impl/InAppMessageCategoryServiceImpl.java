package com.auth.service.system.message.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageCategoryMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.auth.service.system.message.service.InAppMessageCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_MESSAGE_CATEGORY_NOT_FOUND;

/**
 * 站内信业务分类只读查询
 *
 * @author Bunny
 */
@Service
public class InAppMessageCategoryServiceImpl extends ServiceImpl<InAppMessageCategoryMapper, InAppMessageCategoryEntity>
		implements InAppMessageCategoryService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<InAppMessageCategoryVO> listMajors() {
		List<InAppMessageCategoryEntity> list = baseMapper.selectEnabledMajors();
		return list.stream().map(this::toFlatVo).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<InAppMessageCategoryVO> listChildren(String parentCode) {
		if (CharSequenceUtil.isBlank(parentCode)) {
			throw new MessageException(IN_APP_MESSAGE_CATEGORY_NOT_FOUND, parentCode);
		}
		InAppMessageCategoryEntity parent = baseMapper.selectEnabledMajorByCode(parentCode);
		if (parent == null) {
			throw new MessageException(IN_APP_MESSAGE_CATEGORY_NOT_FOUND, parentCode);
		}

		List<InAppMessageCategoryEntity> list = baseMapper.selectEnabledChildrenByParentId(parent.getId());
		return list.stream().map(this::toFlatVo).toList();
	}

	/**
	 * 实体 → 扁平 VO（无 children）
	 * @param entity 分类实体
	 * @return VO
	 */
	private InAppMessageCategoryVO toFlatVo(InAppMessageCategoryEntity entity) {
		return InAppMessageCategoryVO.builder()
			.code(entity.getCode())
			.name(entity.getName())
			.sortOrder(entity.getSortOrder())
			.children(null)
			.build();
	}

}
