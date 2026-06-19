package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.dept.DeptClosureQueryMapper;
import com.auth.service.system.admin.model.po.dept.DeptClosureDepthChainRowPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureHealthStatsPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureParentLinkRowPO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * {@link DeptClosureHealthQueryServiceImpl} 单元测试。
 */
@DisplayName("DeptClosureHealthQueryServiceImpl 闭包表健康检查")
@ExtendWith(MockitoExtension.class)
class DeptClosureHealthQueryServiceImplTest {

	@Mock
	private DeptClosureQueryMapper deptClosureQueryMapper;

	@InjectMocks
	private DeptClosureHealthQueryServiceImpl deptClosureHealthQueryService;

	@Test
	@DisplayName("inspect：无异常时 passed 为 true")
	void inspect_whenNoAnomalies_passedTrue() {
		DeptClosureHealthStatsPO stats = new DeptClosureHealthStatsPO();
		stats.setOnlySelf(1L);
		stats.setHasAncestors(2L);
		stats.setZeroClosure(0L);
		stats.setTotal(3L);
		when(deptClosureQueryMapper.countParentLinkAnomalies()).thenReturn(0L);
		when(deptClosureQueryMapper.countDepthChainAnomalies()).thenReturn(0L);
		when(deptClosureQueryMapper.selectHealthStats()).thenReturn(stats);

		DeptClosureHealthVO result = deptClosureHealthQueryService.inspect();

		assertThat(result.getPassed()).isTrue();
		assertThat(result.getMissingParentLinkCount()).isZero();
		assertThat(result.getDepthChainAnomalyCount()).isZero();
		assertThat(result.getParentLinkSampleTruncated()).isFalse();
		assertThat(result.getDepthChainSampleTruncated()).isFalse();
		assertThat(result.getHealthStats().getTotal()).isEqualTo(3L);
		assertThat(result.getCheckedAt()).isNotNull();
	}

	@Test
	@DisplayName("inspect：存在父子直连缺失时 passed 为 false")
	void inspect_whenParentLinkMissing_passedFalse() {
		DeptClosureParentLinkRowPO row = new DeptClosureParentLinkRowPO();
		row.setId(10L);
		row.setDeptName("研发一部");
		row.setParentId(1L);
		when(deptClosureQueryMapper.countParentLinkAnomalies()).thenReturn(1L);
		when(deptClosureQueryMapper.selectParentLinkAnomalies(anyInt())).thenReturn(List.of(row));
		when(deptClosureQueryMapper.countDepthChainAnomalies()).thenReturn(0L);
		when(deptClosureQueryMapper.selectHealthStats()).thenReturn(new DeptClosureHealthStatsPO());

		DeptClosureHealthVO result = deptClosureHealthQueryService.inspect();

		assertThat(result.getPassed()).isFalse();
		assertThat(result.getParentLinkAnomalies()).hasSize(1);
		assertThat(result.getParentLinkAnomalies().get(0).getParentLinkStatus()).isEqualTo("MISSING_PARENT_LINK");
	}

	@Test
	@DisplayName("inspect：异常数超过样本上限时标记截断")
	void inspect_whenCountExceedsSample_truncatedTrue() {
		when(deptClosureQueryMapper.countParentLinkAnomalies()).thenReturn(250L);
		when(deptClosureQueryMapper.countDepthChainAnomalies()).thenReturn(0L);
		when(deptClosureQueryMapper.selectHealthStats()).thenReturn(new DeptClosureHealthStatsPO());

		DeptClosureHealthVO result = deptClosureHealthQueryService.inspect();

		assertThat(result.getParentLinkSampleTruncated()).isTrue();
		assertThat(result.getMissingParentLinkCount()).isEqualTo(250L);
	}

	@Test
	@DisplayName("inspect：存在深度链条异常时 passed 为 false")
	void inspect_whenDepthChainBroken_passedFalse() {
		DeptClosureDepthChainRowPO row = new DeptClosureDepthChainRowPO();
		row.setId(20L);
		row.setDeptName("子部门");
		row.setParentId(1L);
		row.setChildClosureCnt(1L);
		row.setParentClosureCnt(3L);
		row.setExpectedChildCnt(4L);
		when(deptClosureQueryMapper.countParentLinkAnomalies()).thenReturn(0L);
		when(deptClosureQueryMapper.countDepthChainAnomalies()).thenReturn(1L);
		when(deptClosureQueryMapper.selectDepthChainAnomalies(anyInt())).thenReturn(List.of(row));
		when(deptClosureQueryMapper.selectHealthStats()).thenReturn(new DeptClosureHealthStatsPO());

		DeptClosureHealthVO result = deptClosureHealthQueryService.inspect();

		assertThat(result.getPassed()).isFalse();
		assertThat(result.getDepthChainAnomalies()).hasSize(1);
		assertThat(result.getDepthChainAnomalies().get(0).getExpectedChildCnt()).isEqualTo(4L);
	}

}
