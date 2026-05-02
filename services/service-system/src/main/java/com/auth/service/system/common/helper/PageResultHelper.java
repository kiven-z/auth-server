package com.auth.service.system.common.helper;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.common.data.model.BaseEntity;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.common.mapper.UserMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 分页结果助手类
 *
 * @author Bunny
 */
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class PageResultHelper {

	private final UserMapper userMapper;

	public PageResultHelper(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	/**
	 * 为分页结果中的每条记录设置创建和更新用户的用户名
	 * @param page 分页结果对象，包含需要设置用户名的记录列表
	 * @param <T> 继承自BaseVo的泛型类型
	 */
	public <T extends BaseResponse> void setUpdateWithCreateUsername(IPage<T> page) {
		List<T> records = page.getRecords();

		// 如果当前页没有数据，直接返回空的分页结果
		if (CollUtil.isEmpty(records)) {
			return;
		}

		Map<Long, String> usernameMap;
		try {
			usernameMap = getUsernameMapInternal(records);
		}
		catch (Exception exception) {
			log.error("分页结果用户名设置失败", exception);
			return;
		}

		// 遍历每条记录，设置创建和更新用户的用户名
		records.forEach(vo -> {
			Long createUser = vo.getCreateUser();
			Long updateUser = vo.getUpdateUser();

			// 从映射中获取用户名并设置到 VO 对象中
			String createUsername = usernameMap.get(createUser);
			String updateUsername = usernameMap.get(updateUser);

			vo.setCreateUsername(createUsername);
			vo.setUpdateUsername(updateUsername);
		});
	}

	/**
	 * 内部方法：根据记录列表提取所有涉及的创建和更新用户的ID，并查询对应的用户名
	 * @param records 记录列表，可以是BaseEntity或BaseVo的实例
	 * @param <T> 记录类型
	 * @return 一个映射，键为用户ID，值为对应的用户名
	 */
	private <T> Map<Long, String> getUsernameMapInternal(List<T> records) {
		if (records.isEmpty()) {
			return new HashMap<>(0);
		}

		// 批量查询 username（避免循环查库）
		// 根据当前查询出来的用户信息中包含的：创建用户id、更新用户id
		// 使用 in 对数据库查询
		List<Long> userIds = records.stream()
			.filter(t -> t instanceof BaseEntity || t instanceof BaseResponse)
			.flatMap(t -> {
				Long createUser;
				Long updateUser;
				if (t instanceof BaseEntity baseEntity) {
					createUser = baseEntity.getCreateUser();
					updateUser = baseEntity.getUpdateUser();
				}
				else {
					createUser = ((BaseResponse) t).getCreateUser();
					updateUser = ((BaseResponse) t).getUpdateUser();
				}
				return Stream.of(createUser, updateUser).filter(Objects::nonNull);
			})
			.distinct()
			.toList();

		if (userIds.isEmpty()) {
			return new HashMap<>(0);
		}

		return userMapper.selectBatchIds(userIds)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
	}

}
