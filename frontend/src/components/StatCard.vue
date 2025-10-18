<template>
  <div class="stat-card" :class="{ loading: isLoading }">
    <div class="stat-icon" :style="{ background: iconColor }">
      <el-icon v-if="!isLoading"><component :is="icon" /></el-icon>
      <div v-else class="loading-spinner"></div>
    </div>
    <div class="stat-content">
      <h3 class="stat-value" v-if="!isLoading">{{ formattedValue }}</h3>
      <div v-else class="skeleton-value"></div>
      
      <p class="stat-label">{{ label }}</p>
      
      <div class="stat-trend" :class="trendClass" v-if="!isLoading && showTrend">
        <el-icon><ArrowUp v-if="trend > 0" /><ArrowDown v-else /></el-icon>
        <span>{{ Math.abs(trend) }}%</span>
        <span class="trend-text">{{ trendText }}</span>
      </div>
    </div>
    
    <!-- 迷你图表 -->
    <div class="mini-chart" v-if="chartData && !isLoading">
      <div ref="miniChart" class="mini-chart-container"></div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'StatCard',
  props: {
    icon: {
      type: String,
      required: true
    },
    label: {
      type: String,
      required: true
    },
    value: {
      type: [String, Number],
      required: true
    },
    trend: {
      type: Number,
      default: 0
    },
    iconColor: {
      type: String,
      default: 'var(--primary-gradient)'
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    showTrend: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Array,
      default: null
    },
    valueType: {
      type: String,
      default: 'number', // number, currency, percentage
      validator: value => ['number', 'currency', 'percentage'].includes(value)
    }
  },
  setup(props) {
    const miniChart = ref(null)
    let miniChartInstance = null
    
    const formattedValue = computed(() => {
      if (props.isLoading) return ''
      
      switch (props.valueType) {
        case 'currency':
          return `¥${Number(props.value).toLocaleString()}`
        case 'percentage':
          return `${props.value}%`
        default:
          return Number(props.value).toLocaleString()
      }
    })
    
    const trendClass = computed(() => ({
      positive: props.trend > 0,
      negative: props.trend < 0,
      neutral: props.trend === 0
    }))
    
    const trendText = computed(() => {
      if (props.trend > 0) return '较上期'
      if (props.trend < 0) return '较上期'
      return '与上期持平'
    })
    
    const initMiniChart = () => {
      if (!miniChart.value || !props.chartData) return
      
      miniChartInstance = echarts.init(miniChart.value)
      
      const option = {
        grid: {
          left: 0,
          right: 0,
          top: 0,
          bottom: 0
        },
        xAxis: {
          type: 'category',
          show: false,
          data: props.chartData.map((_, index) => index)
        },
        yAxis: {
          type: 'value',
          show: false
        },
        series: [{
          type: 'line',
          data: props.chartData,
          smooth: true,
          symbol: 'none',
          lineStyle: {
            color: 'var(--primary-color)',
            width: 2
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0,
                color: 'var(--primary-color)'
              }, {
                offset: 1,
                color: 'transparent'
              }]
            }
          }
        }]
      }
      
      miniChartInstance.setOption(option)
    }
    
    onMounted(() => {
      if (props.chartData) {
        nextTick(() => {
          initMiniChart()
        })
      }
    })
    
    watch(() => props.chartData, () => {
      if (props.chartData) {
        nextTick(() => {
          initMiniChart()
        })
      }
    })
    
    return {
      miniChart,
      formattedValue,
      trendClass,
      trendText
    }
  }
}
</script>

<style scoped>
.stat-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-card.loading {
  pointer-events: none;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-white);
  font-size: 24px;
  flex-shrink: 0;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.2;
}

.skeleton-value {
  height: 32px;
  background: var(--border-light);
  border-radius: var(--radius-sm);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.stat-label {
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  font-weight: 500;
}

.stat-trend.positive {
  color: var(--success-color);
}

.stat-trend.negative {
  color: var(--error-color);
}

.stat-trend.neutral {
  color: var(--text-secondary);
}

.trend-text {
  font-size: 0.8rem;
  opacity: 0.8;
}

.mini-chart {
  position: absolute;
  right: 16px;
  bottom: 16px;
  width: 80px;
  height: 40px;
  opacity: 0.6;
}

.mini-chart-container {
  width: 100%;
  height: 100%;
}

@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
    gap: 12px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .mini-chart {
    display: none;
  }
}
</style>