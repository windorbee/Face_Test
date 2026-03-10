package com.luofeng.facetest.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    
    /**
     * 获取统计概览数据
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计概览数据
     */
    Map<String, Object> getStatisticsOverview(Integer userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取详细统计数据
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 详细统计数据
     */
    List<Map<String, Object>> getStatisticsDetail(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取平均置信度
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均置信度
     */
    Double getAverageConfidence(Integer userId, LocalDate startDate, LocalDate endDate);
}