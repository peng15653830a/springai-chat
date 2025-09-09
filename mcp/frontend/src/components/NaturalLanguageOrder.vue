<template>
  <div class="natural-language-order">
    <h2>指令发送</h2>
    <el-form :model="orderForm" ref="orderForm" label-width="120px">
      <el-form-item label="请输入指令">
        <el-input
          v-model="orderForm.instruction"
          type="textarea"
          placeholder="例如：我要买3个苹果手机"
          :rows="4"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitOrder">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-dialog v-model="dialogVisible" title="处理结果" width="50%">
      <div v-if="orderResult" style="white-space: pre-line; line-height: 1.6;">
        {{ orderResult }}
      </div>
      <div v-else>
        <p>处理失败，请重试</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'NaturalLanguageOrder',
  data() {
    return {
      orderForm: {
        instruction: ''
      },
      dialogVisible: false,
      orderResult: null
    }
  },
  methods: {
    async submitOrder() {
      if (!this.orderForm.instruction) {
        this.$message.warning('请输入指令')
        return
      }
      
      try {
        const response = await axios.post('http://localhost:8081/api/nl-orders', 
          this.orderForm.instruction,
          {
            headers: {
              'Content-Type': 'text/plain'
              }
          }
        )
        
        // 直接使用AI返回的文本
        this.orderResult = response.data
        this.dialogVisible = true
      } catch (error) {
        console.error('处理失败:', error)
        this.$message.error('处理失败: ' + (error.response?.data?.message || error.message))
      }
    },
    resetForm() {
      this.orderForm.instruction = ''
    },
    handleConfirm() {
      // 关闭对话框
      this.dialogVisible = false
      // 发出事件通知父组件刷新商品和订单列表
      this.$emit('refresh-data')
    }
  }
}
</script>

<style scoped>
.natural-language-order {
  max-width: 600px;
  margin: 0 auto;
}
</style>