<template>
  <div class="user-profile-container">
    <div class="header">
      <h2 class="section-header"><el-icon><User /></el-icon> 个人资料</h2>
      <p class="header-subtitle">管理您的个人资料和账户设置</p>
    </div>
    
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3><el-icon><User /></el-icon> 个人资料</h3>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="profile-header">
          <el-avatar 
            :src="profileForm.avatar || defaultAvatar" 
            :size="120" 
            shape="circle"
            class="profile-avatar"
          />
          <div class="profile-basic-info">
            <h2>{{ profileForm.name || profileForm.username }}</h2>
            <p class="username">@{{ profileForm.username }}</p>
            <div class="profile-actions">
              <el-button type="primary" @click="openEditDialog" plain size="large">
                <el-icon><Edit /></el-icon> 编辑资料
              </el-button>
              <el-button type="primary" @click="openAvatarDialog" size="large">
                <el-icon><Picture /></el-icon> 更换头像
              </el-button>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="profile-details">
          <el-row :gutter="30">
            <el-col :span="12" class="detail-col">
              <div class="detail-item">
                <div class="detail-label">
                  <el-icon class="detail-icon"><Message /></el-icon>
                  邮箱
                </div>
                <div class="detail-value">
                  {{ profileForm.email || '未设置' }}
                </div>
              </div>
            </el-col>
            
            <el-col :span="12" class="detail-col">
              <div class="detail-item">
                <div class="detail-label">
                  <el-icon class="detail-icon"><Phone /></el-icon>
                  手机号
                </div>
                <div class="detail-value">
                  {{ profileForm.phone || '未设置' }}
                </div>
              </div>
            </el-col>
            
            <el-col :span="12" class="detail-col">
              <div class="detail-item">
                <div class="detail-label">
                  <el-icon class="detail-icon" v-if="profileForm.gender === 1"><Male /></el-icon>
                  <el-icon class="detail-icon" v-else-if="profileForm.gender === 2"><Female /></el-icon>
                  <el-icon class="detail-icon" v-else><QuestionFilled /></el-icon>
                  性别
                </div>
                <div class="detail-value">
                  {{ genderText }}
                </div>
              </div>
            </el-col>
            
            <el-col :span="12" class="detail-col">
              <div class="detail-item">
                <div class="detail-label">
                  <el-icon class="detail-icon"><Calendar /></el-icon>
                  生日
                </div>
                <div class="detail-value">
                  {{ profileForm.birthday || '未设置' }}
                </div>
              </div>
            </el-col>
            
            <el-col :span="24" class="detail-col">
              <div class="detail-item">
                <div class="detail-label">
                  <el-icon class="detail-icon"><Location /></el-icon>
                  地址
                </div>
                <div class="detail-value address-value">
                  {{ profileForm.address || '未设置' }}
                </div>
              </div>
            </el-col>
            
            <el-col :span="24" class="detail-col">
              <div class="detail-item">
                <div class="detail-label">
                  <el-icon class="detail-icon"><Clock /></el-icon>
                  注册时间
                </div>
                <div class="detail-value">
                  {{ formattedCreateTime }}
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
    
    <!-- 修改信息弹窗 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="修改个人信息" 
      width="600px" 
      :before-close="handleEditDialogClose"
      class="edit-dialog"
    >
      <el-form 
        ref="editFormRef"
        :model="editForm" 
        :rules="editRules"
        label-width="100px"
        class="edit-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="editForm.username" disabled />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="editForm.name" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="editForm.gender" placeholder="请选择性别" clearable style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
                <el-option label="未知" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="editForm.birthday"
                type="date"
                placeholder="请选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="地址" prop="address">
              <el-input v-model="editForm.address" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="editForm.newPassword" 
                type="password"
                placeholder="不修改请留空"
                show-password
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword" v-if="editForm.newPassword">
              <el-input 
                v-model="editForm.confirmPassword" 
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false" size="large">
            <el-icon><Close /></el-icon> 取消
          </el-button>
          <el-button type="primary" @click="updateProfile" :loading="loading" size="large">
            <el-icon><Check /></el-icon> 确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改头像弹窗 -->
    <el-dialog 
      v-model="avatarDialogVisible" 
      title="修改头像" 
      width="500px" 
      :before-close="handleAvatarDialogClose"
      class="avatar-dialog"
    >
      <div class="avatar-edit">
        <div class="avatar-preview">
          <el-avatar 
            :src="profileForm.avatar || defaultAvatar" 
            :size="150" 
            shape="circle"
            class="preview-avatar"
          />
        </div>
        <el-upload
          class="avatar-uploader"
          action="/api/user/avatar"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
          :before-upload="beforeAvatarUpload"
          :auto-upload="true"
          :headers="uploadHeaders"
          name="avatar"
        >
          <el-button size="large" type="primary">
            <el-icon><Upload /></el-icon> 选择图片
          </el-button>
          <p class="upload-tip">支持JPG、PNG格式，文件小于2MB</p>
        </el-upload>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="avatarDialogVisible = false" size="large">
            <el-icon><Close /></el-icon> 取消
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      :closable="false"
      class="error-alert"
    />
    
    <el-alert
      v-if="successMessage"
      :title="successMessage"
      type="success"
      show-icon
      :closable="false"
      class="success-alert"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import request from '../utils/request';
import {
  User,
  Edit,
  Picture,
  Message,
  Phone,
  Male,
  Female,
  Calendar,
  Location,
  Clock,
  Upload,
  QuestionFilled,
  Close,
  Check
} from '@element-plus/icons-vue';

export default {
  name: 'UserProfile',
  components: {
    User,
    Edit,
    Picture,
    Message,
    Phone,
    Male,
    Female,
    Calendar,
    Location,
    Clock,
    Upload,
    QuestionFilled,
    Close,
    Check
  },
  setup() {
    // 表单引用
    const profileFormRef = ref(null);
    const editFormRef = ref(null);
    
    // 表单数据
    const profileForm = ref({
      id: '',
      username: '',
      createTime: '',
      avatar: '',
      name: '',
      email: '',
      phone: '',
      gender: 0,
      birthday: '',
      address: ''
    });
    
    const editForm = ref({
      id: '',
      username: '',
      name: '',
      email: '',
      phone: '',
      gender: 0,
      birthday: '',
      address: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    // 弹窗控制
    const editDialogVisible = ref(false);
    const avatarDialogVisible = ref(false);
    
    // 上传头像请求头
    // 上传头像的请求头
    const uploadHeaders = computed(() => {
      const token = localStorage.getItem('token');
      return token ? { Authorization: token } : {};
    });
    
    // 使用Element Plus默认头像URL作为默认头像
    const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    
    // 性别文本显示
    const genderText = computed(() => {
      switch (profileForm.value.gender) {
        case 1: return '男';
        case 2: return '女';
        default: return '未知';
      }
    });
    
    // 格式化注册时间
    const formattedCreateTime = computed(() => {
      try {
        // 检查是否有注册时间数据
        if (!profileForm.value.createTime) {
          return '未设置';
        }
        
        let date;
        // 根据不同的输入类型处理日期
        if (typeof profileForm.value.createTime === 'string') {
          // 如果是字符串，尝试解析
          date = new Date(profileForm.value.createTime);
        } else if (profileForm.value.createTime instanceof Date) {
          // 如果已经是Date对象
          date = profileForm.value.createTime;
        } else {
          // 其他情况尝试直接转换
          date = new Date(profileForm.value.createTime);
        }
        
        // 检查日期是否有效
        if (isNaN(date.getTime())) {
          console.warn('无法解析的日期格式:', profileForm.value.createTime);
          return profileForm.value.createTime || '未知'; // 返回原始值或'未知'
        }
        
        // 返回格式化的日期时间
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        }).replace(/\//g, '-'); // 将日期分隔符从/改为-
      } catch (error) {
        console.error('时间格式化错误:', error);
        // 出错时返回默认值
        return '未知';
      }
    });
    
    // 表单验证规则
    const editRules = {
      name: [
        { required: false, message: '请输入姓名', trigger: 'blur' }
      ],
      email: [
        { required: false, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
      ],
      phone: [
        { required: false, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      newPassword: [
        { required: false, message: '如需修改密码请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应在6到20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: false, message: '请再次输入密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value && value !== editForm.value.newPassword) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    };
    
    const loading = ref(false);
    const errorMessage = ref('');
    const successMessage = ref('');
    
    // 打开编辑弹窗
    const openEditDialog = () => {
      // 初始化编辑表单，确保所有字段都有默认值
      editForm.value = {
        id: profileForm.value.id || '',
        username: profileForm.value.username || '',
        name: profileForm.value.name || '',
        email: profileForm.value.email || '',
        phone: profileForm.value.phone || '',
        gender: profileForm.value.gender || 0,
        birthday: profileForm.value.birthday || '',
        address: profileForm.value.address || '',
        newPassword: '',
        confirmPassword: ''
      };
      editDialogVisible.value = true;
    };
    
    // 打开头像弹窗
    const openAvatarDialog = () => {
      avatarDialogVisible.value = true;
    };
    
    // 关闭编辑弹窗前的处理
    const handleEditDialogClose = (done) => {
      editForm.newPassword = '';
      editForm.confirmPassword = '';
      done();
    };
    
    // 关闭头像弹窗前的处理
    const handleAvatarDialogClose = (done) => {
      done();
    };
    
    // 头像上传成功处理
    const handleAvatarSuccess = (response) => {
      if (response.code === 1) {
        profileForm.avatar = response.data;
        successMessage.value = '头像修改成功';
        // 重新获取用户信息
        getCurrentUser();
        avatarDialogVisible.value = false;
      } else {
        errorMessage.value = response.msg || '头像修改失败';
      }
    };
    
    // 头像上传失败处理
    const handleAvatarError = (error) => {
      console.error('头像修改失败:', error);
      errorMessage.value = '头像修改失败，请重试';
    };
    
    // 头像上传前检查
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;
      
      if (!isImage) {
        errorMessage.value = '头像必须是图片格式!';
        return false;
      }
      if (!isLt2M) {
        errorMessage.value = '头像大小不能超过 2MB!';
        return false;
      }
      return true;
    };
    
    // 获取当前用户信息
    const getCurrentUser = async () => {
      try {
        const response = await request.get('/user');
        console.log('用户信息响应:', response); // 调试信息
        if (response.code === 1) {
          profileForm.value.id = response.data.id;
          profileForm.value.username = response.data.username;
          profileForm.value.createTime = response.data.createTime;
          profileForm.value.avatar = response.data.avatar || '';
          profileForm.value.name = response.data.name || '';
          profileForm.value.email = response.data.email || '';
          profileForm.value.phone = response.data.phone || '';
          profileForm.value.gender = response.data.gender || 0;
          profileForm.value.birthday = response.data.birthday || '';
          profileForm.value.address = response.data.address || '';
          console.log('注册时间:', profileForm.value.createTime); // 调试信息
        } else {
          errorMessage.value = response.msg || '获取用户信息失败';
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        errorMessage.value = error.message || '获取用户信息失败';
      }
    };
    
    // 更新用户信息
    const updateProfile = async () => {
      if (!editFormRef.value) return;
      
      await editFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          errorMessage.value = '';
          successMessage.value = '';
          
          try {
            // 准备更新数据
            const updateData = {
              id: editForm.value.id,
              username: editForm.value.username,
              name: editForm.value.name,
              email: editForm.value.email,
              phone: editForm.value.phone,
              gender: editForm.value.gender,
              birthday: editForm.value.birthday,
              address: editForm.value.address
            };
            
            // 如果密码不为空，则包含密码
            if (editForm.value.newPassword) {
              updateData.password = editForm.value.newPassword;
            }
            
            const response = await request.put('/user', updateData);
            if (response.code === 1) {
              successMessage.value = '用户信息更新成功';
              editDialogVisible.value = false;
              // 重新获取用户信息
              await getCurrentUser();
            } else {
              errorMessage.value = response.msg || '更新失败';
            }
          } catch (error) {
            console.error('更新用户信息失败:', error);
            errorMessage.value = error.message || '更新用户信息失败';
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    // 组件挂载时获取用户信息
    onMounted(() => {
      getCurrentUser();
    });
    
    return {
      profileFormRef,
      editFormRef,
      profileForm,
      editForm,
      formattedCreateTime,
      genderText,
      editRules,
      loading,
      errorMessage,
      successMessage,
      defaultAvatar,
      uploadHeaders,
      editDialogVisible,
      avatarDialogVisible,
      handleAvatarSuccess,
      handleAvatarError,
      beforeAvatarUpload,
      openEditDialog,
      openAvatarDialog,
      handleEditDialogClose,
      handleAvatarDialogClose,
      updateProfile
    };
  }
};
</script>

<style scoped>
.user-profile-container {
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

.profile-card {
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: none;
  overflow: hidden;
}

.profile-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 25px 30px;
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

.profile-content {
  padding: 30px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fc, #edf2f7);
  border-radius: 16px;
}

.profile-avatar {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  border: 4px solid white;
}

.profile-basic-info {
  flex: 1;
}

.profile-basic-info h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: #333;
  font-weight: 600;
}

.username {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 16px;
}

.profile-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.profile-actions .el-button {
  padding: 12px 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.profile-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.profile-details {
  margin-top: 20px;
}

.detail-col {
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  padding: 20px;
  background: #f8f9fc;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.detail-item:hover {
  background: #edf2f7;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 120px;
  font-weight: 600;
  color: #444;
  font-size: 16px;
}

.detail-icon {
  font-size: 20px;
  color: #409eff;
}

.detail-value {
  flex: 1;
  color: #333;
  font-size: 16px;
  line-height: 1.5;
}

.address-value {
  white-space: pre-wrap;
}

.edit-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
}

.edit-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.edit-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.edit-form {
  padding: 20px;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.avatar-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
}

.avatar-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.avatar-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.avatar-edit {
  text-align: center;
  padding: 30px 20px;
}

.avatar-preview {
  margin-bottom: 25px;
}

.preview-avatar {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  border: 4px solid white;
}

.avatar-uploader {
  margin-top: 20px;
}

.upload-tip {
  margin: 10px 0 0 0;
  color: #999;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.dialog-footer .el-button {
  min-width: 100px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.error-alert,
.success-alert {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 40px);
  max-width: 500px;
  border-radius: 12px;
  z-index: 1000;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  width: 100px !important;
}

@media (max-width: 768px) {
  .user-profile-container {
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
  
  .profile-content {
    padding: 20px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;
    padding: 25px;
  }
  
  .profile-basic-info h2 {
    font-size: 24px;
  }
  
  .profile-actions {
    justify-content: center;
    gap: 12px;
  }
  
  .profile-actions .el-button {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .detail-item {
    flex-direction: column;
    gap: 10px;
    padding: 15px;
  }
  
  .detail-label {
    width: auto;
    justify-content: flex-start;
  }
  
  .detail-value {
    font-size: 15px;
  }
  
  .card-header {
    padding: 20px;
  }
  
  .card-header h3 {
    font-size: 18px;
  }
  
  .edit-dialog {
    width: 90%;
  }
  
  .edit-form {
    padding: 15px;
  }
  
  .edit-form :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  .edit-dialog :deep(.el-dialog__header) {
    padding: 15px 20px;
  }
  
  .edit-dialog :deep(.el-dialog__title) {
    font-size: 18px;
  }
  
  .avatar-dialog :deep(.el-dialog__header) {
    padding: 15px 20px;
  }
  
  .avatar-dialog :deep(.el-dialog__title) {
    font-size: 18px;
  }
  
  .avatar-edit {
    padding: 20px 15px;
  }
  
  .preview-avatar {
    width: 120px;
    height: 120px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 10px;
    padding: 15px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    justify-content: center;
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .user-profile-container {
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
  
  .profile-header {
    padding: 20px;
  }
  
  .profile-basic-info h2 {
    font-size: 20px;
  }
  
  .username {
    font-size: 14px;
    margin-bottom: 15px;
  }
  
  .profile-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .profile-actions .el-button {
    width: 100%;
    justify-content: center;
    padding: 12px;
  }
  
  .detail-item {
    padding: 15px;
  }
  
  .detail-label,
  .detail-value {
    font-size: 14px;
  }
  
  .card-header {
    padding: 15px;
  }
  
  .card-header h3 {
    font-size: 16px;
  }
  
  .profile-content {
    padding: 15px;
  }
}
</style>