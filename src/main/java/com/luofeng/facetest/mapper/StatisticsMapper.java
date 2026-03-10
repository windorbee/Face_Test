package com.luofeng.facetest.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {
    
    /**
     * 获取总检测次数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总检测次数
     */
    Integer getTotalTests(@Param("userId") Integer userId, 
                         @Param("startDate") LocalDate startDate, 
                         @Param("endDate") LocalDate endDate);
    
    /**
     * 获取腺体面容次数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 腺体面容次数
     */
    Integer getGlandFaceCount(@Param("userId") Integer userId, 
                             @Param("startDate") LocalDate startDate, 
                             @Param("endDate") LocalDate endDate);
    
    /**
     * 获取平均置信度
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均置信度
     */
    Double getAverageConfidence(@Param("userId") Integer userId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);
    
    /**
     * 获取趋势数据
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    @MapKey("date")
    List<Map<String, Object>> getTrendData(@Param("userId") Integer userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * 获取等级分布数据
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 等级分布数据
     */
    @MapKey("level")
    List<Map<String, Object>> getLevelData(@Param("userId") Integer userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * 获取详细统计数据
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 详细统计数据
     */
    @MapKey("date")
    List<Map<String, Object>> getDetailData(@Param("userId") Integer userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}