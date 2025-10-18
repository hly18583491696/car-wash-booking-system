<template>
  <div class="dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="container">
        <h1 class="page-title">数据统计分析</h1>
        <p class="page-subtitle">洗车服务预约数据可视化</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card" v-for="stat in statsData" :key="stat.id">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-content">
              <h3 class="stat-value">{{ stat.value }}</h3>
              <p class="stat-label">{{ stat.label }}</p>
              <div class="stat-trend" :class="stat.trend > 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="stat.trend > 0" /><ArrowDown v-else /></el-icon>
                <span>{{ Math.abs(stat.trend) }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="container">
        <div class="charts-grid">
          <!-- 预约趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>预约趋势分析</h3>
              <el-select v-model="trendPeriod" size="small" style="width: 120px">
                <el-option label="最近7天" value="7days" />
                <el-option label="最近30天" value="30days" />
                <el-option label="最近3个月" value="3months" />
              </el-select>
            </div>
            <div class="chart-container">
              <div ref="trendChart" class="chart"></div>
            </div>
          </div>

          <!-- 服务类型分布 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>服务类型分布</h3>
            </div>
            <div class="chart-container">
              <div ref="serviceChart" class="chart"></div>
            </div>
          </div>

          <!-- 收入统计 -->
          <div class="chart-card full-width">
            <div class="chart-header">
              <h3>收入统计分析</h3>
              <div class="chart-controls">
                <el-radio-group v-model="revenueType" size="small">
                  <el-radio-button label="daily">日收入</el-radio-button>
                  <el-radio-button label="monthly">月收入</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <div class="chart-container">
              <div ref="revenueChart" class="chart"></div>
            </div>
          </div>

          <!-- 时段热力图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>预约时段热力图</h3>
            </div>
            <div class="chart-container">
              <div ref="heatmapChart" class="chart"></div>
            </div>
          </div>

          <!-- 客户满意度 -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>客户满意度</h3>
            </div>
            <div class="chart-container">
              <div ref="satisfactionChart" class="chart"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  setup() {
    const trendChart = ref(null)
    const serviceChart = ref(null)
    const revenueChart = ref(null)
    const heatmapChart = ref(null)
    const satisfactionChart = ref(null)
    
    const trendPeriod = ref('7days')
    const revenueType = ref('daily')
    
    // 统计数据
    const statsData = ref([
      {
        id: 1,
        icon: 'Calendar',
        label: '今日预约',
        value: '156',
        color: 'var(--primary-gradient)',
        trend: 12.5
      },
      {
        id: 2,
        icon: 'Money',
        label: '今日收入',
        value: '¥8,420',
        color: 'linear-gradient(135deg, #10B981 0%, #059669 100%)',
        trend: 8.3
      },
      {
        id: 3,
        icon: 'User',
        label: '活跃用户',
        value: '2,341',
        color: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
        trend: -2.1
      },
      {
        id: 4,
        icon: 'Star',
        label: '平均评分',
        value: '4.8',
        color: 'linear-gradient(135deg, #EC4899 0%, #DB2777 100%)',
        trend: 5.2
      }
    ])
    
    let trendChartInstance = null
    let serviceChartInstance = null
    let revenueChartInstance = null
    let heatmapChartInstance = null
    let satisfactionChartInstance = null
    
    // 初始化预约趋势图
    const initTrendChart = () => {
      if (!trendChart.value) return
      
      trendChartInstance = echarts.init(trendChart.value)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['预约数量', '完成数量']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '预约数量',
            type: 'line',
            smooth: true,
            data: [45, 52, 38, 65, 72, 89, 94],
            itemStyle: {
              color: 'var(--primary-color)'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'var(--primary-color)'
                }, {
                  offset: 1, color: 'var(--primary-light)'
                }]
              }
            }
          },
          {
            name: '完成数量',
            type: 'line',
            smooth: true,
            data: [42, 48, 35, 61, 68, 85, 90],
            itemStyle: {
              color: 'var(--success-color)'
            }
          }
        ]
      }
      
      trendChartInstance.setOption(option)
    }
    
    // 初始化服务类型分布图
    const initServiceChart = () => {
      if (!serviceChart.value) return
      
      serviceChartInstance = echarts.init(serviceChart.value)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '服务类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: 335, name: '基础洗车' },
              { value: 310, name: '精洗套餐' },
              { value: 234, name: '内饰清洁' },
              { value: 135, name: '打蜡服务' },
              { value: 154, name: '镀膜服务' }
            ]
          }
        ]
      }
      
      serviceChartInstance.setOption(option)
    }
    
    // 初始化收入统计图
    const initRevenueChart = () => {
      if (!revenueChart.value) return
      
      revenueChartInstance = echarts.init(revenueChart.value)
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['收入', '成本', '利润']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [
          {
            name: '收入',
            type: 'bar',
            data: [45000, 52000, 38000, 65000, 72000, 89000, 94000, 87000, 76000, 82000, 91000, 98000],
            itemStyle: {
              color: 'var(--primary-color)'
            }
          },
          {
            name: '成本',
            type: 'bar',
            data: [25000, 28000, 22000, 35000, 38000, 45000, 48000, 44000, 38000, 42000, 46000, 49000],
            itemStyle: {
              color: 'var(--warning-color)'
            }
          },
          {
            name: '利润',
            type: 'line',
            data: [20000, 24000, 16000, 30000, 34000, 44000, 46000, 43000, 38000, 40000, 45000, 49000],
            itemStyle: {
              color: 'var(--success-color)'
            }
          }
        ]
      }
      
      revenueChartInstance.setOption(option)
    }
    
    // 初始化热力图
    const initHeatmapChart = () => {
      if (!heatmapChart.value) return
      
      heatmapChartInstance = echarts.init(heatmapChart.value)
      
      const hours = ['8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00']
      const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      
      const data = []
      for (let i = 0; i < days.length; i++) {
        for (let j = 0; j < hours.length; j++) {
          data.push([j, i, Math.floor(Math.random() * 20)])
        }
      }
      
      const option = {
        tooltip: {
          position: 'top',
          formatter: function (params) {
            return `${days[params.value[1]]} ${hours[params.value[0]]}<br/>预约数量: ${params.value[2]}`
          }
        },
        grid: {
          height: '50%',
          top: '10%'
        },
        xAxis: {
          type: 'category',
          data: hours,
          splitArea: {
            show: true
          }
        },
        yAxis: {
          type: 'category',
          data: days,
          splitArea: {
            show: true
          }
        },
        visualMap: {
          min: 0,
          max: 20,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '15%',
          inRange: {
            color: ['var(--primary-light)', 'var(--primary-color)', 'var(--primary-dark)']
          }
        },
        series: [{
          name: '预约热力',
          type: 'heatmap',
          data: data,
          label: {
            show: true
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      
      heatmapChartInstance.setOption(option)
    }
    
    // 初始化满意度图表
    const initSatisfactionChart = () => {
      if (!satisfactionChart.value) return
      
      satisfactionChartInstance = echarts.init(satisfactionChart.value)
      
      const option = {
        tooltip: {
          formatter: '{a} <br/>{b} : {c}%'
        },
        series: [
          {
            name: '满意度',
            type: 'gauge',
            progress: {
              show: true
            },
            detail: {
              valueAnimation: true,
              formatter: '{value}%'
            },
            data: [
              {
                value: 87,
                name: '客户满意度'
              }
            ],
            axisLine: {
              lineStyle: {
                width: 20,
                color: [
                  [0.3, '#fd666d'],
                  [0.7, '#37a2da'],
                  [1, 'var(--primary-color)']
                ]
              }
            }
          }
        ]
      }
      
      satisfactionChartInstance.setOption(option)
    }
    
    // 响应式处理
    const handleResize = () => {
      nextTick(() => {
        trendChartInstance?.resize()
        serviceChartInstance?.resize()
        revenueChartInstance?.resize()
        heatmapChartInstance?.resize()
        satisfactionChartInstance?.resize()
      })
    }
    
    // 监听主题变化
    const handleThemeChange = () => {
      // 重新初始化图表以应用新主题
      setTimeout(() => {
        initTrendChart()
        initServiceChart()
        initRevenueChart()
        initHeatmapChart()
        initSatisfactionChart()
      }, 100)
    }
    
    onMounted(() => {
      nextTick(() => {
        initTrendChart()
        initServiceChart()
        initRevenueChart()
        initHeatmapChart()
        initSatisfactionChart()
        
        window.addEventListener('resize', handleResize)
        
        // 监听主题变化
        const observer = new MutationObserver(handleThemeChange)
        observer.observe(document.documentElement, {
          attributes: true,
          attributeFilter: ['data-theme']
        })
      })
    })
    
    // 监听筛选条件变化
    watch([trendPeriod, revenueType], () => {
      // 这里可以根据筛选条件重新加载数据
      console.log('筛选条件变化:', trendPeriod.value, revenueType.value)
    })
    
    return {
      trendChart,
      serviceChart,
      revenueChart,
      heatmapChart,
      satisfactionChart,
      trendPeriod,
      revenueType,
      statsData
    }
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.dashboard-header {
  background: var(--primary-gradient);
  color: var(--text-white);
  padding: 40px 0;
  text-align: center;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 300;
  margin-bottom: 10px;
}

.page-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
}

.stats-section {
  padding: 40px 0;
  background: var(--bg-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

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
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
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
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.9rem;
  font-weight: 500;
}

.stat-trend.positive {
  color: var(--success-color);
}

.stat-trend.negative {
  color: var(--error-color);
}

.charts-section {
  padding: 40px 0;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.chart-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  color: var(--text-primary);
  font-size: 1.2rem;
  font-weight: 600;
}

.chart-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chart-container {
  padding: 20px;
}

.chart {
  width: 100%;
  height: 300px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .chart {
    height: 250px;
  }
}
</style>