/**
 * 前端统一时间工具类
 * 解决前后端时间同步问题
 */

import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 扩展dayjs插件
dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 系统默认时区 - 中国标准时间
 */
const SYSTEM_TIMEZONE = 'Asia/Shanghai'

/**
 * 标准日期时间格式
 */
const DEFAULT_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss'

/**
 * 标准日期格式
 */
const DEFAULT_DATE_FORMAT = 'YYYY-MM-DD'

/**
 * ISO格式（用于前后端传输）
 */
const ISO_FORMAT = 'YYYY-MM-DDTHH:mm:ss.SSS[Z]'

export class TimeUtils {
  /**
   * 获取当前时间（系统时区）
   * 使用本地时间，如需服务器时间同步请使用时间同步服务
   * @returns {dayjs.Dayjs} 当前时间
   */
  static now() {
    return dayjs().tz(SYSTEM_TIMEZONE)
  }

  /**
   * 获取当前UTC时间
   * @returns {dayjs.Dayjs} 当前UTC时间
   */
  static utcNow() {
    return dayjs().utc()
  }

  /**
   * 获取当前日期
   * @returns {dayjs.Dayjs} 当前日期
   */
  static today() {
    return dayjs().tz(SYSTEM_TIMEZONE).startOf('day')
  }

  /**
   * 将服务器时间转换为本地显示时间
   * @param {string|Date|dayjs.Dayjs} serverTime 服务器时间
   * @param {string} format 格式化字符串
   * @returns {string} 格式化后的时间字符串
   */
  static formatServerTime(serverTime, format = DEFAULT_DATETIME_FORMAT) {
    if (!serverTime) return ''
    
    try {
      // 假设服务器时间是系统时区时间
      return dayjs(serverTime)
        .tz(SYSTEM_TIMEZONE)
        .format(format)
    } catch (error) {
      console.warn('时间格式化失败:', error)
      return ''
    }
  }

  /**
   * 格式化日期
   * @param {string|Date|dayjs.Dayjs} date 日期
   * @param {string} format 格式化字符串
   * @returns {string} 格式化后的日期字符串
   */
  static formatDate(date, format = DEFAULT_DATE_FORMAT) {
    if (!date) return ''
    
    try {
      return dayjs(date).tz(SYSTEM_TIMEZONE).format(format)
    } catch (error) {
      console.warn('日期格式化失败:', error)
      return ''
    }
  }

  /**
   * 格式化为ISO字符串（用于发送给后端）
   * @param {string|Date|dayjs.Dayjs} time 时间
   * @returns {string} ISO格式字符串
   */
  static formatToISO(time) {
    if (!time) return ''
    
    try {
      return dayjs(time).tz(SYSTEM_TIMEZONE).utc().format(ISO_FORMAT)
    } catch (error) {
      console.warn('ISO格式化失败:', error)
      return ''
    }
  }

  /**
   * 从ISO字符串解析时间
   * @param {string} isoString ISO格式字符串
   * @returns {dayjs.Dayjs|null} dayjs对象
   */
  static parseFromISO(isoString) {
    if (!isoString) return null
    
    try {
      return dayjs(isoString).utc().tz(SYSTEM_TIMEZONE)
    } catch (error) {
      console.warn('ISO解析失败:', error)
      return null
    }
  }

  /**
   * 获取相对时间描述
   * @param {string|Date|dayjs.Dayjs} time 时间
   * @returns {string} 相对时间描述
   */
  static fromNow(time) {
    if (!time) return ''
    
    try {
      const targetTime = dayjs(time).tz(SYSTEM_TIMEZONE)
      const now = this.now()
      const diffMinutes = now.diff(targetTime, 'minute')
      
      if (diffMinutes < 1) {
        return '刚刚'
      } else if (diffMinutes < 60) {
        return `${diffMinutes}分钟前`
      } else if (diffMinutes < 1440) { // 24小时
        const hours = Math.floor(diffMinutes / 60)
        return `${hours}小时前`
      } else if (diffMinutes < 10080) { // 7天
        const days = Math.floor(diffMinutes / 1440)
        return `${days}天前`
      } else {
        return this.formatServerTime(time)
      }
    } catch (error) {
      console.warn('相对时间计算失败:', error)
      return ''
    }
  }

  /**
   * 判断是否为同一天
   * @param {string|Date|dayjs.Dayjs} date1 日期1
   * @param {string|Date|dayjs.Dayjs} date2 日期2
   * @returns {boolean} 是否同一天
   */
  static isSameDate(date1, date2) {
    if (!date1 || !date2) return false
    
    try {
      return dayjs(date1).tz(SYSTEM_TIMEZONE).isSame(
        dayjs(date2).tz(SYSTEM_TIMEZONE), 'day'
      )
    } catch (error) {
      return false
    }
  }

  /**
   * 判断是否为今天
   * @param {string|Date|dayjs.Dayjs} date 日期
   * @returns {boolean} 是否为今天
   */
  static isToday(date) {
    return this.isSameDate(date, this.today())
  }

  /**
   * 获取时间戳（毫秒）
   * @param {string|Date|dayjs.Dayjs} time 时间
   * @returns {number} 时间戳
   */
  static toTimestamp(time) {
    if (!time) return 0
    
    try {
      return dayjs(time).tz(SYSTEM_TIMEZONE).valueOf()
    } catch (error) {
      return 0
    }
  }

  /**
   * 从时间戳创建时间对象
   * @param {number} timestamp 时间戳（毫秒）
   * @returns {dayjs.Dayjs} dayjs对象
   */
  static fromTimestamp(timestamp) {
    return dayjs(timestamp).tz(SYSTEM_TIMEZONE)
  }

  /**
   * 创建统一的当前时间对象（用于前端显示）
   * @returns {Object} 时间对象
   */
  static createCurrentTimeObject() {
    const now = this.now()
    return {
      timestamp: now.valueOf(),
      formatted: now.format(DEFAULT_DATETIME_FORMAT),
      iso: now.utc().format(ISO_FORMAT),
      relative: '刚刚'
    }
  }

  /**
   * 验证日期格式
   * @param {string} dateString 日期字符串
   * @param {string} format 期望格式
   * @returns {boolean} 是否有效
   */
  static isValidDate(dateString, format = DEFAULT_DATE_FORMAT) {
    if (!dateString) return false
    return dayjs(dateString, format, true).isValid()
  }

  /**
   * 获取日期范围
   * @param {number} days 天数
   * @returns {Object} 日期范围
   */
  static getDateRange(days = 30) {
    const today = this.today()
    const maxDate = today.add(days, 'day')
    
    return {
      min: today.toDate(),
      max: maxDate.toDate(),
      minFormatted: today.format(DEFAULT_DATE_FORMAT),
      maxFormatted: maxDate.format(DEFAULT_DATE_FORMAT)
    }
  }
}

// 导出常用格式常量
export const TIME_FORMATS = {
  DATETIME: DEFAULT_DATETIME_FORMAT,
  DATE: DEFAULT_DATE_FORMAT,
  TIME: 'HH:mm:ss',
  ISO: ISO_FORMAT,
  DISPLAY_DATETIME: 'MM月DD日 HH:mm',
  DISPLAY_DATE: 'MM月DD日'
}

// 导出时区常量
export const TIMEZONES = {
  SYSTEM: SYSTEM_TIMEZONE,
  UTC: 'UTC'
}

// 默认导出
export default TimeUtils