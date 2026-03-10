<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2><el-icon><Headset /></el-icon> 腺体面容AI助手</h2>
      <el-button 
        v-if="activeConversation && activeConversation.id" 
        type="danger" 
        plain 
        @click="clearConversation"
        size="small"
      >
        <el-icon><Delete /></el-icon> 清除对话
      </el-button>
    </div>
    
    <div class="chat-layout">
      <!-- 历史对话列表 -->
      <div class="conversation-list" v-show="showConversationList">
        <div class="list-header">
          <h3><el-icon><Clock /></el-icon> 历史对话</h3>
          <el-button type="primary" @click="startNewConversation" size="small" plain>
            <el-icon><Plus /></el-icon> 新对话
          </el-button>
        </div>
        <div class="list-content">
          <div 
            v-for="conversation in conversationList" 
            :key="conversation.id"
            :class="['conversation-item', { active: activeConversation && activeConversation.id === conversation.id }]"
            @click="loadConversation(conversation)"
          >
            <div class="item-title">{{ conversation.title }}</div>
            <div class="item-time">{{ formatConversationTime(conversation.lastUpdateTime) }}</div>
          </div>
          <div v-if="conversationList.length === 0" class="empty-state">
            <el-icon><ChatLineRound /></el-icon>
            <span>暂无历史对话</span>
          </div>
        </div>
      </div>
      
      <!-- 聊天主区域 -->
      <div class="chat-main">
        <div class="chat-messages" ref="messagesContainer">
          <div 
            v-for="message in messageList" 
            :key="message.id"
            :class="['message', message.sender === 0 ? 'user-message' : 'ai-message']"
          >
            <div class="message-content">
              <div class="avatar">
                <el-avatar :icon="message.sender === 0 ? 'el-icon-user' : 'el-icon-headset'" 
                          :class="message.sender === 0 ? 'user-avatar' : 'ai-avatar'" />
              </div>
              <div class="content">
                <div class="text">{{ message.content }}</div>
                <div class="timestamp">{{ formatTime(message.sendTime) }}</div>
              </div>
            </div>
          </div>
          
          <div v-if="loading" class="message ai-message">
            <div class="message-content">
              <div class="avatar">
                <el-avatar icon="el-icon-headset" class="ai-avatar" />
              </div>
              <div class="content">
                <div class="text typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            @keydown.enter.exact.prevent="sendMessage"
            :disabled="loading"
          ></el-input>
          <div class="input-actions">
            <el-button 
              :type="isListening ? 'danger' : 'primary'" 
              @click="toggleSpeechRecognition"
              :disabled="loading"
              class="voice-button"
              circle
            >
              <el-icon><Microphone /></el-icon>
            </el-button>
            <el-button 
              type="primary" 
              @click="sendMessage"
              :loading="loading"
              :disabled="!inputMessage.trim() || loading"
              class="send-button"
            >
              <el-icon><Position /></el-icon> 发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue';
import request from '../utils/request';
import {
  Headset,
  Delete,
  Clock,
  Plus,
  ChatLineRound,
  Microphone,
  Position
} from '@element-plus/icons-vue';

export default {
  name: 'ChatAssistant',
  components: {
    Headset,
    Delete,
    Clock,
    Plus,
    ChatLineRound,
    Microphone,
    Position
  },
  setup() {
    // 响应式数据
    const conversationList = ref([]); // 历史对话列表
    const activeConversation = ref(null); // 当前激活的对话
    const messageList = ref([]); // 当前对话的消息列表
    const inputMessage = ref('');
    const loading = ref(false);
    const messagesContainer = ref(null);
    const showConversationList = ref(true); // 是否显示对话列表
    const isListening = ref(false); // 是否正在语音识别
    let recognition = null; // 语音识别对象
    
    // 初始化语音识别
    const initSpeechRecognition = () => {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      if (SpeechRecognition) {
        recognition = new SpeechRecognition();
        recognition.lang = 'zh-CN'; // 设置为中文识别
        recognition.continuous = false; // 不连续识别
        recognition.interimResults = false; // 不返回临时结果
        
        recognition.onstart = () => {
          isListening.value = true;
        };
        
        recognition.onresult = (event) => {
          const transcript = event.results[0][0].transcript;
          inputMessage.value += transcript;
        };
        
        recognition.onerror = (event) => {
          console.error('语音识别错误:', event.error);
          isListening.value = false;
        };
        
        recognition.onend = () => {
          isListening.value = false;
        };
      } else {
        console.warn('当前浏览器不支持语音识别功能');
      }
    };
    
    // 切换语音识别状态
    const toggleSpeechRecognition = () => {
      if (!recognition) {
        console.warn('语音识别功能不可用');
        return;
      }
      
      if (isListening.value) {
        recognition.stop();
        isListening.value = false;
      } else {
        try {
          recognition.start();
        } catch (error) {
          console.error('启动语音识别失败:', error);
        }
      }
    };
    
    // 格式化消息时间
    const formatTime = (time) => {
      if (!time) return '';
      const date = new Date(time);
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    };
    
    // 格式化对话时间
    const formatConversationTime = (time) => {
      if (!time) return '';
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      
      // 如果是今天
      if (date.toDateString() === now.toDateString()) {
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
      }
      
      // 如果是昨天
      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);
      if (date.toDateString() === yesterday.toDateString()) {
        return '昨天';
      }
      
      // 其他情况显示日期
      return `${date.getMonth() + 1}-${date.getDate()}`;
    };
    
    // 获取历史对话列表
    const loadConversationList = async () => {
      try {
        const response = await request.get('/conversation/list');
        if (response.code === 1) {
          conversationList.value = response.data || [];
        }
      } catch (error) {
        console.error('获取对话列表失败:', error);
        // 检查是否因为未登录导致的错误
        if (error.response && error.response.status === 401) {
          // 可以在这里添加重新登录的逻辑
          console.log('用户未登录或登录已过期');
        }
      }
    };
    
    // 获取对话消息
    const loadMessages = async (conversationId) => {
      try {
        const response = await request.get(`/conversation/messages?conversationId=${conversationId}`);
        if (response.code === 1) {
          messageList.value = response.data || [];
        }
      } catch (error) {
        console.error('获取消息列表失败:', error);
      }
    };
    
    // 开始新对话
    const startNewConversation = () => {
      activeConversation.value = null;
      messageList.value = [];
      inputMessage.value = '';
      showConversationList.value = false; // 开始新对话时隐藏历史记录列表
    };
    
    // 加载对话
    const loadConversation = async (conversation) => {
      console.log('点击加载对话:', conversation); // 调试信息
      try {
        const response = await request.get(`/conversation/messages?conversationId=${conversation.id}`);
        console.log('加载对话响应:', response); // 调试信息
        if (response.code === 1) {
          activeConversation.value = conversation;
          messageList.value = response.data || [];
          showConversationList.value = false; // 加载对话时隐藏历史记录列表
        } else {
          console.error('加载对话失败:', response.msg);
        }
      } catch (error) {
        console.error('加载对话失败:', error);
        if (error.response && error.response.status === 401) {
          // 可以在这里添加重新登录的逻辑
          console.log('用户未登录或登录已过期');
        }
      }
    };
    
    // 创建新对话并发送消息
    const createConversation = async (firstMessage) => {
      try {
        const response = await request.post('/conversation/create', 
          `firstMessage=${encodeURIComponent(firstMessage)}`, {
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          });
        
        if (response.code === 1) {
          activeConversation.value = response.data;
          await loadConversationList(); // 刷新对话列表
          return response.data.id;
        }
        return null;
      } catch (error) {
        console.error('创建对话失败:', error);
        // 检查是否因为未登录导致的错误
        if (error.response && error.response.status === 401) {
          // 可以在这里添加重新登录的逻辑
          console.log('用户未登录或登录已过期');
        }
        return null;
      }
    };
    
    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || loading.value) return;
      
      const userContent = inputMessage.value;
      
      // 如果没有激活的对话，先创建一个新对话
      if (!activeConversation.value) {
        const conversationId = await createConversation(userContent);
        if (!conversationId) {
          // 创建对话失败
          const errorMessage = {
            id: Date.now() + 1,
            sender: 1,
            content: '创建对话失败，请重试',
            sendTime: new Date()
          };
          messageList.value.push(errorMessage);
          return;
        }
      }
      
      // 发送消息
      await sendToAI(userContent);
    };
    
    // 发送消息到AI（非流式）
    const sendToAI = async (content) => {
      const userMessage = {
        id: Date.now(),
        sender: 0, // 用户
        content: content,
        sendTime: new Date()
      };
      
      messageList.value.push(userMessage);
      inputMessage.value = '';
      loading.value = true;
      
      try {
        // 滚动到底部
        await nextTick();
        scrollToBottom();
        
        // 发送消息到后端（使用非流式接口）
        const response = await request.post('/conversation/send', 
          `conversationId=${activeConversation.value.id}&content=${encodeURIComponent(content)}`, {
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          });
        
        if (response.code === 1) {
          // 添加AI回复
          const aiMessage = {
            id: Date.now() + 1,
            sender: 1, // AI助手
            content: response.data,
            sendTime: new Date()
          };
          messageList.value.push(aiMessage);
          
          // 刷新对话列表（更新时间）
          await loadConversationList();
        } else {
          // 处理错误
          const errorMessage = {
            id: Date.now() + 1,
            sender: 1,
            content: response.msg || '发送失败，请重试',
            sendTime: new Date()
          };
          messageList.value.push(errorMessage);
        }
      } catch (error) {
        console.error('发送消息失败:', error);
        // 检查是否因为未登录导致的错误
        if (error.response && error.response.status === 401) {
          const errorMessage = {
            id: Date.now() + 1,
            sender: 1,
            content: '登录已过期，请重新登录',
            sendTime: new Date()
          };
          messageList.value.push(errorMessage);
        } else {
          const errorMessage = {
            id: Date.now() + 1,
            sender: 1,
            content: '网络错误，请重试',
            sendTime: new Date()
          };
          messageList.value.push(errorMessage);
        }
      } finally {
        loading.value = false;
        await nextTick();
        scrollToBottom();
      }
    };
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
      }
    };
    
    // 清除对话
    const clearConversation = async () => {
      if (activeConversation.value && activeConversation.value.id) {
        try {
          await request.delete(`/conversation/${activeConversation.value.id}`);
        } catch (error) {
          console.error('清除对话失败:', error);
        }
      }
      
      messageList.value = [];
      activeConversation.value = null;
      inputMessage.value = '';
      showConversationList.value = true; // 清除对话时显示历史记录列表
    };
    
    // 组件挂载后获取对话列表
    onMounted(async () => {
      await loadConversationList();
      initSpeechRecognition(); // 初始化语音识别
    });
    
    return {
      conversationList,
      activeConversation,
      messageList,
      inputMessage,
      loading,
      messagesContainer,
      showConversationList,
      isListening,
      formatTime,
      formatConversationTime,
      sendMessage,
      startNewConversation,
      loadConversation,
      clearConversation,
      toggleSpeechRecognition
    };
  }
};
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 120px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin: 20px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chat-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.conversation-list {
  width: 280px;
  background: white;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.list-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.conversation-item {
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.conversation-item:hover {
  background: #f0f8ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.conversation-item.active {
  background: #ecf5ff;
  border-color: #409eff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

.item-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-time {
  font-size: 12px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 25px;
  background: #f8f9fc;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.message-content {
  display: flex;
  max-width: 80%;
  gap: 12px;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.content {
  background: white;
  padding: 15px 18px;
  border-radius: 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  position: relative;
}

.ai-message .content {
  border-top-left-radius: 6px;
}

.user-message .content {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border-top-right-radius: 6px;
}

.text {
  font-size: 15px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: #333;
}

.user-message .text {
  color: white;
}

.timestamp {
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.9;
  color: #666;
  text-align: right;
}

.user-message .timestamp {
  color: rgba(255, 255, 255, 0.9);
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  background: #409eff;
  border-radius: 50%;
  margin-right: 6px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
  margin-right: 0;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-5px);
  }
}

.chat-input {
  padding: 25px;
  background: white;
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-textarea__inner) {
  resize: none;
  border-radius: 15px;
  padding: 15px 20px;
  border: 1px solid #dcdfe6;
  transition: border-color 0.3s;
}

:deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.voice-button {
  width: 50px;
  height: 50px;
  min-width: 50px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.voice-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.voice-button:not(:disabled):hover {
  transform: scale(1.1);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.send-button {
  flex: 1;
  margin-left: 20px;
  border-radius: 12px;
  height: 50px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.send-button:hover {
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 100px);
    border-radius: 0;
    margin: 0;
  }
  
  .conversation-list {
    width: 240px;
  }
  
  .message-content {
    max-width: 90%;
  }
  
  .chat-header {
    padding: 15px 20px;
  }
  
  .chat-header h2 {
    font-size: 18px;
  }
  
  .chat-messages {
    padding: 20px;
    gap: 15px;
  }
  
  .content {
    padding: 12px 15px;
  }
  
  .text {
    font-size: 14px;
  }
  
  .chat-input {
    padding: 20px;
  }
  
  .input-actions {
    margin-top: 15px;
  }
  
  .voice-button {
    width: 45px;
    height: 45px;
    min-width: 45px;
  }
  
  .send-button {
    height: 45px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .conversation-list {
    width: 100%;
    position: absolute;
    height: 100%;
    z-index: 10;
    display: none;
  }
  
  .conversation-list[v-show] {
    display: flex;
  }
  
  .chat-main {
    position: absolute;
    width: 100%;
    height: 100%;
  }
  
  .chat-main::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 5;
  }
  
  .input-actions {
    margin-top: 12px;
  }
  
  .voice-button {
    width: 40px;
    height: 40px;
    min-width: 40px;
  }
  
  .send-button {
    height: 40px;
    font-size: 14px;
  }
  
  .content {
    padding: 10px 12px;
  }
  
  .text {
    font-size: 13px;
  }
}
</style>