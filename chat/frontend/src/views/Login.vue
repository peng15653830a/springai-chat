<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>AI智能聊天</h2>
        <p>与AI进行智能对话，获取最新信息</p>
      </div>
      
      <el-form
        ref="loginForm"
        :model="form"
        :rules="rules"
        @submit.native.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            size="large"
            prefix-icon="Avatar"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            {{ loading ? '登录中...' : '开始聊天' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { userApi } from '../api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const loginForm = ref()
    const loading = ref(false)
    
    const form = reactive({
      username: '',
      nickname: ''
    })
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      try {
        await loginForm.value.validate()
        loading.value = true
        
        const response = await userApi.login(form)
        if (response.success) {
          userStore.setUser(response.data)
          ElMessage.success('登录成功')
          router.push('/chat')
        } else {
          ElMessage.error(response.message || '登录失败')
        }
      } catch (error) {
        console.error('Login error:', error)
        ElMessage.error('登录失败，请重试')
      } finally {
        loading.value = false
      }
    }
    
    return {
      loginForm,
      form,
      rules,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}
</style>