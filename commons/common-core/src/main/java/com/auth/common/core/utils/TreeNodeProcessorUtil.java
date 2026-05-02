package com.auth.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.entity.TreeNode;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形结构 构造
 *
 * @author Bunny
 */
@UtilityClass
public class TreeNodeProcessorUtil {

	/**
	 * 处理列表数据，构建树形结构
	 * @param list 待处理的扁平数据列表
	 * @return 树形结构的根节点列表
	 */
	public static <T extends TreeNode<T>> List<T> process(List<T> list) {
		// 查找所有根节点
		List<T> roots = findRoots(list);
		// 为每个根节点构建子节点树
		for (T root : roots) {
			buildChildren(root, list);
		}
		return roots;
	}

	/**
	 * 查找根节点列表
	 * @param list 所有节点列表
	 * @return 根节点列表
	 */
	public static <T extends TreeNode<T>> List<T> findRoots(List<T> list) {
		return list.stream().filter(node -> node.getParentId() == null || node.getParentId().equals(0L)).toList();
	}

	/**
	 * 为指定父节点构建子节点树
	 * @param parent 父节点
	 * @param list 所有节点列表
	 */
	private static <T extends TreeNode<T>> void buildChildren(T parent, List<T> list) {
		List<T> children = list.stream()
			.filter(node -> node.getParentId() != null && node.getParentId().equals(parent.getId()))
			.toList();

		// 设置是否显示
		parent.setHasLeaf(CollUtil.isNotEmpty(children));

		// 设置是否有子节点
		parent.setChildren(children.isEmpty() ? null : children);

		// 递归构建每个子节点的子树
		for (T child : children) {
			buildChildren(child, list);
		}
	}

	/**
	 * 构建树形结构（递归方式）- 实现相同的效果
	 */
	public static <T extends TreeNode<T>> List<T> buildTreeRecursive(List<T> list) {
		if (CollUtil.isEmpty(list)) {
			return new ArrayList<>();
		}

		// 找出所有根节点（parentId = 0）
		List<T> rootNodes = list.stream().filter(node -> node.getParentId() == 0L).toList();

		// 为每个根节点构建子树
		for (T rootNode : rootNodes) {
			findChildren(rootNode, list);
		}

		return rootNodes;
	}

	/**
	 * 递归查找子节点
	 */
	private static <T extends TreeNode<T>> void findChildren(T parentNode, List<T> allNodes) {
		// 找出当前节点的所有子节点
		List<T> children = allNodes.stream().filter(node -> parentNode.getId().equals(node.getParentId())).toList();

		if (CollUtil.isNotEmpty(children)) {
			// 设置子节点
			parentNode.setChildren(children);
			parentNode.setHasLeaf(true);

			// 递归为每个子节点查找它们的子节点
			for (T child : children) {
				findChildren(child, allNodes);
			}
		}
		else {
			// 如果没有子节点，设置空列表
			parentNode.setChildren(new ArrayList<>());
			parentNode.setHasLeaf(false);
		}
	}

}