package com.luofeng.facetest.service;

import com.github.pagehelper.PageInfo;
import com.luofeng.facetest.pojo.testResult;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TestResultService {
    
    /**
     * 用户查看自己的检测结果（支持分页和条件检索）
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     * @return 检测结果分页信息
     */
    PageInfo<testResult> getUserTestResults(Integer userId, Integer page, Integer pageSize,
                                            LocalDateTime startDate, LocalDateTime endDate,
                                            Boolean isGlandFace, String level);
    
    /**
     * 用户删除自己的检测结果
     * @param resultId 检测结果ID
     * @param userId 用户ID
     * @return 删除是否成功
     */
    boolean deleteUserTestResult(Integer resultId, Integer userId);
    
    /**
     * 用户批量删除检测结果
     * @param ids 检测结果ID列表
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteBatchUserTestResults(List<Integer> ids, Integer userId);
    
    /**
     * 导出用户检测结果到Excel
     * @param response HTTP响应对象
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     * @throws Exception 导出异常
     */
    void exportTestResultsToExcel(HttpServletResponse response, Integer userId, LocalDateTime startDate, LocalDateTime endDate,
                                  Boolean isGlandFace, String level) throws Exception;
    
    /**
     * 导出用户检测结果到PDF
     * @param response HTTP响应对象
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     * @throws Exception 导出异常
     */
    void exportTestResultsToPDF(HttpServletResponse response, Integer userId, LocalDateTime startDate, LocalDateTime endDate,
                                Boolean isGlandFace, String level) throws Exception;
}