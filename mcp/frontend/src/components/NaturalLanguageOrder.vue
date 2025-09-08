<template>
  <div class="natural-language-order">
    <h2>自然语言下单</h2>
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
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-dialog v-model="dialogVisible" title="订单结果" width="30%">
      <div v-if="orderResult">
        <p>订单ID: {{ orderResult.id }}</p>
        <p>商品: {{ getProductName(orderResult.productId) }}</p>
        <p>数量: {{ orderResult.quantity }}</p>
        <p>总价: ¥{{ orderResult.totalPrice }}</p>
        <p>状态: {{ orderResult.status }}</p>
      </div>
      <div v-else>
        <p>下单失败，请重试</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">确定</el-button>
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
      orderResult: null,
      products: []
    }
  },
  mounted() {
    this.loadProducts()
  },
  methods: {
    async submitOrder() {
      if (!this.orderForm.instruction) {
        this.$message.warning('请输入指令')
        return
      }
      
      try {
        const response = await axios.post('http://localhost:8080/api/nl-orders', {
          instruction: this.orderForm.instruction
        })
        
        this.orderResult = response.data
        this.dialogVisible = true
      } catch (error) {
        console.error('下单失败:', error)
        this.$message.error('下单失败: ' + (error.response?.data?.message || error.message))
      }
    },
    resetForm() {
      this.orderForm.instruction = ''
    },
    async loadProducts() {
      try {
        const response = await axios.get('http://localhost:8080/api/products')
        this.products = response.data
      } catch (error) {
        console.error('加载商品失败:', error)
      }
    },
    getProductName(productId) {
      const product = this.products.find(p => p.id === productId)
      return product ? product.name : '未知商品'
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