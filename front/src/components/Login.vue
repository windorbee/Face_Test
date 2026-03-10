<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-placeholder">
            <el-icon class="face-icon"><UserFilled /></el-icon>
          </div>
          <h1 class="system-title">腺体面容识别系统</h1>
          <p class="system-description">
            基于先进的生物特征识别技术，提供精准、安全的腺体面容识别解决方案，保护您的信息安全。
          </p>
          
          <div class="features-list">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Lock /></el-icon>
              </div>
              <div class="feature-content">
                <h3 class="feature-title">高度安全</h3>
                <p class="feature-description">多重加密保护，防止身份信息泄露</p>
              </div>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="feature-content">
                <h3 class="feature-title">快速识别</h3>
                <p class="feature-description">毫秒级响应速度，提升使用体验</p>
              </div>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="feature-content">
                <h3 class="feature-title">精准可靠</h3>
                <p class="feature-description">先进算法保障，识别准确率达99.9%</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-section">
        <div class="login-card">
          <div class="login-header">
            <h2 class="login-title">欢迎登录</h2>
            <p class="login-subtitle">请输入您的账号信息以继续</p>
          </div>
          
          <el-form 
            ref="loginFormRef"
            :model="loginForm" 
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input 
                  v-model="loginForm.username" 
                  placeholder="请输入用户名"
                  size="large"
                  clearable
                  class="login-input"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input 
                  v-model="loginForm.password" 
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  size="large"
                  class="login-input"
                />
              </div>
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe" class="remember-checkbox">
                记住我
              </el-checkbox>
              <a href="#" class="forgot-password">忘记密码?</a>
            </div>
            
            <el-form-item>
              <el-button 
                type="primary" 
                native-type="submit"
                :loading="loading"
                size="large"
                class="login-button"
              >
                <el-icon v-if="!loading"><Right /></el-icon>
                {{ loading ? '登录中...' : '登录系统' }}
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="divider">
            <span class="divider-text">其他登录方式</span>
          </div>
          
          <div class="social-login">
            <el-button class="social-btn" plain>
              <el-icon><ChatRound /></el-icon>
            </el-button>
            <el-button class="social-btn" plain>
              <el-icon><ChatDotSquare /></el-icon>
            </el-button>
            <el-button class="social-btn" plain>
              <el-icon><Message /></el-icon>
            </el-button>
          </div>
          
          <div class="register-link">
            <span>还没有账号?</span>
            <a href="#" @click.prevent="goToRegister" class="register-btn">立即注册</a>
          </div>
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
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import request from '../utils/request';
import {
  UserFilled,
  Lock,
  Lightning,
  Check,
  User,
  Right,
  ChatRound,
  ChatDotSquare,
  Message
} from '@element-plus/icons-vue';

export default {
  name: 'Login',
  components: {
    UserFilled,
    Lock,
    Lightning,
    Check,
    User,
    Right,
    ChatRound,
    ChatDotSquare,
    Message
  },
  emits: ['switch-to-register'],
  setup(props, { emit }) {
    const loginFormRef = ref();
    
    const loginForm = reactive({
      username: '',
      password: ''
    });
    
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    };
    
    const rememberMe = ref(false);
    const loading = ref(false);
    const errorMessage = ref('');
    
    const handleLogin = async () => {
      if (!loginFormRef.value) return;
      
      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          errorMessage.value = '';
          
          try {
            const response = await request.post('/login', loginForm);
            if (response.code === 1) {
              // 登录成功
              const token = response.data;
              // 保存token到localStorage
              localStorage.setItem('token', token);
              // 跳转到主页面或刷新页面
              window.location.reload();
            } else {
              // 登录失败
              errorMessage.value = response.msg || '登录失败';
            }
          } catch (error) {
            console.error('登录请求失败:', error);
            errorMessage.value = error.message || '登录请求失败，请检查网络连接';
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    const goToRegister = () => {
      emit('switch-to-register');
    };
    
    return {
      loginFormRef,
      loginForm,
      loginRules,
      rememberMe,
      loading,
      errorMessage,
      handleLogin,
      goToRegister
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  padding: 0;
  background: linear-gradient(135deg, #e0f2ff 0%, #f0f9ff 100%);
}

.login-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100%;
  height: 100vh;
  max-width: 1300px;
  background: white;
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.login-wrapper:hover {
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

.brand-section {
  background: linear-gradient(135deg, #165dff 0%, #36cfc9 100%);
  padding: 60px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.brand-section::after {
  content: '';
  position: absolute;
  bottom: -80px;
  left: -80px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.brand-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-placeholder {
  width: 120px;
  height: 120px;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.logo-placeholder:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}

.face-icon {
  font-size: 60px;
  color: white;
}

.system-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 20px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 1px;
}

.system-description {
  font-size: 17px;
  line-height: 1.7;
  margin-bottom: 40px;
  opacity: 0.95;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.feature-item {
  display: flex;
  gap: 20px;
  transition: all 0.3s ease;
  padding: 15px;
  border-radius: 15px;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(5px);
}

.feature-icon {
  font-size: 28px;
  min-width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.feature-content {
  flex: 1;
}

.feature-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.feature-description {
  font-size: 16px;
  margin: 0;
  opacity: 0.95;
  line-height: 1.5;
}

.login-section {
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.login-card {
  width: 100%;
  max-width: 450px;
  margin: 0 auto;
}

.login-header {
  margin-bottom: 40px;
  text-align: center;
}

.login-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: #1d2129;
}

.login-subtitle {
  font-size: 17px;
  color: #86909c;
  margin: 0;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #86909c;
  z-index: 1;
  font-size: 20px;
}

:deep(.login-input .el-input__inner) {
  width: 100%;
  height: 60px;
  padding-left: 55px;
  font-size: 16px;
  border-radius: 15px;
  border: 2px solid #e5e6eb;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.login-input .el-input__inner:focus) {
  border-color: #165dff;
  box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.25);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 35px;
}

.remember-checkbox {
  color: #1d2129;
}

:deep(.remember-checkbox .el-checkbox__label) {
  font-size: 16px;
  font-weight: 400;
}

.forgot-password {
  color: #165dff;
  font-size: 16px;
  text-decoration: none;
  transition: all 0.3s;
}

.forgot-password:hover {
  text-decoration: underline;
  color: #0d47a1;
}

.login-button {
  width: 100%;
  height: 60px;
  font-size: 18px;
  font-weight: 500;
  border-radius: 15px;
  background: linear-gradient(135deg, #165dff 0%, #36cfc9 100%);
  border: none;
  letter-spacing: 1px;
  transition: all 0.3s;
  margin-bottom: 35px;
  box-shadow: 0 6px 16px rgba(22, 93, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(22, 93, 255, 0.4);
}

.login-button:active {
  transform: translateY(-1px);
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 35px;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e5e6eb;
}

.divider-text {
  position: relative;
  background: white;
  padding: 0 25px;
  font-size: 16px;
  color: #86909c;
  font-weight: 500;
}

.social-login {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
  margin-bottom: 35px;
}

.social-btn {
  width: 100%;
  height: 60px;
  border-radius: 15px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  border: 2px solid #e5e6eb;
  color: #86909c;
  transition: all 0.3s;
  background: white;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.social-btn:hover {
  border-color: #165dff;
  color: #165dff;
  background: rgba(22, 93, 255, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(22, 93, 255, 0.15);
}

.register-link {
  text-align: center;
  font-size: 16px;
  color: #86909c;
}

.register-btn {
  color: #165dff;
  text-decoration: none;
  margin-left: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

.register-btn:hover {
  text-decoration: underline;
  color: #0d47a1;
}

.error-alert {
  position: fixed;
  top: 25px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 50px);
  max-width: 550px;
  border-radius: 15px;
  z-index: 1000;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

@media (max-width: 992px) {
  .login-wrapper {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 100vh;
    max-width: 700px;
    border-radius: 20px;
  }
  
  .brand-section {
    padding: 50px 40px;
  }
  
  .login-section {
    padding: 50px 40px;
  }
  
  .system-title {
    font-size: 32px;
  }
  
  .system-description {
    font-size: 16px;
  }
  
  .feature-title {
    font-size: 20px;
  }
  
  .feature-description {
    font-size: 15px;
  }
  
  .login-card {
    max-width: 100%;
  }
  
  .login-title {
    font-size: 32px;
  }
}

@media (max-width: 576px) {
  .login-container {
    padding: 15px;
  }
  
  .login-wrapper {
    max-width: 100%;
    border-radius: 15px;
  }
  
  .brand-section,
  .login-section {
    padding: 35px 25px;
  }
  
  .system-title {
    font-size: 28px;
  }
  
  .login-title {
    font-size: 28px;
  }
  
  .features-list {
    gap: 25px;
  }
  
  .feature-item {
    gap: 15px;
    padding: 12px;
  }
  
  .feature-icon {
    min-width: 50px;
    height: 50px;
    font-size: 24px;
  }
  
  .feature-title {
    font-size: 18px;
  }
  
  .feature-description {
    font-size: 14px;
  }
  
  .social-login {
    gap: 15px;
  }
  
  .social-btn {
    height: 55px;
    font-size: 22px;
  }
  
  .login-button {
    height: 55px;
    font-size: 16px;
  }
  
  :deep(.login-input .el-input__inner) {
    height: 55px;
    padding-left: 50px;
  }
  
  .input-icon {
    font-size: 18px;
    left: 15px;
  }
}
</style>