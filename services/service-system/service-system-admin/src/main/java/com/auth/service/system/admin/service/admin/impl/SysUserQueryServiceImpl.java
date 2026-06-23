package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.reference.ReferenceConverter;
import com.auth.service.system.admin.convert.sys.SysUserProfileConverter;
import com.auth.service.system.admin.mapper.user.SysUserQueryMapper;
import com.auth.service.system.admin.model.po.user.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.user.UserPostProfilePO;
import com.auth.service.system.admin.model.po.user.UserSearchItemPO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户只读查询实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysUserQueryServiceImpl extends ServiceImpl<SysUserQueryMapper, UserEntity>
		implements SysUserQueryService {

	private final UserReferenceChecker userReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserProfileVO getProfile(Long userId) {
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "userId");
		}
		UserEntity user = userReferenceChecker.getExistingActive(userId);

		SysUserProfileVO response = SysUserProfileConverter.INSTANCE.toProfileVo(user);

		List<UserDeptProfilePO> deptProfileList = baseMapper.selectDeptProfileByUserId(userId);
		response.setDeptList(ReferenceConverter.INSTANCE.toUserDeptReferenceList(deptProfileList));

		List<UserPostProfilePO> postProfileList = baseMapper.selectPostProfileByUserId(userId);
		response.setPostList(ReferenceConverter.INSTANCE.toUserPostReferenceList(postProfileList));

		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysUserSearchItemVO> searchByKeyword(String keyword, Integer limit) {
		if (CharSequenceUtil.isBlank(keyword)) {
			return Collections.emptyList();
		}

		limit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
		List<UserSearchItemPO> searchItemList = baseMapper.searchByKeyword(keyword, limit);
		List<UserSearchItemPO> list = Objects.requireNonNullElse(searchItemList, List.of());

		return SysUserProfileConverter.INSTANCE.toSearchItemVoList(list);
	}

}
