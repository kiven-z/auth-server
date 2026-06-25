package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.dept.DeptClosureQueryMapper;
import com.auth.service.system.admin.model.po.dept.DeptClosureDepthChainRowPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureHealthStatsPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureParentLinkRowPO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureDepthChainAnomalyVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthStatsVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureParentLinkAnomalyVO;
import com.auth.service.system.admin.service.admin.DeptClosureHealthQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 部门闭包表健康检查只读查询实现
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeptClosureHealthQueryServiceImpl implements DeptClosureHealthQueryService {

	private final DeptClosureQueryMapper deptClosureQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DeptClosureHealthVO inspect() {
		int sampleLimit = 500;

		long missingParentLinkCount = deptClosureQueryMapper.countParentLinkAnomalies();
		List<DeptClosureParentLinkRowPO> parentLinkSamples = deptClosureQueryMapper
			.selectParentLinkAnomalies(sampleLimit);

		long depthChainAnomalyCount = deptClosureQueryMapper.countDepthChainAnomalies();
		List<DeptClosureDepthChainRowPO> depthChainSamples = deptClosureQueryMapper
			.selectDepthChainAnomalies(sampleLimit);

		DeptClosureHealthStatsPO statsPo = deptClosureQueryMapper.selectHealthStats();

		DeptClosureHealthVO result = new DeptClosureHealthVO();
		result.setCheckedAt(LocalDateTime.now());
		result.setMissingParentLinkCount(missingParentLinkCount);
		result.setParentLinkSampleTruncated(missingParentLinkCount > parentLinkSamples.size());
		result.setParentLinkAnomalies(parentLinkSamples.stream().map(this::toParentLinkAnomaly).toList());
		result.setDepthChainAnomalyCount(depthChainAnomalyCount);
		result.setDepthChainSampleTruncated(depthChainAnomalyCount > depthChainSamples.size());
		result.setDepthChainAnomalies(depthChainSamples.stream().map(this::toDepthChainAnomaly).toList());
		result.setHealthStats(toHealthStats(statsPo));
		result.setPassed(missingParentLinkCount == 0 && depthChainAnomalyCount == 0);
		return result;
	}

	/**
	 * 转换父子直连缺失异常行
	 * @param row 父子直连缺失异常行
	 * @return 父子直连缺失异常行 VO
	 */
	private DeptClosureParentLinkAnomalyVO toParentLinkAnomaly(DeptClosureParentLinkRowPO row) {
		DeptClosureParentLinkAnomalyVO vo = new DeptClosureParentLinkAnomalyVO();
		vo.setId(row.getId());
		vo.setDeptName(row.getDeptName());
		vo.setParentId(row.getParentId());
		vo.setParentLinkStatus("MISSING_PARENT_LINK");
		return vo;
	}

	/**
	 * 转换深度链条异常行
	 * @param row 深度链条异常行
	 * @return 深度链条异常行 VO
	 */
	private DeptClosureDepthChainAnomalyVO toDepthChainAnomaly(DeptClosureDepthChainRowPO row) {
		DeptClosureDepthChainAnomalyVO vo = new DeptClosureDepthChainAnomalyVO();
		vo.setId(row.getId());
		vo.setDeptName(row.getDeptName());
		vo.setParentId(row.getParentId());
		vo.setChildClosureCnt(row.getChildClosureCnt());
		vo.setParentClosureCnt(row.getParentClosureCnt());
		vo.setExpectedChildCnt(row.getExpectedChildCnt());
		return vo;
	}

	/**
	 * 转换健康度统计
	 * @param statsPo 健康度统计 PO
	 * @return 健康度统计 VO
	 */
	private DeptClosureHealthStatsVO toHealthStats(DeptClosureHealthStatsPO statsPo) {
		DeptClosureHealthStatsPO safePo = Objects.requireNonNullElse(statsPo, new DeptClosureHealthStatsPO());

		DeptClosureHealthStatsVO vo = new DeptClosureHealthStatsVO();
		vo.setOnlySelf(Objects.requireNonNullElse(safePo.getOnlySelf(), 0L));
		vo.setHasAncestors(Objects.requireNonNullElse(safePo.getHasAncestors(), 0L));
		vo.setZeroClosure(Objects.requireNonNullElse(safePo.getZeroClosure(), 0L));
		vo.setTotal(Objects.requireNonNullElse(safePo.getTotal(), 0L));
		return vo;
	}

}
