package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.DeptClosureDiagnosticsMapper;
import com.auth.service.system.admin.model.po.dept.DeptClosureDepthChainRowPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureHealthStatsPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureParentLinkRowPO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureDepthChainAnomalyVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthStatsVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureParentLinkAnomalyVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 部门闭包表健康检查（只读运维诊断）
 *
 * @author bunny
 */
@Service
public class DeptClosureHealthService {

	private final DeptClosureDiagnosticsMapper deptClosureDiagnosticsMapper;

	public DeptClosureHealthService(DeptClosureDiagnosticsMapper deptClosureDiagnosticsMapper) {
		this.deptClosureDiagnosticsMapper = deptClosureDiagnosticsMapper;
	}

	/**
	 * 执行闭包表一致性诊断
	 * @return 健康检查结果
	 */
	@Transactional(readOnly = true)
	public DeptClosureHealthVO inspect() {
		int sampleLimit = 500;

		// 缺失父级链接异常数量
		long missingParentLinkCount = deptClosureDiagnosticsMapper.countParentLinkAnomalies();
		List<DeptClosureParentLinkRowPO> parentLinkSamples = deptClosureDiagnosticsMapper
			.selectParentLinkAnomalies(sampleLimit);

		// 深度链异常数量
		long depthChainAnomalyCount = deptClosureDiagnosticsMapper.countDepthChainAnomalies();
		List<DeptClosureDepthChainRowPO> depthChainSamples = deptClosureDiagnosticsMapper
			.selectDepthChainAnomalies(sampleLimit);

		// 健康度统计
		DeptClosureHealthStatsPO statsPo = deptClosureDiagnosticsMapper.selectHealthStats();

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
		DeptClosureHealthStatsVO vo = new DeptClosureHealthStatsVO();
		if (statsPo == null) {
			vo.setOnlySelf(0L);
			vo.setHasAncestors(0L);
			vo.setZeroClosure(0L);
			vo.setTotal(0L);
			return vo;
		}
		vo.setOnlySelf(Objects.requireNonNullElse(statsPo.getOnlySelf(), 0L));
		vo.setHasAncestors(Objects.requireNonNullElse(statsPo.getHasAncestors(), 0L));
		vo.setZeroClosure(Objects.requireNonNullElse(statsPo.getZeroClosure(), 0L));
		vo.setTotal(Objects.requireNonNullElse(statsPo.getTotal(), 0L));
		return vo;
	}

}
