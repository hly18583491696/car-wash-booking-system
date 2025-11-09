import request from './request.js'

/**
 * 时间段相关API
 */
export const timeSlotsApi = {
  /**
   * 获取指定日期的可用时间段
   * @param {string} date - 日期字符串 YYYY-MM-DD
   */
  async getAvailableTimeSlots(date) {
    return request.get('/time-slots/available', {
      params: { date }
    })
  },

  /**
   * 根据时间字符串查找时间段ID
   * @param {string} date - 日期字符串 YYYY-MM-DD
   * @param {string} timeRange - 时间范围字符串，如 "09:00-09:30"
   */
  async findTimeSlotId(date, timeRange) {
    return request.get('/time-slots/find', {
      params: { date, timeRange }
    })
  },

  /**
   * 获取时间段详情
   * @param {number} timeSlotId - 时间段ID
   */
  async getTimeSlotById(timeSlotId) {
    return request.get(`/time-slots/${timeSlotId}`)
  }
}

/**
 * 时间段工具类
 */
export class TimeSlotUtils {
  /**
   * 将时间范围字符串转换为开始和结束时间
   * @param {string} timeRange - 时间范围，如 "09:00-09:30"
   * @returns {object} - {startTime, endTime}
   */
  static parseTimeRange(timeRange) {
    const [startTime, endTime] = timeRange.split('-')
    return { startTime, endTime }
  }

  /**
   * 根据日期和时间范围生成时间段查询参数
   * @param {string} date - 日期字符串
   * @param {string} timeRange - 时间范围字符串
   * @returns {object} - 查询参数
   */
  static buildTimeSlotQuery(date, timeRange) {
    const { startTime, endTime } = this.parseTimeRange(timeRange)
    return {
      date,
      startTime,
      endTime
    }
  }

  /**
   * 从时间段列表中查找匹配的时间段ID
   * @param {Array} timeSlots - 时间段列表
   * @param {string} timeRange - 时间范围字符串
   * @returns {number|null} - 时间段ID或null
   */
  static findTimeSlotIdFromList(timeSlots, timeRange) {
    const { startTime, endTime } = this.parseTimeRange(timeRange)
    
    const matchedSlot = timeSlots.find(slot => {
      const slotStart = slot.startTime || slot.start_time
      const slotEnd = slot.endTime || slot.end_time
      return slotStart === startTime && slotEnd === endTime
    })
    
    return matchedSlot ? matchedSlot.id : null
  }
}

export default timeSlotsApi