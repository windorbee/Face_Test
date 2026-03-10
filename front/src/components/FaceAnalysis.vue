<template>
  <div class="face-analysis-container">

    <div class="header">
      <h2 class="section-header"><el-icon><Camera /></el-icon> 腺体面容分析</h2>
      <p class="header-subtitle">上传面部照片进行腺体面容检测与分析</p>
    </div>
    
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <h3><el-icon><Upload /></el-icon> 图片上传</h3>
        </div>
      </template>
      
      <div class="upload-section">
        <!-- 摄像头预览区域 -->
        <div v-if="isCameraActive" class="camera-preview">
          <video ref="videoElement" autoplay playsinline class="video-preview"></video>
          <canvas ref="canvasElement" style="display: none;"></canvas>
          <div class="camera-overlay">
            <div class="camera-instructions">
              <el-icon class="capture-icon"><Camera /></el-icon>
              <p>请将面部对准摄像头</p>
            </div>
          </div>
        </div>
        
        <!-- 文件上传区域 -->
        <el-upload
          v-else
          class="upload-area"
          drag
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileChange"
          :multiple="true"
          accept="image/*"
        >
          <div v-if="!selectedFiles.length" class="upload-placeholder">
            <el-icon class="el-icon--upload upload-icon">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">
              将图片拖到此处，或 <em>点击上传</em>
            </div>
            <div class="el-upload__tip">
              <p>支持 JPG、PNG 格式图片，可多选</p>
              <p class="file-limit">最多可上传9张图片</p>
            </div>
          </div>
          <div v-else class="preview-container">
            <div class="multi-preview">
              <div 
                v-for="(file, index) in selectedFiles" 
                :key="index" 
                class="preview-item"
              >
                <img :src="file.previewUrl" :alt="'预览图片' + (index + 1)" class="preview-image-small" />
                <p class="file-name">{{ file.name }}</p>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="removeFile(index)"
                  circle
                  class="remove-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-upload>
        
        <div class="action-buttons">
          <el-button 
            v-if="!isCameraActive"
            type="primary" 
            @click="startCamera"
            size="large"
            class="action-button"
          >
            <el-icon><Camera /></el-icon> 拍照检测
          </el-button>
          
          <template v-else>
            <el-button 
              type="primary" 
              @click="captureImage"
              size="large"
              class="action-button"
            >
              <el-icon><CameraFilled /></el-icon> 拍摄
            </el-button>
            
            <el-button 
              @click="stopCamera"
              size="large"
              class="action-button secondary"
            >
              <el-icon><Close /></el-icon> 取消
            </el-button>
          </template>
          
          <el-button 
            v-if="selectedFiles.length && !isCameraActive"
            type="primary" 
            :disabled="loading"
            :loading="loading"
            @click="analyzeFaces"
            size="large"
            class="action-button"
          >
            <el-icon><Search /></el-icon> {{ loading ? '分析中...' : '开始分析' }}
          </el-button>
          
          <el-button 
            v-if="selectedFiles.length && !isCameraActive"
            @click="clearFiles"
            size="large"
            class="action-button secondary"
          >
            <el-icon><Refresh /></el-icon> 重新选择
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 对比结果弹窗 -->
    <el-dialog
      v-model="compareDialogVisible"
      title="对比分析结果"
      width="95%"
      class="compare-dialog"
      :fullscreen="isMobile"
    >
      <div v-if="compareResults.length" class="compare-results">
        <el-row :gutter="24">
          <el-col 
            v-for="(result, index) in compareResults" 
            :key="index" 
            :span="24 / Math.min(compareResults.length, 4)"
            class="result-col"
          >
            <el-card class="result-card">
              <template #header>
                <div class="result-header">
                  <h4><el-icon><Picture /></el-icon> 图片 {{ index + 1 }}</h4>
                </div>
              </template>
              
              <div class="result-content">
                <div class="result-image">
                  <el-image 
                    :src="result.imagePath" 
                    fit="cover"
                    style="width: 100%; height: 200px; border-radius: 12px;"
                    :preview-src-list="[result.imagePath]"
                    preview-teleported
                  >
                    <template #error>
                      <div class="image-slot">
                        <el-icon><PictureRounded /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                
                <div class="result-details">
                  <div class="result-item">
                    <span class="label"><el-icon><Timer /></el-icon> 检测时间:</span>
                    <span class="value">{{ formatDateTime(result.testTime) }}</span>
                  </div>
                  
                  <div class="result-item">
                    <span class="label"><el-icon><User /></el-icon> 腺体面容:</span>
                    <el-tag :type="result.isGlandFace ? 'danger' : 'success'" round size="large">
                      {{ result.isGlandFace ? '是' : '否' }}
                    </el-tag>
                  </div>
                  
                  <div class="result-item">
                    <span class="label"><el-icon><DataAnalysis /></el-icon> 等级:</span>
                    <el-tag :type="getLevelType(result.level)" round size="large">
                      {{ result.level }}
                    </el-tag>
                  </div>
                  
                  <div class="result-item">
                    <span class="label"><el-icon><TrendCharts /></el-icon> 置信度:</span>
                    <span class="value confidence">{{ (result.confidence * 100).toFixed(2) }}%</span>
                  </div>
                  
                  <div class="result-item">
                    <span class="label"><el-icon><Document /></el-icon> 描述:</span>
                    <span class="value description">{{ result.visualizationDescription }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 对比总结 -->
        <el-card class="summary-card" v-if="compareResults.length > 1">
          <template #header>
            <div class="summary-header">
              <h4><el-icon><DataBoard /></el-icon> 对比总结</h4>
            </div>
          </template>
          
          <div class="summary-content">
            <el-row :gutter="24">
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label"><el-icon><Collection /></el-icon> 总检测数</div>
                  <div class="summary-value">{{ compareResults.length }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label"><el-icon><Warning /></el-icon> 腺体面容数</div>
                  <div class="summary-value">{{ compareResults.filter(r => r.isGlandFace).length }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label"><el-icon><PieChart /></el-icon> 平均置信度</div>
                  <div class="summary-value">
                    {{ (compareResults.reduce((sum, r) => sum + r.confidence, 0) / compareResults.length * 100).toFixed(2) }}%
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>
      
      <div v-else class="no-results">
        <el-empty description="暂无分析结果">
          <el-button type="primary" @click="compareDialogVisible = false">返回</el-button>
        </el-empty>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="compareDialogVisible = false" size="large">
            <el-icon><Close /></el-icon> 关闭
          </el-button>
          <el-button type="primary" @click="exportCompareResults" size="large">
            <el-icon><Download /></el-icon> 导出对比报告
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 单张图片分析结果弹窗 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="分析结果"
      width="550px"
      :before-close="handleDialogClose"
      class="single-result-dialog"
    >
      <div v-if="analysisResult" class="dialog-result-content">
        <div class="result-summary">
          <div class="result-tags">
            <el-tag 
              :type="analysisResult.isGlandFace ? 'danger' : 'success'" 
              size="large"
              class="result-tag"
            >
              {{ analysisResult.isGlandFace ? '腺体面容' : '非腺体面容' }}
            </el-tag>
            <el-tag 
              :type="getLevelType(analysisResult.level)"
              size="large"
              class="result-tag"
            >
              {{ analysisResult.level }}
            </el-tag>
          </div>
          
          <div class="result-details">
            <el-descriptions :column="1" border size="large">
              <el-descriptions-item label="检测时间">
                <span class="detail-value"><el-icon><Timer /></el-icon> {{ formatDateTime(analysisResult.testTime) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="置信度">
                <span class="detail-value confidence"><el-icon><TrendCharts /></el-icon> {{ (analysisResult.confidence * 100).toFixed(2) }}%</span>
              </el-descriptions-item>
              <el-descriptions-item label="可视化描述">
                <span class="detail-value description"><el-icon><Document /></el-icon> {{ analysisResult.visualizationDescription }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        
        <div class="result-image">
          <h4 class="image-title"><el-icon><Picture /></el-icon> 分析图像</h4>
          <el-image 
            :src="analysisResult.imagePath" 
            :preview-src-list="[analysisResult.imagePath]"
            fit="contain"
            class="result-image-preview"
            style="max-width: 350px; max-height: 350px; border-radius: 12px;"
          />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resultDialogVisible = false" size="large">
            <el-icon><Close /></el-icon> 关闭
          </el-button>
          <el-button type="primary" @click="exportToPDF" size="large">
            <el-icon><Download /></el-icon> 导出PDF报告
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      closable
      @close="error = ''"
      class="error-alert"
    />
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import request, { downloadRequest } from '../utils/request';
import { ElMessage, ElLoading } from 'element-plus';
import {
  Camera,
  Upload,
  UploadFilled,
  Delete,
  CameraFilled,
  Close,
  Search,
  Refresh,
  Picture,
  PictureRounded,
  Timer,
  User,
  DataAnalysis,
  TrendCharts,
  Document,
  DataBoard,
  Collection,
  Warning,
  PieChart,
  Download
} from '@element-plus/icons-vue';

export default {
  name: 'FaceAnalysis',
  components: {
    Camera,
    Upload,
    UploadFilled,
    Delete,
    CameraFilled,
    Close,
    Search,
    Refresh,
    Picture,
    PictureRounded,
    Timer,
    User,
    DataAnalysis,
    TrendCharts,
    Document,
    DataBoard,
    Collection,
    Warning,
    PieChart,
    Download
  },
  setup() {
    const selectedFiles = ref([]); // 存储多个选中的文件
    const loading = ref(false);
    const analysisResult = ref(null);
    const compareResults = ref([]); // 存储对比结果
    const error = ref('');
    const resultDialogVisible = ref(false);
    const compareDialogVisible = ref(false); // 对比结果弹窗
    const isCameraActive = ref(false);
    const videoElement = ref(null);
    const canvasElement = ref(null);
    const isMobile = ref(false);
    let stream = null;

    // 检测是否为移动设备
    const checkIsMobile = () => {
      isMobile.value = window.innerWidth <= 768;
    };

    onMounted(() => {
      checkIsMobile();
      window.addEventListener('resize', checkIsMobile);
    });

    onUnmounted(() => {
      window.removeEventListener('resize', checkIsMobile);
      stopCamera();
    });

    const handleFileChange = (file, fileList) => {
      // 处理多个文件
      const newFiles = fileList.map(f => {
        return {
          raw: f.raw,
          name: f.name,
          previewUrl: URL.createObjectURL(f.raw)
        };
      });
      
      selectedFiles.value = newFiles;
      error.value = '';
    };

    const removeFile = (index) => {
      const file = selectedFiles.value[index];
      if (file && file.previewUrl) {
        URL.revokeObjectURL(file.previewUrl);
      }
      selectedFiles.value.splice(index, 1);
    };

    const clearFiles = () => {
      // 清理预览URL
      selectedFiles.value.forEach(file => {
        if (file.previewUrl) {
          URL.revokeObjectURL(file.previewUrl);
        }
      });
      selectedFiles.value = [];
      analysisResult.value = null;
      compareResults.value = [];
    };

    // 启动摄像头
    const startCamera = async () => {
      try {
        clearFiles();
        isCameraActive.value = true;
        error.value = '';
        
        stream = await navigator.mediaDevices.getUserMedia({ 
          video: { 
            facingMode: 'user',
            width: { ideal: 1280 },
            height: { ideal: 720 }
          }, 
          audio: false 
        });
        
        if (videoElement.value) {
          videoElement.value.srcObject = stream;
        }
      } catch (err) {
        console.error('无法访问摄像头:', err);
        error.value = '无法访问摄像头，请检查权限设置';
        isCameraActive.value = false;
      }
    };

    // 拍摄照片
    const captureImage = () => {
      if (!videoElement.value || !canvasElement.value) return;
      
      const video = videoElement.value;
      const canvas = canvasElement.value;
      const context = canvas.getContext('2d');
      
      // 设置canvas尺寸与视频相同
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      
      // 绘制当前视频帧到canvas
      context.drawImage(video, 0, 0, canvas.width, canvas.height);
      
      // 将canvas转换为Blob并创建文件对象
      canvas.toBlob((blob) => {
        if (blob) {
          const file = new File([blob], 'camera-capture.jpg', { type: 'image/jpeg' });
          selectedFiles.value = [{
            raw: file,
            name: 'camera-capture.jpg',
            previewUrl: URL.createObjectURL(file)
          }];
          stopCamera();
        }
      }, 'image/jpeg', 0.95);
    };

    // 停止摄像头
    const stopCamera = () => {
      if (stream) {
        const tracks = stream.getTracks();
        tracks.forEach(track => track.stop());
        stream = null;
      }
      isCameraActive.value = false;
    };

    const formatDateTime = (dateString) => {
      if (!dateString) return '';
      // 处理不同的时间字段
      const date = new Date(dateString);
      // 检查是否是有效日期
      if (isNaN(date.getTime())) {
        return '';
      }
      return date.toLocaleString('zh-CN');
    };
    
    const getLevelType = (level) => {
      // 处理不同的等级值
      if (level && (level.includes('轻') || level.includes('微'))) {
        return 'success';
      } else if (level && (level.includes('中') || level.includes('等'))) {
        return 'warning';
      } else if (level && level.includes('重')) {
        return 'danger';
      } else {
        return 'info';
      }
    };

    // 分析多张图片
    const analyzeFaces = async () => {
      if (!selectedFiles.value.length) {
        error.value = '请先选择图片';
        return;
      }

      loading.value = true;
      error.value = '';
      compareResults.value = [];

      try {
        // 如果只有一张图片，使用原来的单张分析逻辑
        if (selectedFiles.value.length === 1) {
          await analyzeSingleFace(selectedFiles.value[0].raw);
          return;
        }

        // 多张图片分析
        const results = [];
        
        // 逐个分析每张图片
        for (const file of selectedFiles.value) {
          try {
            const formData = new FormData();
            formData.append('image', file.raw);
            
            const response = await request.post('/doubao/analyzeFace', formData);
            
            if (response && response.code === 1) {
              results.push(response.data);
            } else {
              console.warn(`图片 ${file.name} 分析失败:`, response?.msg || '未知错误');
              ElMessage.warning(`图片 ${file.name} 分析失败: ${response?.msg || '未知错误'}`);
            }
          } catch (err) {
            console.error(`分析图片 ${file.name} 失败:`, err);
            ElMessage.error(`分析图片 ${file.name} 失败: ${err.message || '未知错误'}`);
          }
        }
        
        if (results.length > 0) {
          compareResults.value = results;
          compareDialogVisible.value = true;
        } else {
          error.value = '所有图片分析都失败了';
        }
      } catch (err) {
        console.error('分析请求失败，错误详情:', err);
        if (err.response) {
          // 服务器返回了错误响应
          console.log('服务器响应错误，状态码:', err.response.status);
          console.log('响应数据:', err.response.data);
          if (err.response.status === 401) {
            error.value = '登录已过期，请重新登录';
          } else if (err.response.data) {
            error.value = err.response.data.msg || `分析请求失败，状态码: ${err.response.status}`;
          } else {
            error.value = `分析请求失败，状态码: ${err.response.status}`;
          }
        } else if (err.request) {
          // 请求已发出但没有收到响应
          console.log('网络请求无响应:', err.request);
          error.value = '网络连接异常，请检查网络设置';
        } else {
          // 其他错误
          console.log('其他错误:', err.message);
          error.value = '分析请求失败: ' + (err.message || '未知错误');
        }
      } finally {
        loading.value = false;
      }
    };

    // 分析单张图片（保持原有逻辑）
    const analyzeSingleFace = async (file) => {
      try {
        const formData = new FormData();
        formData.append('image', file);
        
        console.log('发送分析请求...');
        const response = await request.post('/doubao/analyzeFace', formData);

        console.log('API完整响应:', response);
        if (response && response.code === 1) {
          console.log('分析成功，数据:', response.data);
          analysisResult.value = response.data;
          error.value = '';
          resultDialogVisible.value = true;
        } else {
          const errorMsg = (response && response.msg) ? response.msg : '分析失败';
          console.log('分析失败:', errorMsg);
          error.value = errorMsg;
        }
      } catch (err) {
        console.error('分析请求失败，错误详情:', err);
        if (err.response) {
          console.log('服务器响应错误，状态码:', err.response.status);
          console.log('响应数据:', err.response.data);
          if (err.response.status === 401) {
            error.value = '登录已过期，请重新登录';
          } else if (err.response.data) {
            error.value = err.response.data.msg || `分析请求失败，状态码: ${err.response.status}`;
          } else {
            error.value = `分析请求失败，状态码: ${err.response.status}`;
          }
        } else if (err.request) {
          console.log('网络错误，无响应返回:', err.request);
          error.value = '网络错误，请检查网络连接';
        } else {
          console.log('请求配置错误:', err.message);
          error.value = `请求错误: ${err.message}`;
        }
      } finally {
        loading.value = false;
      }
    };

    const handleDialogClose = (done) => {
      analysisResult.value = null;
      done();
    };

    // 导出单个分析结果为PDF
    const exportToPDF = () => {
      if (!analysisResult.value) {
        ElMessage.error('没有可导出的分析结果');
        return;
      }

      // 创建一个临时的用于打印的元素
      const printContent = document.createElement('div');
      printContent.style.padding = '20px';
      printContent.style.fontFamily = 'Arial, sans-serif';
      printContent.innerHTML = `
        <div style="text-align: center; margin-bottom: 20px;">
          <h2>腺体面容检测报告</h2>
        </div>
        <div style="margin-bottom: 20px;">
          <div style="display: flex; justify-content: center; gap: 15px; margin-bottom: 20px;">
            <span style="padding: 10px 20px; border-radius: 4px; font-weight: bold; background-color: ${analysisResult.value.isGlandFace ? '#fef0f0' : '#f0f9ff'}; color: ${analysisResult.value.isGlandFace ? '#f56c6c' : '#409eff'}">
              ${analysisResult.value.isGlandFace ? '腺体面容' : '非腺体面容'}
            </span>
            <span style="padding: 10px 20px; border-radius: 4px; font-weight: bold; background-color: ${getLevelType(analysisResult.value.level) === 'primary' ? '#ecf5ff' : getLevelType(analysisResult.value.level) === 'warning' ? '#fdf6ec' : getLevelType(analysisResult.value.level) === 'danger' ? '#fef0f0' : '#f4f4f5'}; color: ${getLevelType(analysisResult.value.level) === 'primary' ? '#409eff' : getLevelType(analysisResult.value.level) === 'warning' ? '#e6a23c' : getLevelType(analysisResult.value.level) === 'danger' ? '#f56c6c' : '#909399'}">
              ${analysisResult.value.level}
            </span>
          </div>
          
          <div style="margin-bottom: 20px;">
            <table style="width: 100%; border-collapse: collapse;">
              <tr>
                <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">检测时间</td>
                <td style="border: 1px solid #ddd; padding: 12px;">${formatDateTime(analysisResult.value.testTime)}</td>
              </tr>
              <tr>
                <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">置信度</td>
                <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold; color: #409eff;">${(analysisResult.value.confidence * 100).toFixed(2)}%</td>
              </tr>
              <tr>
                <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">可视化描述</td>
                <td style="border: 1px solid #ddd; padding: 12px;">${analysisResult.value.visualizationDescription}</td>
              </tr>
            </table>
          </div>
          
          <div style="text-align: center;">
            <h4 style="margin-bottom: 15px;">分析图像</h4>
            <img src="${analysisResult.value.imagePath}" style="max-width: 300px; max-height: 300px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);" />
          </div>
        </div>
        <div style="margin-top: 30px; text-align: center; color: #999; font-size: 12px;">
          报告生成时间: ${new Date().toLocaleString('zh-CN')}
        </div>
      `;

      // 创建打印窗口
      const printWindow = window.open('', '_blank');
      printWindow.document.write(`
        <html>
          <head>
            <title>腺体面容检测报告</title>
            <style>
              @media print {
                body {
                  -webkit-print-color-adjust: exact;
                  print-color-adjust: exact;
                }
              }
            </style>
          </head>
          <body>
            ${printContent.innerHTML}
            <script>
              window.onload = function() {
                window.print();
                window.close();
              }
            <\/script>
          </body>
        </html>
      `);
      printWindow.document.close();
    };

    // 导出对比结果
    const exportCompareResults = () => {
      if (!compareResults.value.length) {
        ElMessage.error('没有可导出的分析结果');
        return;
      }

      // 创建一个临时的用于打印的元素
      const printContent = document.createElement('div');
      printContent.style.padding = '20px';
      printContent.style.fontFamily = 'Arial, sans-serif';
      
      let resultsHTML = '';
      compareResults.value.forEach((result, index) => {
        resultsHTML += `
          <div style="margin-bottom: 30px; page-break-inside: avoid;">
            <h3 style="text-align: center; margin-bottom: 20px;">图片 ${index + 1}</h3>
            <div style="display: flex; gap: 20px; flex-wrap: wrap;">
              <div style="flex: 1; min-width: 300px;">
                <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">检测时间</td>
                    <td style="border: 1px solid #ddd; padding: 12px;">${formatDateTime(result.testTime)}</td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">腺体面容</td>
                    <td style="border: 1px solid #ddd; padding: 12px;">
                      <span style="padding: 4px 8px; border-radius: 4px; background-color: ${result.isGlandFace ? '#fef0f0' : '#f0f9ff'}; color: ${result.isGlandFace ? '#f56c6c' : '#409eff'}">
                        ${result.isGlandFace ? '是' : '否'}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">等级</td>
                    <td style="border: 1px solid #ddd; padding: 12px;">
                      <span style="padding: 4px 8px; border-radius: 4px; background-color: ${getLevelType(result.level) === 'primary' ? '#ecf5ff' : getLevelType(result.level) === 'warning' ? '#fdf6ec' : getLevelType(result.level) === 'danger' ? '#fef0f0' : '#f4f4f5'}; color: ${getLevelType(result.level) === 'primary' ? '#409eff' : getLevelType(result.level) === 'warning' ? '#e6a23c' : getLevelType(result.level) === 'danger' ? '#f56c6c' : '#909399'}">
                        ${result.level}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">置信度</td>
                    <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold; color: #409eff;">${(result.confidence * 100).toFixed(2)}%</td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 12px; font-weight: bold;">可视化描述</td>
                    <td style="border: 1px solid #ddd; padding: 12px;">${result.visualizationDescription}</td>
                  </tr>
                </table>
              </div>
              <div style="flex: 1; min-width: 300px; text-align: center;">
                <h4 style="margin-bottom: 15px;">分析图像</h4>
                <img src="${result.imagePath}" style="max-width: 300px; max-height: 300px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);" />
              </div>
            </div>
          </div>
        `;
      });

      // 添加总结部分
      if (compareResults.value.length > 1) {
        const total = compareResults.value.length;
        const glandFaceCount = compareResults.value.filter(r => r.isGlandFace).length;
        const avgConfidence = compareResults.value.reduce((sum, r) => sum + r.confidence, 0) / total;
        
        resultsHTML += `
          <div style="margin-top: 30px; page-break-inside: avoid;">
            <h3 style="text-align: center; margin-bottom: 20px;">对比总结</h3>
            <div style="display: flex; gap: 20px;">
              <div style="flex: 1; text-align: center; padding: 20px; background-color: #f5f7fa; border-radius: 8px;">
                <div style="font-size: 24px; font-weight: bold; color: #333;">${total}</div>
                <div style="color: #666;">总检测数</div>
              </div>
              <div style="flex: 1; text-align: center; padding: 20px; background-color: #f5f7fa; border-radius: 8px;">
                <div style="font-size: 24px; font-weight: bold; color: #f56c6c;">${glandFaceCount}</div>
                <div style="color: #666;">腺体面容数</div>
              </div>
              <div style="flex: 1; text-align: center; padding: 20px; background-color: #f5f7fa; border-radius: 8px;">
                <div style="font-size: 24px; font-weight: bold; color: #409eff;">${(avgConfidence * 100).toFixed(2)}%</div>
                <div style="color: #666;">平均置信度</div>
              </div>
            </div>
          </div>
        `;
      }

      printContent.innerHTML = `
        <div style="text-align: center; margin-bottom: 30px;">
          <h1>腺体面容对比分析报告</h1>
        </div>
        <div>
          ${resultsHTML}
        </div>
        <div style="margin-top: 40px; text-align: center; color: #999; font-size: 12px;">
          报告生成时间: ${new Date().toLocaleString('zh-CN')}
        </div>
      `;

      // 创建打印窗口
      const printWindow = window.open('', '_blank');
      printWindow.document.write(`
        <html>
          <head>
            <title>腺体面容对比分析报告</title>
            <style>
              @media print {
                body {
                  -webkit-print-color-adjust: exact;
                  print-color-adjust: exact;
                }
              }
            </style>
          </head>
          <body>
            ${printContent.innerHTML}
            <script>
              window.onload = function() {
                window.print();
                window.close();
              }
            <\/script>
          </body>
        </html>
      `);
      printWindow.document.close();
    };

    return {
      selectedFiles,
      loading,
      analysisResult,
      compareResults,
      error,
      resultDialogVisible,
      compareDialogVisible,
      isCameraActive,
      videoElement,
      canvasElement,
      isMobile,
      handleFileChange,
      removeFile,
      clearFiles,
      startCamera,
      captureImage,
      stopCamera,
      formatDateTime,
      getLevelType,
      analyzeFaces,
      handleDialogClose,
      exportToPDF,
      exportCompareResults
    };
  }
};
</script>

<style scoped>
.face-analysis-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  min-height: 100%;
  box-sizing: border-box;
}

.header {
  margin-bottom: 25px;
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  color: white;
}

.header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.header-subtitle {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.upload-card {
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: none;
  overflow: hidden;
}

.upload-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 20px 25px;
  background: linear-gradient(135deg, #f0f8ff, #e6f2ff);
  border-bottom: 1px solid #ebeef5;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-section {
  padding: 30px;
}

.camera-preview {
  position: relative;
  width: 100%;
  height: 400px;
  background: #000;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.video-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.camera-instructions {
  text-align: center;
  color: white;
}

.camera-instructions .capture-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.camera-instructions p {
  font-size: 18px;
  margin: 0;
}

.upload-area {
  margin-bottom: 30px;
}

.upload-placeholder {
  padding: 50px 0;
}

.upload-icon {
  font-size: 60px;
  color: #409eff;
  margin-bottom: 20px;
}

.el-upload__tip {
  color: #999;
  font-size: 14px;
  margin-top: 15px;
}

.file-limit {
  margin-top: 8px;
  font-style: italic;
}

.preview-container {
  margin-top: 20px;
}

.multi-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
}

.preview-item {
  position: relative;
  width: 120px;
  text-align: center;
}

.preview-image-small {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.file-name {
  margin: 10px 0 5px 0;
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #f56c6c;
  border: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.action-button {
  min-width: 140px;
  padding: 15px 25px;
  font-size: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.action-button.secondary {
  background: #f0f2f5;
  border-color: #dcdfe6;
  color: #606266;
}

.action-button.secondary:hover {
  background: #e1e5ea;
  border-color: #c0c4cc;
}

.compare-dialog {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.compare-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
}

.compare-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
}

.compare-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.compare-results {
  max-height: 70vh;
  overflow-y: auto;
  padding: 25px;
}

.result-col {
  margin-bottom: 25px;
}

.result-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
}

.result-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
}

.result-header {
  padding: 15px 20px;
  background: linear-gradient(135deg, #f0f8ff, #e6f2ff);
  border-bottom: 1px solid #ebeef5;
}

.result-header h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-content {
  padding: 20px;
}

.result-image {
  margin-bottom: 20px;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-item .label {
  font-weight: 600;
  color: #444;
  min-width: 120px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.result-item .value {
  flex: 1;
  color: #333;
  font-size: 15px;
}

.result-item .value.confidence {
  font-weight: 700;
  color: #409eff;
  font-size: 18px;
}

.result-item .value.description {
  line-height: 1.6;
  color: #555;
}

.summary-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  margin-top: 20px;
}

.summary-header {
  padding: 15px 20px;
  background: linear-gradient(135deg, #f0f8ff, #e6f2ff);
  border-bottom: 1px solid #ebeef5;
}

.summary-header h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-content {
  padding: 30px 20px;
}

.summary-item {
  text-align: center;
  padding: 25px;
  background: linear-gradient(135deg, #f0f8ff, #e6f2ff);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.summary-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.summary-label {
  font-size: 16px;
  color: #555;
  margin-bottom: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.no-results {
  text-align: center;
  padding: 50px 0;
}

.single-result-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
}

.single-result-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
}

.single-result-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.dialog-result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 25px;
}

.result-tags {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.result-tag {
  font-size: 16px;
  padding: 10px 20px;
}

.result-image .image-title {
  text-align: center;
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.dialog-footer {
  text-align: right;
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.error-alert {
  margin-top: 20px;
  border-radius: 12px;
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
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .face-analysis-container {
    padding: 15px;
  }
  
  .header {
    padding: 20px;
    border-radius: 16px;
    margin-bottom: 20px;
  }
  
  .header h2 {
    font-size: 24px;
  }
  
  .header-subtitle {
    font-size: 14px;
  }
  
  .upload-card,
  .result-card,
  .summary-card {
    margin: 10px;
    border-radius: 16px;
  }
  
  .upload-section {
    padding: 20px;
  }
  
  .card-header h3 {
    font-size: 18px;
  }
  
  .camera-preview {
    height: 300px;
  }
  
  .action-buttons {
    gap: 15px;
  }
  
  .action-button {
    min-width: 120px;
    padding: 12px 20px;
    font-size: 15px;
  }
  
  .multi-preview {
    gap: 15px;
  }
  
  .preview-item {
    width: 100px;
  }
  
  .preview-image-small {
    height: 100px;
  }
  
  .result-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .result-item .label {
    min-width: auto;
    margin-bottom: 5px;
  }
  
  .compare-dialog {
    width: 95%;
  }
  
  .compare-results {
    padding: 15px;
  }
  
  .summary-content {
    padding: 20px 10px;
  }
  
  .summary-item {
    padding: 20px 15px;
  }
  
  .summary-label {
    font-size: 14px;
  }
  
  .summary-value {
    font-size: 20px;
  }
  
  .dialog-result-content {
    gap: 20px;
  }
  
  .result-tag {
    font-size: 14px;
    padding: 8px 16px;
  }
}

@media (max-width: 480px) {
  .face-analysis-container {
    padding: 10px;
  }
  
  .header {
    padding: 15px;
    border-radius: 12px;
  }
  
  .header h2 {
    font-size: 20px;
    gap: 8px;
  }
  
  .header-subtitle {
    font-size: 12px;
  }
  
  .upload-section {
    padding: 15px;
  }
  
  .camera-preview {
    height: 250px;
  }
  
  .upload-placeholder {
    padding: 30px 0;
  }
  
  .upload-icon {
    font-size: 40px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .action-button {
    width: 100%;
    justify-content: center;
  }
  
  .multi-preview {
    gap: 10px;
  }
  
  .preview-item {
    width: 80px;
  }
  
  .preview-image-small {
    height: 80px;
  }
  
  .file-name {
    font-size: 10px;
  }
  
  .result-header h4,
  .summary-header h4 {
    font-size: 16px;
  }
  
  .result-item .label,
  .result-item .value {
    font-size: 14px;
  }
  
  .result-item .value.confidence {
    font-size: 16px;
  }
  
  .summary-label {
    font-size: 13px;
  }
  
  .summary-value {
    font-size: 18px;
  }
  
  .compare-dialog :deep(.el-dialog__header) {
    padding: 15px 20px;
  }
  
  .compare-dialog :deep(.el-dialog__title) {
    font-size: 18px;
  }
  
  .single-result-dialog :deep(.el-dialog__header) {
    padding: 15px 20px;
  }
  
  .single-result-dialog :deep(.el-dialog__title) {
    font-size: 18px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 10px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    justify-content: center;
  }
}
</style>