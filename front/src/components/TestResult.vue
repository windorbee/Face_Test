<template>
  <div class="test-result-container">
    <div class="header">
      <h2 class="section-header"><el-icon><Document /></el-icon> 检测记录</h2>
      <div class="header-buttons">
        <el-button 
          type="danger" 
          :disabled="selectedResults.length === 0"
          @click="deleteSelectedResults"
          v-if="resultList.length > 0"
          size="large"
        >
          <el-icon><Delete /></el-icon> 批量删除 ({{ selectedResults.length }})
        </el-button>
        <el-button type="primary" @click="downloadResults" :loading="downloading" size="large">
          <el-icon><Download /></el-icon> 下载记录 (Excel)
        </el-button>
        <el-button type="success" @click="downloadResultsAsPDF" :loading="downloadingPDF" size="large">
          <el-icon><Document /></el-icon> 下载记录 (PDF)
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-card class="filter-card">
        <template #header>
          <div class="filter-header">
            <el-icon><Filter /></el-icon>
            <span>筛选条件</span>
          </div>
        </template>
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="filterForm.startDate"
              type="datetime"
              placeholder="选择开始日期"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 220px;">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="filterForm.endDate"
              type="datetime"
              placeholder="选择结束日期"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 220px;">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="腺体面容">
            <el-select v-model="filterForm.isGlandFace" clearable placeholder="请选择" style="width: 120px;">
              <el-option label="是" :value="true"></el-option>
              <el-option label="否" :value="false"></el-option>
            </el-select>
          </el-form-item>
          <!-- 等级筛选选项，与数据库值匹配 -->
          <el-form-item label="等级">
            <el-select v-model="filterForm.level" clearable placeholder="请选择等级" style="width: 140px;">
              <el-option label="轻度" value="轻度"></el-option>
              <el-option label="中度" value="中度"></el-option>
              <el-option label="重度" value="重度"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchResults" size="large">
              <el-icon><Search /></el-icon> 查询
            </el-button>
            <el-button @click="resetFilter" size="large">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
            <el-button type="success" :loading="downloading" @click="downloadResults" size="large">
              <el-icon><Download /></el-icon> 检索条件下载
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 检测记录表格 -->
    <div class="table-section">
      <el-card class="table-card">
        <template #header>
          <div class="table-header">
            <el-icon><List /></el-icon>
            <span>检测记录列表</span>
            <div class="table-info">
              共 {{ total }} 条记录
            </div>
          </div>
        </template>
        <el-table 
          :data="resultList" 
          v-loading="loading" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
          stripe
          highlight-current-row
          :header-cell-style="{ background: '#f5f7fa', color: '#333', fontWeight: '600' }"
          class="result-table"
          :resizable="true"
        >
          <el-table-column type="selection" min-width="50" align="center"></el-table-column>
          <el-table-column label="序号" min-width="60" align="center">
            <template #default="scope">
              <span class="table-cell-id">{{ (currentPage - 1) * pageSize + scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="检测图片" min-width="120" align="center">
            <template #default="scope">
              <el-image 
                :src="getImageUrl(scope.row.imagePath)" 
                fit="cover"
                class="result-image"
                :preview-src-list="[getImageUrl(scope.row.imagePath)]"
                preview-teleported
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="isGlandFace" label="腺体面容" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isGlandFace ? 'danger' : 'success'" round size="large">
                {{ scope.row.isGlandFace ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="level" label="等级" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="getLevelType(scope.row.level)" round size="large">
                {{ scope.row.level }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="confidence" label="置信度" min-width="100" align="center">
            <template #default="scope">
              <span v-if="scope.row.confidence !== null" :class="getConfidenceClass(scope.row.confidence)">
                {{ (scope.row.confidence * 100).toFixed(2) }}%
              </span>
              <span v-else>N/A</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="检测时间" min-width="180" align="center" sortable>
            <template #default="scope">
              <span class="time-text">{{ scope.row.createTime ? scope.row.createTime : scope.row.testTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" min-width="100" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-button size="small" type="danger" @click="deleteResult(scope.row.id)" circle>
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import request, { downloadRequest } from '../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Document,
  Delete,
  Download,
  Filter,
  Search,
  Refresh,
  List,
  Picture
} from '@element-plus/icons-vue';

export default {
  name: 'TestResult',
  components: {
    Document,
    Delete,
    Download,
    Filter,
    Search,
    Refresh,
    List,
    Picture
  },
  setup() {
    // 数据响应式定义
    const resultList = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);
    const selectedResults = ref([]);
    const downloading = ref(false);
    const downloadingPDF = ref(false); // 添加PDF下载状态
    const loading = ref(false); // 添加loading状态
    
    // 筛选表单
    const filterForm = ref({
      startDate: '',
      endDate: '',
      isGlandFace: null,
      level: ''
    });

    // 获取检测记录列表
    const getResultList = async () => {
      loading.value = true;
      try {
        // 构建请求参数
        const params = {
          page: currentPage.value,
          pageSize: pageSize.value
        };
        
        // 只添加非空的筛选条件
        if (filterForm.value.startDate) {
          params.startDate = filterForm.value.startDate;
        }
        if (filterForm.value.endDate) {
          params.endDate = filterForm.value.endDate;
        }
        if (filterForm.value.isGlandFace !== null) {
          params.isGlandFace = filterForm.value.isGlandFace;
        }
        if (filterForm.value.level) {
          params.level = filterForm.value.level;
        }

        const response = await request.get('/testResult/result', { params });
        console.log('检测记录响应:', response); // 调试信息
        if (response.code === 1) {
          resultList.value = response.data.list || [];
          total.value = response.data.total || 0;
        } else {
          ElMessage.error(response.msg || '获取检测记录失败');
        }
      } catch (error) {
        console.error('获取检测记录失败:', error);
        ElMessage.error('获取检测记录失败');
      } finally {
        loading.value = false;
      }
    };

    // 处理表格选择变化
    const handleSelectionChange = (selection) => {
      selectedResults.value = selection;
    };

    // 批量删除检测记录
    const deleteSelectedResults = () => {
      if (selectedResults.value.length === 0) {
        ElMessage.warning('请先选择要删除的记录');
        return;
      }

      ElMessageBox.confirm(
        `确定要删除选中的 ${selectedResults.value.length} 条检测记录吗？此操作不可恢复。`,
        '批量删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          // 提取选中记录的ID
          const ids = selectedResults.value.map(item => item.id);
          
          // 调用批量删除接口 - 修复参数格式，直接发送ID数组
          const response = await request.delete('/testResult/batch', {
            data: ids  // 直接发送ID数组，而不是{ ids }对象
          });

          if (response && response.code === 1) {
            ElMessage.success('批量删除成功');
            // 清空选中项
            selectedResults.value = [];
            // 重新加载数据
            await getResultList();
          } else {
            ElMessage.error(response.msg || '批量删除失败');
          }
        } catch (error) {
          console.error('批量删除检测记录失败:', error);
          ElMessage.error('批量删除检测记录失败: ' + (error.message || '未知错误'));
        }
      }).catch(() => {
        // 用户取消删除
      });
    };

    // 删除单条检测记录
    const deleteResult = (id) => {
      ElMessageBox.confirm('确定要删除这条检测记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await request.delete(`/testResult/${id}`);
          // 检查响应结构
          if (response && response.code === 1) {
            ElMessage.success('删除成功');
            // 重新加载数据
            await getResultList();
          } else {
            ElMessage.error(response.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除检测记录失败:', error);
          ElMessage.error('删除检测记录失败: ' + (error.message || '未知错误'));
        }
      }).catch(() => {
        // 用户取消删除
      });
    };

    // 下载检测记录
    const downloadResults = async () => {
      downloading.value = true;
      try {
        // 构建下载参数
        const params = {};
        if (filterForm.value.startDate) params.startDate = filterForm.value.startDate;
        if (filterForm.value.endDate) params.endDate = filterForm.value.endDate;
        if (filterForm.value.isGlandFace !== null) params.isGlandFace = filterForm.value.isGlandFace;
        if (filterForm.value.level) params.level = filterForm.value.level;
        
        // 发送下载请求
        const response = await downloadRequest.get('/testResult/download', { params });
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `检测记录_${new Date().getTime()}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        ElMessage.success('下载成功');
      } catch (error) {
        console.error('下载检测记录失败:', error);
        ElMessage.error('下载检测记录失败');
      } finally {
        downloading.value = false;
      }
    };
    
    // 下载PDF格式检测记录
    const downloadResultsAsPDF = async () => {
      downloadingPDF.value = true;
      try {
        // 构建下载参数
        const params = {};
        if (filterForm.value.startDate) params.startDate = filterForm.value.startDate;
        if (filterForm.value.endDate) params.endDate = filterForm.value.endDate;
        if (filterForm.value.isGlandFace !== null) params.isGlandFace = filterForm.value.isGlandFace;
        if (filterForm.value.level) params.level = filterForm.value.level;
        
        // 发送PDF下载请求
        const response = await downloadRequest.get('/testResult/download/pdf', { params });
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'text/html' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `检测记录_${new Date().getTime()}.pdf.html`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        ElMessage.success('PDF下载成功');
      } catch (error) {
        console.error('下载PDF检测记录失败:', error);
        ElMessage.error('下载PDF检测记录失败');
      } finally {
        downloadingPDF.value = false;
      }
    };

    // 查询
    const searchResults = () => {
      currentPage.value = 1;
      getResultList();
    };

    // 重置筛选条件
    const resetFilter = () => {
      filterForm.value = {
        startDate: '',
        endDate: '',
        isGlandFace: null,
        level: ''
      };
      currentPage.value = 1;
      getResultList();
    };

    // 分页相关方法
    const handleSizeChange = (val) => {
      pageSize.value = val;
      currentPage.value = 1;
      getResultList();
    };

    const handleCurrentChange = (val) => {
      currentPage.value = val;
      getResultList();
    };

    // 获取等级标签类型
    const getLevelType = (level) => {
      // 处理数据库中的中文字符
      if (level && (level.includes('轻') || level.includes('轻微'))) {
        return 'success';
      } else if (level && (level.includes('中') || level.includes('等'))) {
        return 'warning';
      } else if (level && level.includes('重')) {
        return 'danger';
      } else {
        return 'info';
      }
    };
    
    // 获取置信度样式类
    const getConfidenceClass = (confidence) => {
      if (confidence > 0.9) {
        return 'confidence-high';
      } else if (confidence > 0.7) {
        return 'confidence-medium';
      } else {
        return 'confidence-low';
      }
    };

    // 获取完整的图片URL
    const getImageUrl = (imagePath) => {
      if (!imagePath) return '';
      
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http')) {
        return imagePath;
      }
      
      // 否则拼接OSS基础URL
      const baseUrl = 'https://java-web-ai388.oss-cn-beijing.aliyuncs.com/';
      return baseUrl + imagePath;
    };

    // 组件挂载后获取数据
    onMounted(() => {
      getResultList();
    });

    return {
      resultList,
      loading,
      downloading,
      downloadingPDF,
      currentPage,
      pageSize,
      total,
      filterForm,
      selectedResults,
      getResultList,
      handleSelectionChange,
      deleteSelectedResults,
      deleteResult,
      downloadResults,
      downloadResultsAsPDF,
      searchResults,
      resetFilter,
      handleSizeChange,
      handleCurrentChange,
      getLevelType,
      getConfidenceClass,
      getImageUrl
    };
  }
};
</script>

<style scoped>
.test-result-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  min-height: 100%;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 0 20px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 25px 30px;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  width: 100%;
  box-sizing: border-box;
}


.header-buttons {
  display: flex;
  gap: 15px;
}

.header h2 {
  margin: 0;
  color: white;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-header {
  margin: 0;
  line-height: 1.2;
}

.filter-section {
  margin: 0 0 30px 0;
}

.filter-card {
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
}

.filter-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  transform: translateY(-3px);
}

.filter-header {
  font-weight: 600;
  color: #333;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  padding: 25px;
}

.table-section {
  margin-bottom: 30px;
}

.table-card {
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
}

.table-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  transform: translateY(-3px);
}

.table-header {
  font-weight: 600;
  color: #333;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-info {
  margin-left: auto;
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.result-image {
  width: 110px;
  height: 110px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  object-fit: cover;
  border: 3px solid #fff;
}

.result-image:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa, #e4e7f1);
  color: #909399;
  font-size: 32px;
  border-radius: 16px;
}

.table-cell-id {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

.time-text {
  color: #666;
  font-size: 14px;
}

.confidence-high {
  color: #67C23A;
  font-weight: bold;
  font-size: 16px;
}

.confidence-medium {
  color: #E6A23C;
  font-weight: bold;
  font-size: 16px;
}

.confidence-low {
  color: #F56C6C;
  font-weight: bold;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  justify-content: center;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 30px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #333;
}

:deep(.el-table .el-table__cell) {
  padding: 15px 0;
}

:deep(.el-card__header) {
  padding: 25px 30px;
  background-color: #f9fafc;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-pagination) {
  padding: 0;
}

:deep(.el-button) {
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-tag) {
  font-size: 14px;
  padding: 10px 15px;
  border: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .test-result-container {
    padding: 15px;
  }
  
  .header {
    flex-direction: column;
    gap: 20px;
    align-items: stretch;
    padding: 20px;
    border-radius: 16px;
  }
  
  .header-buttons {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .filter-form {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
    padding: 20px;
  }
  
  .filter-form .el-form-item {
    margin-bottom: 0;
  }
  
  .filter-form .el-form-item__content {
    display: flex;
    justify-content: center;
  }
  
  .table-section {
    margin-bottom: 20px;
  }
  
  .table-card {
    border-radius: 16px;
  }
  
  .table-header {
    font-size: 18px;
  }
  
  .filter-header {
    font-size: 18px;
  }
  
  .table-info {
    font-size: 14px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th) {
    padding: 12px 0;
  }
  
  :deep(.el-table td) {
    padding: 12px 0;
  }
  
  :deep(.el-table .el-button) {
    font-size: 12px;
    padding: 6px 10px;
  }
  
  .result-image {
    width: 80px;
    height: 80px;
    border-radius: 12px;
  }
  
  :deep(.el-tag) {
    font-size: 12px;
    padding: 6px 10px;
  }
  
  .table-cell-id {
    font-size: 14px;
  }
  
  .confidence-high,
  .confidence-medium,
  .confidence-low {
    font-size: 14px;
  }
  
  .pagination-section {
    padding: 20px;
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .header-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .header-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .filter-form {
    gap: 15px;
  }
  
  .result-image {
    width: 70px;
    height: 70px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 10px 0;
  }
  
  .table-header {
    font-size: 16px;
  }
  
  .filter-header {
    font-size: 16px;
  }
}
</style>