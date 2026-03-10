package com.luofeng.facetest.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.luofeng.facetest.mapper.TestResultMapper;
import com.luofeng.facetest.pojo.testResult;
import com.luofeng.facetest.service.TestResultService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

// 添加iText7相关的导入
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.layout.properties.TextAlignment;

// 添加Excel相关的导入
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class TestResultServiceImpl implements TestResultService {
    
    @Autowired
    private TestResultMapper testResultMapper;
    
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
    @Override
    public PageInfo<testResult> getUserTestResults(Integer userId, Integer page, Integer pageSize,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Boolean isGlandFace, String level) {
        PageHelper.startPage(page, pageSize);
        List<testResult> results = testResultMapper.getUserTestResults(userId, startDate, endDate, isGlandFace, level);
        return new PageInfo<>(results);
    }
    
    /**
     * 用户删除自己的检测结果
     * @param resultId 检测结果ID
     * @param userId 用户ID
     * @return 删除是否成功
     */
    @Override
    public boolean deleteUserTestResult(Integer resultId, Integer userId) {
        return testResultMapper.deleteUserTestResult(resultId, userId) > 0;
    }
    
    /**
     * 用户批量删除检测结果
     * @param ids 检测结果ID列表
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Override
    public int deleteBatchUserTestResults(List<Integer> ids, Integer userId) {
        // 参数检查
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 过滤掉空值
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return 0;
        }
        
        return testResultMapper.deleteBatchUserTestResults(ids, userId);
    }
    
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
    @Override
    public void exportTestResultsToExcel(HttpServletResponse response, Integer userId, LocalDateTime startDate, LocalDateTime endDate,
                                       Boolean isGlandFace, String level) throws Exception {
        // 查询数据
        List<testResult> results = testResultMapper.getTestResultsForExport(userId, startDate, endDate, isGlandFace, level);

        // 创建工作簿和工作表
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("检测记录");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "用户ID", "图片路径", "是否为腺体面容", "等级", "置信度", "可视化描述", "检测时间"};
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        for (int i = 0; i < results.size(); i++) {
            testResult result = results.get(i);
            Row row = sheet.createRow(i + 1);
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(result.getId());
            
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(result.getUserId());
            
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(result.getImagePath());
            
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            cell3.setCellValue(result.getIsGlandFace() != null ? (result.getIsGlandFace() ? "是" : "否") : "");
            
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            cell4.setCellValue(result.getLevel());
            
            org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
            cell5.setCellValue(result.getConfidence() != null ? result.getConfidence() : 0);
            
            org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
            cell6.setCellValue(result.getVisualizationDescription());
            
            org.apache.poi.ss.usermodel.Cell cell7 = row.createCell(7);
            cell7.setCellValue(result.getCreateTime() != null ? result.getCreateTime().toString() : "");
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("检测记录.xlsx", StandardCharsets.UTF_8.toString());
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 写入响应
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
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
    @Override
    public void exportTestResultsToPDF(HttpServletResponse response, Integer userId, LocalDateTime startDate, LocalDateTime endDate,
                                      Boolean isGlandFace, String level) throws Exception {
        // 查询数据
        List<testResult> results = testResultMapper.getTestResultsForExport(userId, startDate, endDate, isGlandFace, level);
        
        // 生成HTML内容
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>");
        htmlContent.append("<html>");
        htmlContent.append("<head>");
        htmlContent.append("<meta charset='UTF-8'>");
        htmlContent.append("<title>检测记录报告</title>");
        htmlContent.append("<style>");
        htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        htmlContent.append("h1 { text-align: center; color: #333; }");
        htmlContent.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        htmlContent.append("th { background-color: #f2f2f2; font-weight: bold; }");
        htmlContent.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        htmlContent.append(".footer { margin-top: 30px; text-align: center; color: #666; font-size: 12px; }");
        htmlContent.append(".image-preview { max-width: 100px; max-height: 100px; }");
        htmlContent.append("</style>");
        htmlContent.append("</head>");
        htmlContent.append("<body>");
        htmlContent.append("<h1>检测记录报告</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<thead>");
        htmlContent.append("<tr>");
        htmlContent.append("<th>ID</th>");
        htmlContent.append("<th>用户ID</th>");
        htmlContent.append("<th>图片</th>");
        htmlContent.append("<th>是否为腺体面容</th>");
        htmlContent.append("<th>等级</th>");
        htmlContent.append("<th>置信度</th>");
        htmlContent.append("<th>可视化描述</th>");
        htmlContent.append("<th>检测时间</th>");
        htmlContent.append("</tr>");
        htmlContent.append("</thead>");
        htmlContent.append("<tbody>");
        
        // 填充数据
        for (testResult result : results) {
            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(result.getId()).append("</td>");
            htmlContent.append("<td>").append(result.getUserId()).append("</td>");
            
            // 处理图片路径，添加图片预览
            String imagePath = result.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                // 如果是相对路径，添加基础URL
                if (!imagePath.startsWith("http")) {
                    imagePath = "https://java-web-ai388.oss-cn-beijing.aliyuncs.com/" + imagePath;
                }
                htmlContent.append("<td><a href='").append(imagePath).append("' target='_blank'><img src='").append(imagePath).append("' class='image-preview' alt='检测图片'/></a></td>");
            } else {
                htmlContent.append("<td></td>");
            }
            
            htmlContent.append("<td>").append(result.getIsGlandFace() != null ? (result.getIsGlandFace() ? "是" : "否") : "").append("</td>");
            htmlContent.append("<td>").append(result.getLevel()).append("</td>");
            htmlContent.append("<td>").append(result.getConfidence() != null ? result.getConfidence() : 0).append("</td>");
            htmlContent.append("<td>").append(result.getVisualizationDescription() != null ? result.getVisualizationDescription() : "").append("</td>");
            htmlContent.append("<td>").append(result.getCreateTime() != null ? result.getCreateTime().toString() : "").append("</td>");
            htmlContent.append("</tr>");
        }
        
        htmlContent.append("</tbody>");
        htmlContent.append("</table>");
        htmlContent.append("<div class='footer'>");
        htmlContent.append("报告生成时间: ").append(LocalDateTime.now().toString());
        htmlContent.append("</div>");
        htmlContent.append("</body>");
        htmlContent.append("</html>");
        
        // 设置响应头
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("检测记录.pdf.html", StandardCharsets.UTF_8.toString());
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        // 输出HTML内容，用户可以通过浏览器另存为PDF
        response.getWriter().write(htmlContent.toString());
    }
}