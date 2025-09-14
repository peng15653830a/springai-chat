<template>
  <div class="order-list">
    <h2>订单列表</h2>
    <el-table :data="orders" style="width: 100%">
      <el-table-column prop="id" label="订单ID" width="80"></el-table-column>
      <el-table-column prop="productId" label="商品ID" width="80"></el-table-column>
      <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
      <el-table-column prop="totalPrice" label="总价">
        <template #default="scope">
          ¥{{ scope.row.totalPrice }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100"></el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'OrderList',
  data() {
    return {
      orders: []
    }
  },
  mounted() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        const response = await axios.get('http://localhost:8082/api/orders')
        this.orders = response.data
      } catch (error) {
        console.error('加载订单失败:', error)
        this.$message.error('加载订单失败')
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.order-list {
  margin-top: 20px;
}
</style>