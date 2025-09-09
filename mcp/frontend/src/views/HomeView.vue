<template>
  <div class="home">
    <el-tabs type="border-card">
      <el-tab-pane label="发送指令">
        <NaturalLanguageOrder @refresh-data="refreshData" />
      </el-tab-pane>
      <el-tab-pane label="商品列表">
        <ProductList ref="productList" />
      </el-tab-pane>
      <el-tab-pane label="订单列表">
        <OrderList ref="orderList" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import NaturalLanguageOrder from '@/components/NaturalLanguageOrder.vue'
import ProductList from '@/components/ProductList.vue'
import OrderList from '@/components/OrderList.vue'

export default {
  name: 'HomeView',
  components: {
    NaturalLanguageOrder,
    ProductList,
    OrderList
  },
  methods: {
    refreshData() {
      // 刷新商品列表
      if (this.$refs.productList) {
        this.$refs.productList.loadProducts()
      }
      // 刷新订单列表
      if (this.$refs.orderList) {
        this.$refs.orderList.loadOrders()
      }
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>