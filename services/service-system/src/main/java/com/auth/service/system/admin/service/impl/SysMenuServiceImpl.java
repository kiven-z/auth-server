package com.auth.service.system.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuListQuery;
import com.auth.service.system.admin.model.query.SysMenuPageQuery;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.SysMenuService;
import com.auth.service.system.admin.support.SysMenuFormMapper;
import com.auth.service.system.admin.support.SysMenuListVoConverter;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
	public List<SysMenuListVO> listFlat(SysMenuListQuery query) {
		List<SysMenuEntity> entities = list(buildListWrapper(query));
		List<SysMenuListVO> rows = entities.stream().map(SysMenuListVoConverter::fromEntity).toList();
		auditUserDisplayService.enrichAuditUsernames(rows);
		return rows;
	}

	/**
	 * 分页查询菜单（表格视图）
	 * @param pageParams 分页参数
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	@Override
	public PageResponse<SysMenuListVO> pageFlat(Page<SysMenuEntity> pageParams, SysMenuPageQuery query) {
		IPage<SysMenuEntity> entityPage = page(pageParams, buildPageWrapper(query));
		List<SysMenuListVO> rows = entityPage.getRecords().stream().map(SysMenuListVoConverter::fromEntity).toList();
		auditUserDisplayService.enrichAuditUsernames(rows);
		return PageResponse.of(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal(), rows);
	}

	/**
	 * 菜单详情
	 * @param id 主键
	 * @return 详情
	 */
	@Override
	public SysMenuDetailVO getDetail(Long id) {
		SysMenuEntity entity = requireMenu(id);
		SysMenuDetailVO vo = SysMenuListVoConverter.toDetailVO(entity);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo));
		return vo;
	}

	/**
	 * 新增菜单
	 * @param form 表单
	 * @return 新菜单主键字符串（与前端约定）
	 */
	@Override
	public String create(SysMenuSaveForm form) {
		SysMenuEntity entity = new SysMenuEntity();
		SysMenuFormMapper.applyToEntity(form, entity);
		save(entity);
		return String.valueOf(entity.getId());
	}

	/**
	 * 更新菜单
	 * @param form 表单（含 id）
	 */
	@Override
	public void update(SysMenuSaveForm form) {
		requireMenu(form.getId());
		SysMenuEntity entity = new SysMenuEntity();

		entity.setId(form.getId());
		SysMenuFormMapper.applyToEntity(form, entity);
		updateById(entity);
	}

	/**
	 * 批量逻辑删除
	 * @param ids 主键列表
	 */
	@Override
	public void removeByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		removeBatchByIds(ids);
	}

	private SysMenuEntity requireMenu(Long id) {
		SysMenuEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(SystemResultCodeEnum.MENU_NOT_FOUND, id);
		}
		return entity;
	}

	private LambdaQueryWrapper<SysMenuEntity> buildListWrapper(SysMenuListQuery query) {
		LambdaQueryWrapper<SysMenuEntity> w = Wrappers.lambdaQuery();
		w.like(StrUtil.isNotBlank(query.getName()), SysMenuEntity::getName, query.getName());
		w.like(StrUtil.isNotBlank(query.getTitle()), SysMenuEntity::getTitle, query.getTitle());
		w.eq(query.getMenuType() != null, SysMenuEntity::getMenuType, query.getMenuType());
		w.like(StrUtil.isNotBlank(query.getComponent()), SysMenuEntity::getComponent, query.getComponent());
		if (query.getStatus() != null) {
			if (query.getStatus() == 0) {
				w.eq(SysMenuEntity::getStatus, Boolean.TRUE);
			}
			else if (query.getStatus() == 1) {
				w.eq(SysMenuEntity::getStatus, Boolean.FALSE);
			}
		}
		w.orderByAsc(SysMenuEntity::getMenuRank).orderByAsc(SysMenuEntity::getId);
		return w;
	}

	private LambdaQueryWrapper<SysMenuEntity> buildPageWrapper(SysMenuPageQuery query) {
		SysMenuListQuery q = new SysMenuListQuery();
		q.setName(query.getName());
		q.setTitle(query.getTitle());
		q.setMenuType(query.getMenuType());
		q.setStatus(query.getStatus());
		q.setComponent(query.getComponent());
		return buildListWrapper(q);
	}

}
