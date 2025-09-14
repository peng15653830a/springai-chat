<template>
  <div class="product-list">
    <h2>商品列表</h2>
    <el-table :data="products" style="width: 100%">
      <el-table-column prop="name" label="商品名称" width="180"></el-table-column>
      <el-table-column prop="price" label="价格" width="180">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ProductList',
  data() {
    return {
      products: []
    }
  },
  mounted() {
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      try {
        const response = await axios.get('http://localhost:8082/api/products')
        this.products = response.data
      } catch (error) {
        console.error('加载商品失败:', error)
        this.$message.error('加载商品失败')
      }
    }
  }
}
</script>

<style scoped>
.product-list {
  margin-top: 20px;
}
</style>