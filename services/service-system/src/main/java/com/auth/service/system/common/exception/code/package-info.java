/**
 * 系统服务（模块 10）结果码按域拆分；{@code getCode()} 在模块内全局唯一。
 *
 * <h2>号段登记（新增码请先占用并更新本表）</h2>
 * <ul>
 * <li>{@link com.auth.service.system.common.exception.code.common.SystemCommonResultCode}
 * — 0, 101–103, 199, 201–211, 301–302, 401–405</li>
 * <li>{@link com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode}
 * — 212–240</li>
 * <li>{@link com.auth.service.system.common.exception.code.message.SystemEmailResultCode}
 * — 241–262</li>
 * <li>{@link com.auth.service.system.common.exception.code.admin.SystemAdminResultCode} —
 * 263–278</li>
 * <li>{@link com.auth.service.system.common.exception.code.message.SystemMessageResultCode}
 * — 279–285（下一可用 286）</li>
 * </ul>
 */
package com.auth.service.system.common.exception.code;
