<template>
  <div class="parameter-panel">
    <div class="panel-header">
      <h3>模型参数</h3>
    </div>
    <div class="panel-content">
      <el-form label-width="80px" size="small">
        <el-form-item label="模型">
          <el-select v-model="novelStore.currentModel" style="width: 100%">
            <el-option
              v-for="model in novelStore.models"
              :key="model.name"
              :label="model.displayName || model.name"
              :value="model.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="温度">
          <el-slider
            v-model="novelStore.modelParameters.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
            :input-size="'small'"
          />
        </el-form-item>

        <el-form-item label="最大Token">
          <el-input-number
            v-model="novelStore.modelParameters.maxTokens"
            :min="100"
            :max="4000"
            :step="100"
            size="small"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Top P">
          <el-slider
            v-model="novelStore.modelParameters.topP"
            :min="0"
            :max="1"
            :step="0.1"
            show-input
            :input-size="'small'"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { useNovelStore } from '../stores/novel'

const novelStore = useNovelStore()
</script>

<style scoped>
.parameter-panel {
  padding: 16px;
  max-height: 300px;
}

.panel-header {
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
  color: #303133;
  font-size: 14px;
}

.panel-content {
  font-size: 13px;
}
</style>