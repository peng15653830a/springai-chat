<template>
  <div class="home">
    <el-tabs type="border-card">
      <el-tab-pane label="自然语言下单">
        <NaturalLanguageOrder />
      </el-tab-pane>
      <el-tab-pane label="商品列表">
        <ProductList />
      </el-tab-pane>
      <el-tab-pane label="订单列表">
        <OrderList />
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