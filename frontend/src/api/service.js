import mockApi from './mock.js'

// 服务相关API
export const serviceApi = {
  // 获取服务列表
  async getServices() {
    try {
      const response = await mockApi.getServices()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取服务详情
  async getServiceById(id) {
    try {
      const servicesResponse = await mockApi.getServices()
      const service = servicesResponse.data.find(s => s.id === parseInt(id))
      
      if (service) {
        return {
          code: 200,
          message: '获取成功',
          data: service
        }
      } else {
        throw new Error('服务不存在')
      }
    } catch (error) {
      throw error
    }
  }
}

export default serviceApi