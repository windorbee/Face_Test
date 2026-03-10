<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-card">
        <div class="register-header">
          <h2 class="register-title">用户注册</h2>
          <p class="register-subtitle">请填写以下信息完成注册</p>
        </div>
        
        <el-form 
          ref="registerFormRef"
          :model="registerForm" 
          :rules="registerRules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <i class="el-icon-user input-icon"></i>
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入用户名"
                size="large"
                clearable
                class="register-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="name">
            <div class="input-wrapper">
              <i class="el-icon-user input-icon"></i>
              <el-input 
                v-model="registerForm.name" 
                placeholder="请输入姓名"
                size="large"
                clearable
                class="register-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="email">
            <div class="input-wrapper">
              <i class="el-icon-message input-icon"></i>
              <el-input 
                v-model="registerForm.email" 
                placeholder="请输入邮箱"
                size="large"
                clearable
                class="register-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="phone">
            <div class="input-wrapper">
              <i class="el-icon-phone input-icon"></i>
              <el-input 
                v-model="registerForm.phone" 
                placeholder="请输入手机号"
                size="large"
                clearable
                class="register-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <i class="el-icon-lock input-icon"></i>
              <el-input 
                v-model="registerForm.password" 
                type="password"
                placeholder="请输入密码"
                show-password
                size="large"
                class="register-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <div class="input-wrapper">
              <i class="el-icon-lock input-icon"></i>
              <el-input 
                v-model="registerForm.confirmPassword" 
                type="password"
                placeholder="请确认密码"
                show-password
                size="large"
                class="register-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              native-type="submit"
              :loading="loading"
              size="large"
              class="register-button"
            >
              {{ loading ? '注册中...' : '注册账号' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-link">
          <span>已有账号?</span>
          <a href="#" @click.prevent="goToLogin" class="login-btn">立即登录</a>
        </div>
      </div>
    </div>
    
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
import { ref, reactive } from 'vue';
import request from '../utils/request';
import { ElMessage } from 'element-plus';

export default {
  name: 'Register',
  emits: ['switch-to-login'],
  setup(props, { emit }) {
    const registerFormRef = ref();
    
    const registerForm = reactive({
      username: '',
      name: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: ''
    });
    
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
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
    
    const handleRegister = async () => {
      if (!registerFormRef.value) return;
      
      await registerFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          errorMessage.value = '';
          successMessage.value = '';
          
          try {
            const userData = {
              username: registerForm.username,
              name: registerForm.name,
              email: registerForm.email,
              phone: registerForm.phone,
              password: registerForm.password
            };
            
            const response = await request.post('/user/register', userData);
            if (response.code === 1) {
              // 注册成功
              successMessage.value = '注册成功，请登录';
              // 清空表单
              registerForm.username = '';
              registerForm.name = '';
              registerForm.email = '';
              registerForm.phone = '';
              registerForm.password = '';
              registerForm.confirmPassword = '';
              
              // 3秒后自动跳转到登录页面
              setTimeout(() => {
                emit('switch-to-login');
              }, 3000);
            } else {
              // 注册失败
              errorMessage.value = response.msg || '注册失败';
            }
          } catch (error) {
            console.error('注册请求失败:', error);
            errorMessage.value = error.message || '注册请求失败，请检查网络连接';
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    const goToLogin = () => {
      emit('switch-to-login');
    };
    
    return {
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      errorMessage,
      successMessage,
      handleRegister,
      goToLogin
    };
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  padding: 20px;
  background: linear-gradient(135deg, #e0f2ff 0%, #f0f9ff 100%);
}

.register-wrapper {
  width: 100%;
  max-width: 500px;
}

.register-card {
  background: white;
  padding: 50px;
  border-radius: 20px;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.register-header {
  margin-bottom: 30px;
  text-align: center;
}

.register-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #1d2129;
}

.register-subtitle {
  font-size: 16px;
  color: #86909c;
  margin: 0;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #86909c;
  z-index: 1;
  font-size: 18px;
}

:deep(.register-input .el-input__inner) {
  width: 100%;
  height: 55px;
  padding-left: 50px;
  font-size: 16px;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
  transition: all 0.3s;
}

:deep(.register-input .el-input__inner:focus) {
  border-color: #165dff;
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.2);
}

.register-button {
  width: 100%;
  height: 55px;
  font-size: 17px;
  font-weight: 500;
  border-radius: 12px;
  background: linear-gradient(135deg, #165dff 0%, #36cfc9 100%);
  border: none;
  letter-spacing: 1px;
  transition: all 0.3s;
  margin-bottom: 30px;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 93, 255, 0.3);
}

.login-link {
  text-align: center;
  font-size: 15px;
  color: #86909c;
}

.login-btn {
  color: #165dff;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.login-btn:hover {
  text-decoration: underline;
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
}

@media (max-width: 576px) {
  .register-container {
    padding: 15px;
  }
  
  .register-card {
    padding: 30px 20px;
  }
  
  .register-title {
    font-size: 28px;
  }
}
</style>