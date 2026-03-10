package com.luofeng.facetest.mapper;

import com.luofeng.facetest.pojo.testResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TestResultMapper {
    
    /**
     * 用户查询自己的检测结果（支持条件检索）
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     * @return 检测结果列表
     */
    List<testResult> getUserTestResults(@Param("userId") Integer userId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("isGlandFace") Boolean isGlandFace,
                                        @Param("level") String level);
    
    /**
     * 用户删除自己的检测结果
     * @param resultId 检测结果ID
     * @param userId 用户ID
     * @return 删除记录数
     */
    int deleteUserTestResult(@Param("resultId") Integer resultId, @Param("userId") Integer userId);
    
    /**
     * 用户批量删除检测结果
     * @param ids 检测结果ID列表
     * @param userId 用户ID
     * @return 删除记录数
     */
    int deleteBatchUserTestResults(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);

    /**
     * 查询用户检测结果用于导出
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     * @return 检测结果列表
     */
    List<testResult> getTestResultsForExport(@Param("userId") Integer userId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("isGlandFace") Boolean isGlandFace,
                                             @Param("level") String level);
}