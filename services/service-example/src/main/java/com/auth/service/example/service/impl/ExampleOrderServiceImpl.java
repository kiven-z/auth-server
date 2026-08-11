package com.auth.service.example.service.impl;

import com.auth.service.example.mapper.ExampleOrderMapper;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.model.vo.ExampleOrderVO;
import com.auth.service.example.service.ExampleOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限演示单服务实现
 *
 * @author Bunny
 */
@Service
public class ExampleOrderServiceImpl implements ExampleOrderService {

	private final ExampleOrderMapper exampleOrderMapper;

	public ExampleOrderServiceImpl(ExampleOrderMapper exampleOrderMapper) {
		this.exampleOrderMapper = exampleOrderMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExampleOrderVO> listScoped() {
		List<ExampleOrderEntity> rows = exampleOrderMapper.selectScopedList();
		List<ExampleOrderVO> result = new ArrayList<>(rows.size());
		for (ExampleOrderEntity row : rows) {
			ExampleOrderVO vo = new ExampleOrderVO();
			vo.setId(row.getId());
			vo.setTitle(row.getTitle());
			vo.setDeptId(row.getDeptId());
			vo.setCreatedBy(row.getCreatedBy());
			result.add(vo);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPage<ExampleOrderEntity> exportPage(ExampleOrderQuery query, int pageNum, int pageSize) {
		ExampleOrderQuery effectiveQuery = query == null ? new ExampleOrderQuery() : query;
		Page<ExampleOrderEntity> page = new Page<>(pageNum, pageSize);
		return exampleOrderMapper.selectScopedPage(page, effectiveQuery);
	}

}
