<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>统一登录</h2>
        <p>登录后进入门户主页选择功能</p>
      </div>
      <el-form ref="loginForm" :model="form" :rules="rules" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            {{ loading ? '登录中…' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
  
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../shared/api/client'
import { useAuthStore } from '../shared/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loginForm = ref()
const loading = ref(false)

const form = reactive({ username: '', nickname: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginForm.value?.validate()
    loading.value = true
    const resp = await api.post('/users/login', form)
    if (resp && resp.success) {
      auth.setUser(resp.data)
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(resp?.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container { min-height: 100vh; display: flex; align-items: center; justify-content: center; }
.login-card { width: 400px; padding: 32px; background: #fff; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,.08); }
.login-header { text-align: center; margin-bottom: 16px; }
.login-header h2 { margin: 0 0 6px; color: #333; }
.login-header p { margin: 0; color: #666; font-size: 13px; }
</style>

