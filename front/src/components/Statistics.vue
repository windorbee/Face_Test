<template>
  <div class="statistics-container">
    <div class="header">
      <h2><el-icon><PieChart /></el-icon> 检测统计</h2>
    </div>

    <!-- 统计概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6" :xs="12">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #ecf5ff;">
              <el-icon style="color: #409eff;"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalTests }}</div>
              <div class="stat-label">总检测次数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6" :xs="12">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #fef0f0;">
              <el-icon style="color: #f56c6c;"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ glandFaceCount }}</div>
              <div class="stat-label">腺体面容次数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6" :xs="12">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #f0f9ff;">
              <el-icon style="color: #409eff;"><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ nonGlandFaceCount }}</div>
              <div class="stat-label">非腺体面容次数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6" :xs="12">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #fdf6ec;">
              <el-icon style="color: #e6a23c;"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ accuracyRate }}%</div>
              <div class="stat-label">准确率</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12" :xs="24">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <el-icon><TrendCharts /></el-icon>
                <span>检测趋势</span>
              </div>
            </template>
            <div ref="trendChart" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12" :xs="24">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <el-icon><DataAnalysis /></el-icon>
                <span>等级分布</span>
              </div>
            </template>
            <div ref="levelChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
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
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 300px;">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStatistics">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetFilter">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 详细统计表格 -->
    <div class="table-section">
      <el-card class="table-card">
        <template #header>
          <div class="table-header">
            <el-icon><List /></el-icon>
            <span>详细统计</span>
          </div>
        </template>
        <el-table :data="statisticsDetail" border style="width: 100%">
          <el-table-column prop="date" label="日期" align="center"></el-table-column>
          <el-table-column prop="total" label="检测次数" align="center">
            <template #default="scope">
              <el-tag type="primary">{{ scope.row.total }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="glandFace" label="腺体面容" align="center">
            <template #default="scope">
              <el-tag type="danger">{{ scope.row.glandFace }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="nonGlandFace" label="非腺体面容" align="center">
            <template #default="scope">
              <el-tag type="success">{{ scope.row.nonGlandFace }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="light" label="轻度" align="center">
            <template #default="scope">
              <el-tag type="warning">{{ scope.row.light }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="moderate" label="中度" align="center">
            <template #default="scope">
              <el-tag type="warning">{{ scope.row.moderate }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="severe" label="重度" align="center">
            <template #default="scope">
              <el-tag type="warning">{{ scope.row.severe }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, onMounted } from 'vue';
import * as echarts from 'echarts';
import request from '../utils/request';
import {
  PieChart,
  Document,
  Warning,
  Check,
  Star,
  TrendCharts,
  DataAnalysis,
  Filter,
  Search,
  Refresh,
  List
} from '@element-plus/icons-vue';

export default {
  name: 'Statistics',
  components: {
    PieChart,
    Document,
    Warning,
    Check,
    Star,
    TrendCharts,
    DataAnalysis,
    Filter,
    Search,
    Refresh,
    List
  },
  setup() {
    // 统计数据
    const totalTests = ref(0);
    const glandFaceCount = ref(0);
    const nonGlandFaceCount = ref(0);
    const accuracyRate = ref(0);
    const statisticsDetail = ref([]);
    
    // 图表引用
    const trendChart = ref(null);
    const levelChart = ref(null);
    
    // 图表实例
    let trendChartInstance = null;
    let levelChartInstance = null;
    
    // 筛选表单
    const filterForm = ref({
      dateRange: []
    });

    // 加载统计数据
    const loadStatistics = async () => {
      try {
        // 构建请求参数
        const params = {};
        if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
          params.startDate = filterForm.value.dateRange[0];
          params.endDate = filterForm.value.dateRange[1];
        }

        const response = await request.get('/statistics/overview', { params });
        if (response.code === 1) {
          const data = response.data;
          totalTests.value = data.totalTests || 0;
          glandFaceCount.value = data.glandFaceCount || 0;
          nonGlandFaceCount.value = data.nonGlandFaceCount || 0;
          accuracyRate.value = data.accuracyRate ? (data.accuracyRate * 100).toFixed(2) : 0;
          
          // 加载详细统计数据
          await loadDetailStatistics();
          
          // 渲染图表
          await nextTick();
          renderTrendChart(data.trendData || []);
          renderLevelChart(data.levelData || []);
        }
      } catch (error) {
        console.error('获取统计数据失败:', error);
      }
    };

    // 加载详细统计数据
    const loadDetailStatistics = async () => {
      try {
        const params = {};
        if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
          params.startDate = filterForm.value.dateRange[0];
          params.endDate = filterForm.value.dateRange[1];
        }

        const response = await request.get('/statistics/detail', { params });
        if (response.code === 1) {
          statisticsDetail.value = response.data || [];
        }
      } catch (error) {
        console.error('获取详细统计数据失败:', error);
      }
    };

    // 渲染趋势图表
    const renderTrendChart = (data) => {
      if (!trendChart.value) return;
      
      if (!trendChartInstance) {
        trendChartInstance = echarts.init(trendChart.value);
      }
      
      const dates = data.map(item => item.date);
      const totals = data.map(item => item.total);
      const glandFaces = data.map(item => item.glandFace);
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['总检测次数', '腺体面容次数']
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '总检测次数',
            type: 'line',
            data: totals,
            smooth: true
          },
          {
            name: '腺体面容次数',
            type: 'line',
            data: glandFaces,
            smooth: true
          }
        ]
      };
      
      trendChartInstance.setOption(option);
    };

    // 渲染等级分布图表
    const renderLevelChart = (data) => {
      if (!levelChart.value) return;
      
      if (!levelChartInstance) {
        levelChartInstance = echarts.init(levelChart.value);
      }
      
      const levels = data.map(item => item.level);
      const counts = data.map(item => item.count);
      
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          data: levels
        },
        series: [
          {
            type: 'pie',
            radius: '50%',
            data: data.map(item => ({
              name: item.level,
              value: item.count
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      
      levelChartInstance.setOption(option);
    };

    // 重置筛选条件
    const resetFilter = () => {
      filterForm.value.dateRange = [];
      loadStatistics();
    };

    // 窗口大小改变时重绘图表
    const handleResize = () => {
      if (trendChartInstance) {
        trendChartInstance.resize();
      }
      if (levelChartInstance) {
        levelChartInstance.resize();
      }
    };

    // 组件挂载后加载数据
    onMounted(() => {
      loadStatistics();
      window.addEventListener('resize', handleResize);
    });

    return {
      totalTests,
      glandFaceCount,
      nonGlandFaceCount,
      accuracyRate,
      statisticsDetail,
      trendChart,
      levelChart,
      filterForm,
      loadStatistics,
      resetFilter
    };
  }
};
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  min-height: 100%;
  box-sizing: border-box;
}

.header {
  margin: 0 0 20px 0;
  padding: 25px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.header h2 {
  margin: 0;
  color: white;
  font-size: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.overview-section {
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 25px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  flex-shrink: 0;
}

.stat-icon .el-icon {
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  font-family: 'Arial', sans-serif;
}

.stat-label {
  font-size: 15px;
  color: #666;
  font-weight: 500;
}

.charts-section {
  margin-bottom: 30px;
}

.chart-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.chart-header {
  font-weight: 600;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.filter-section {
  margin-bottom: 30px;
}

.filter-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
}

.filter-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.filter-header {
  font-weight: 600;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.table-section {
  margin-bottom: 20px;
}

.table-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
}

.table-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.table-header {
  font-weight: 600;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}

:deep(.el-card__header) {
  padding: 20px 25px;
  background-color: #f9fafc;
  border-bottom: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .statistics-container {
    padding: 15px;
  }
  
  .header {
    padding: 20px;
    border-radius: 12px;
  }
  
  .header h2 {
    font-size: 20px;
  }
  
  .el-col {
    margin-bottom: 20px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    margin-right: 15px;
  }
  
  .stat-icon .el-icon {
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 280px;
  }
  
  .filter-form {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-card,
  .filter-card,
  .table-card {
    border-radius: 12px;
  }
  
  .chart-header,
  .filter-header,
  .table-header {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .overview-section .el-col {
    width: 100%;
  }
  
  .charts-section .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-value {
    font-size: 20px;
    margin-bottom: 5px;
  }
  
  .stat-label {
    font-size: 14px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>