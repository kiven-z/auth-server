package com.auth.service.system.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysMenuConverter;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.SysMenuService;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 菜单服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

	private final AuditUserDisplayService auditUserDisplayService;

	public SysMenuServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * 条件查询全部菜单（扁平，供前端建树）
	 * @param query 筛选条件
	 * @return 菜单行
	 */
	@Override
	public List<SysMenuListVO> listFlat(SysMenuQuery query) {
		List<SysMenuListVO> list = baseMapper.selectListByQuery(query);
		auditUserDisplayService.enrichAuditUsernames(list);
		return list;
	}

	/**
	 * 分页查询菜单（表格视图）
	 * @param pageParams 分页参数
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	@Override
	public PageResponse<SysMenuListVO> pageFlat(Page<SysMenuEntity> pageParams, SysMenuQuery query) {
		IPage<SysMenuListVO> result = baseMapper.selectPageByQuery(pageParams, query);
		auditUserDisplayService.enrichAuditUsernames(result);
		return PageResponse.of(result);
	}

	/**
	 * 菜单详情
	 * @param id 主键
	 * @return 详情
	 */
	@Override
	public SysMenuDetailVO getDetail(Long id) {
		SysMenuEntity entity = getById(id);
		return SysMenuConverter.INSTANCE.toDetailVo(entity);
	}

	/**
	 * 新增菜单
	 * @param form 表单
	 * @return 新菜单主键字符串（与前端约定）
	 */
	@Override
	public String create(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(ObjectUtil.defaultIfNull(entity.getParentId(), 0L));
		save(entity);

		Long menuId = entity.getId();
		return String.valueOf(menuId);
	}

	/**
	 * 更新菜单
	 * @param form 表单（含 id）
	 */
	@Override
	public void update(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(ObjectUtil.defaultIfNull(entity.getParentId(), 0L));
		updateById(entity);
	}

	/**
	 * 批量更新状态（0=启用 1=禁用）
	 * @param form 请求体
	 */
	@Override
	public void batchUpdateStatus(SysMenuBatchStatusForm form) {
		Boolean status = form.getStatus();
		List<SysMenuEntity> list = form.getIds().stream().filter(Objects::nonNull).map(id -> {
			SysMenuEntity sysMenuEntity = new SysMenuEntity();
			sysMenuEntity.setId(id);
			sysMenuEntity.setStatus(status);
			return sysMenuEntity;
		}).toList();
		updateBatchById(list);
	}

}
