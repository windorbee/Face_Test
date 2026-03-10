package com.luofeng.facetest.controller;

import com.github.pagehelper.PageInfo;
import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.pojo.testResult;
import com.luofeng.facetest.service.TestResultService;
import com.luofeng.facetest.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/testResult")
public class TestResultController {
    
    @Autowired
    private TestResultService testResultService;
    
    /**
     * 用户查看自己的检测结果（支持分页和条件检索）
     * @param page 页码
     * @param pageSize 每页大小
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     * @return 检测结果列表
     */
    @GetMapping("/result")
    public Result getUserTestResults(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime startDate,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime endDate,
                                     @RequestParam(required = false) Boolean isGlandFace,
                                     @RequestParam(required = false) String level) {
        try {
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            PageInfo<testResult> pageInfo = testResultService.getUserTestResults(userId, page, pageSize, 
                    startDate, endDate, isGlandFace, level);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户删除自己的检测结果
     * @param resultId 检测结果ID
     * @return 删除结果
     */
    @DeleteMapping("/{resultId}")
    public Result deleteUserTestResult(@PathVariable Integer resultId) {
        try {
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            boolean success = testResultService.deleteUserTestResult(resultId, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败，记录不存在或无权限");
            }
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户批量删除检测结果
     * @param ids 检测结果ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result deleteBatchUserTestResults(@RequestBody List<Integer> ids) {
        try {
            // 检查参数
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的记录");
            }
            
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            int deletedCount = testResultService.deleteBatchUserTestResults(ids, userId);
            return Result.success("成功删除 " + deletedCount + " 条记录");
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户下载检测记录（支持条件筛选后下载）
     * @param response HTTP响应对象
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     */
    @GetMapping("/download")
    public void downloadUserTestResults(HttpServletResponse response,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime startDate,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime endDate,
                                        @RequestParam(required = false) Boolean isGlandFace,
                                        @RequestParam(required = false) String level) {
        try {
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            testResultService.exportTestResultsToExcel(response, userId, startDate, endDate, isGlandFace, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 用户下载PDF格式检测记录（支持条件筛选后下载）
     * @param response HTTP响应对象
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isGlandFace 是否为腺体面容
     * @param level 等级
     */
    @GetMapping("/download/pdf")
    public void downloadUserTestResultsAsPDF(HttpServletResponse response,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime startDate,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime endDate,
                                        @RequestParam(required = false) Boolean isGlandFace,
                                        @RequestParam(required = false) String level) {
        try {
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            testResultService.exportTestResultsToPDF(response, userId, startDate, endDate, isGlandFace, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}