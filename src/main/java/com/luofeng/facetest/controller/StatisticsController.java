package com.luofeng.facetest.controller;

import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.service.StatisticsService;
import com.luofeng.facetest.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取统计概览数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计概览数据
     */
    @GetMapping("/overview")
    public Result getStatisticsOverview(
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) LocalDate startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) LocalDate endDate) {
        try {
            Integer userId = UserThreadLocal.getUserId();
            Map<String, Object> statistics = statisticsService.getStatisticsOverview(userId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取统计概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取详细统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 详细统计数据
     */
    @GetMapping("/detail")
    public Result getStatisticsDetail(
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) LocalDate startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(required = false) LocalDate endDate) {
        try {
            Integer userId = UserThreadLocal.getUserId();
            return Result.success(statisticsService.getStatisticsDetail(userId, startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取详细统计失败: " + e.getMessage());
        }
    }
}