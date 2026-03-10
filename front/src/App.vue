<template>
  <div id="app">
    <div v-if="!isLoggedIn && currentView === 'login'">
      <Login @login-success="handleLoginSuccess" @switch-to-register="currentView = 'register'" />
    </div>
    <div v-else-if="!isLoggedIn && currentView === 'register'">
      <Register @switch-to-login="currentView = 'login'" />
    </div>
    <div v-else>
      <el-container style="height: 100vh; margin: 0; padding: 0;">
        <!-- 侧边栏 -->
        <el-aside width="200px" style="background-color: #304156; color: #fff; display: flex; flex-direction: column; margin: 0; padding: 0;">
          <div class="system-title" style="margin: 0;">
            <div class="title-icon">
              <el-icon :size="24" color="#409eff"><User /></el-icon>
            </div>
            <div class="title-text">腺体面容检测系统</div>
          </div>
          <el-menu
            :default-active="activePage"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409eff"
            @select="switchToPage"
            style="border: none; flex: 1; margin: 0; padding: 0;"
          >
            <el-menu-item index="face-analysis" class="sidebar-menu-item">
              <div class="menu-item-content">
                <el-icon class="sidebar-icon"><Picture /></el-icon>
                <span class="sidebar-text">面容分析</span>
              </div>
            </el-menu-item>
            <el-menu-item index="chat-assistant" class="sidebar-menu-item">
              <div class="menu-item-content">
                <el-icon class="sidebar-icon"><Service /></el-icon>
                <span class="sidebar-text">AI助手</span>
              </div>
            </el-menu-item>
            <el-menu-item index="test-result" class="sidebar-menu-item">
              <div class="menu-item-content">
                <el-icon class="sidebar-icon"><List /></el-icon>
                <span class="sidebar-text">检测记录</span>
              </div>
            </el-menu-item>
            <el-menu-item index="statistics" class="sidebar-menu-item">
              <div class="menu-item-content">
                <el-icon class="sidebar-icon"><TrendCharts /></el-icon>
                <span class="sidebar-text">统计分析</span>
              </div>
            </el-menu-item>
            <el-menu-item index="user-profile" class="sidebar-menu-item">
              <div class="menu-item-content">
                <el-icon class="sidebar-icon"><User /></el-icon>
                <span class="sidebar-text">个人中心</span>
              </div>
            </el-menu-item>
          </el-menu>
          <!-- 退出登录按钮放在侧边栏底部 -->
          <div style="padding: 15px; border-top: 1px solid #435565; margin: 0;">
            <el-button @click="handleLogout" type="danger" plain size="small" style="width: 100%;" class="logout-button">
              <el-icon class="sidebar-icon"><SwitchButton /></el-icon>
              <span class="sidebar-text">退出登录</span>
            </el-button>
          </div>
        </el-aside>
        
        <!-- 主内容区 -->
        <el-container style="margin: 0; padding: 0;">
          <!-- 头部 -->
          <el-header style="background-color: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08); display: flex; justify-content: flex-end; align-items: center; padding: 0 20px; margin: 0; height: 60px;">
            <div class="user-info" v-if="userInfo">
              <div class="user-info-content">
                <el-avatar 
                  :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                  :size="40"
                  class="user-avatar"
                />
                <div class="user-details">
                  <div class="user-name">{{ userInfo.name || userInfo.username }}</div>
                  <div class="user-role">系统用户</div>
                </div>
                <div class="user-dropdown">
                  <el-dropdown @command="handleUserCommand">
                    <el-icon class="user-dropdown-icon"><ArrowDown /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="profile">
                          <el-icon><User /></el-icon> 个人中心
                        </el-dropdown-item>
                        <el-dropdown-item command="settings">
                          <el-icon><Setting /></el-icon> 设置
                        </el-dropdown-item>
                        <el-dropdown-item divided command="logout">
                          <el-icon><SwitchButton /></el-icon> 退出登录
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </el-header>
          
          <!-- 主体内容 -->
          <el-main style="padding: 0; margin: 0;">
            <div class="page-container">
              <FaceAnalysis v-if="activePage === 'face-analysis'" />
              <ChatAssistant v-if="activePage === 'chat-assistant'" />
              <TestResult v-if="activePage === 'test-result'" />
              <Statistics v-if="activePage === 'statistics'" />
              <UserProfile v-if="activePage === 'user-profile'" />
            </div>
          </el-main>
        </el-container>
      </el-container>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { 
  Picture, 
  Service, 
  List, 
  User, 
  TrendCharts,
  SwitchButton,
  ArrowDown,
  Setting
} from '@element-plus/icons-vue'
import request from './utils/request'
import FaceAnalysis from './components/FaceAnalysis.vue'
import ChatAssistant from './components/ChatAssistant.vue'
import TestResult from './components/TestResult.vue'
import Statistics from './components/Statistics.vue'
import UserProfile from './components/UserProfile.vue'
import Login from './components/Login.vue'
import Register from './components/Register.vue'

export default {
  name: 'App',
  components: {
    FaceAnalysis,
    ChatAssistant,
    TestResult,
    Statistics,
    UserProfile,
    Login,
    Register,
    Picture,
    Service,
    List,
    User,
    TrendCharts,
    SwitchButton,
    ArrowDown,
    Setting
  },
  setup() {
    // 响应式状态
    const isLoggedIn = ref(false) // 登录状态
    const currentView = ref('login') // 当前视图：login 或 register
    const activePage = ref('face-analysis') // 当前活动页面
    const userInfo = ref(null) // 用户信息

    // 组件挂载时检查登录状态
    onMounted(() => {
      const token = localStorage.getItem('token')
      isLoggedIn.value = !!token
      if (token) {
        fetchUserInfo()
      }
    })

    // 监听登录状态变化，更新用户信息
    watch(isLoggedIn, (newVal) => {
      if (newVal) {
        fetchUserInfo()
      } else {
        userInfo.value = null
      }
    })

    // 获取用户信息
    const fetchUserInfo = async () => {
      try {
        const response = await request.get('/user')
        // 由于响应拦截器返回的是response.data，所以这里直接检查response.code
        if (response.code === 1) {
          userInfo.value = response.data
        } else {
          console.error('获取用户信息失败:', response.msg || response.message || '未知错误')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }

    // 处理登录成功
    const handleLoginSuccess = () => {
      isLoggedIn.value = true
    }

    // 处理登出
    const handleLogout = () => {
      localStorage.removeItem('token')
      isLoggedIn.value = false
      activePage.value = 'face-analysis'
    }

    // 切换页面
    const switchToPage = (page) => {
      activePage.value = page
    }

    // 全局处理认证失效
    const handleAuthFailure = () => {
      localStorage.removeItem('token')
      isLoggedIn.value = false
      activePage.value = 'face-analysis'
    }

    const handleUserCommand = (command) => {
      switch (command) {
        case 'profile':
          activePage.value = 'user-profile'
          break
        case 'settings':
          // 这里可以添加设置页面的逻辑
          console.log('打开设置页面')
          break
        case 'logout':
          handleLogout()
          break
        default:
          break
      }
    }

    // 暴露给模板的属性和方法
    return {
      isLoggedIn,
      currentView,
      activePage,
      userInfo,
      handleLoginSuccess,
      handleLogout,
      switchToPage,
      handleAuthFailure,
      handleUserCommand // 添加这个函数到返回对象中，修复跳转功能
    }
  }
}
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}

.el-container {
  height: 100%;
  margin: 0;
  padding: 0;
}

.el-aside {
  margin: 0;
  padding: 0;
  height: 100%;
}

.el-header {
  margin: 0;
  padding: 0;
  background-color: #fff;
  color: #333;
  height: 50px;
  line-height: 50px;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%;
  background-color: #f5f7fa;
  margin: 0;
  padding: 0;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background-color: #f5f7fa;
}

.system-title {
  height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #007acc, #005a99);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
  padding: 10px 0;
  transition: all 0.3s ease;
  margin: 0;
}

.title-icon {
  margin-bottom: 5px;
  transition: all 0.3s ease;
}

.system-title:hover .title-icon {
  transform: scale(1.1);
}

.title-text {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: #fff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.system-title:hover .title-text {
  letter-spacing: 1.5px;
}

.system-title::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transform: translateX(-100%);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}

.user-info {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.user-info-content {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  border-radius: 30px;
  padding: 5px 15px 5px 5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  min-width: 200px;
}

.user-info-content:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.user-avatar {
  margin-right: 12px;
  border: 2px solid #409eff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.user-details {
  display: flex;
  align-items: center;
  margin-right: 15px;
  min-width: 120px;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80px;
}

.user-role {
  font-size: 12px;
  color: #666;
  margin-left: 8px;
  padding-left: 8px;
  border-left: 1px solid #ccc;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px;
}

.user-dropdown-icon {
  font-size: 16px;
  color: #666;
  transition: all 0.3s ease;
}

.user-dropdown:hover .user-dropdown-icon {
  color: #409eff;
  transform: translateY(1px);
}

/* 页面容器样式，用于消除组件间的空白 */
.page-container {
  height: calc(100vh - 60px);
  overflow-y: auto;
}

/* 侧边栏菜单项样式 */
.sidebar-menu-item {
  height: 50px !important;
  position: relative;
  transition: all 0.3s ease;
  padding: 0 !important;
}

.sidebar-menu-item:hover {
  background-color: #2d3e50 !important;
  color: #fff !important;
}

.sidebar-menu-item.is-active {
  background-color: #2d3e50 !important;
  color: #409eff !important;
}

.sidebar-menu-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background-color: #409eff;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.sidebar-menu-item.is-active::before {
  transform: scaleY(1);
}

.sidebar-menu-item:hover::before {
  transform: scaleY(1);
}

/* 菜单项内容布局 */
.menu-item-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding-left: 20px;
  transition: all 0.3s ease;
}

.sidebar-menu-item:hover .menu-item-content {
  padding-left: 25px;
}

/* 侧边栏图标样式 */
.sidebar-icon {
  font-size: 18px !important;
  margin-right: 12px;
  width: 24px;
  text-align: center;
  transition: all 0.3s ease;
}

.sidebar-menu-item:hover .sidebar-icon {
  transform: scale(1.2);
  color: #409eff;
}

/* 侧边栏文字样式 */
.sidebar-text {
  font-size: 14px;
  transition: all 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-menu-item:hover .sidebar-text {
  font-weight: 500;
}

/* 退出登录按钮样式 */
.logout-button {
  transition: all 0.3s ease;
  border-radius: 4px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  border: 1px solid #f56c6c !important;
  background-color: transparent !important;
  color: #f56c6c !important;
}

.logout-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(245, 108, 108, 0.3);
  background-color: #fef0f0 !important;
}

.logout-button .sidebar-icon {
  margin-right: 8px;
  font-size: 16px !important;
}
</style>