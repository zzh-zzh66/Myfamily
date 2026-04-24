// 格式化日期
export function formatDate(date: string | Date, format = 'YYYY-MM-DD'): string {
  if (!date) return ''

  const d = typeof date === 'string' ? new Date(date) : date

  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 格式化相对时间
export function formatRelativeTime(date: string | Date): string {
  if (!date) return ''

  const d = typeof date === 'string' ? new Date(date) : date
  const now = new Date()
  const diff = now.getTime() - d.getTime()

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)

  if (years > 0) return `${years}年前`
  if (months > 0) return `${months}个月前`
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

// 格式化年份范围
export function formatYearRange(start: string | Date, end?: string | Date | null): string {
  const startYear = new Date(start).getFullYear()
  if (!end) return `${startYear}-今`
  const endYear = new Date(end).getFullYear()
  return `${startYear}-${endYear}`
}

// 截断文本
export function truncateText(text: string, maxLength: number): string {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 生成辈分称呼
export function getGenerationTitle(generation: number): string {
  const titles = ['鼻祖', '始祖', '二世', '三世', '四世', '五世', '六世', '七世', '八世', '九世', '十世']
  if (generation < titles.length) {
    return titles[generation]
  }
  return `第${generation}世`
}

// 性别格式化
export function formatGender(gender: 'male' | 'female'): string {
  return gender === 'male' ? '男' : '女'
}

// 文件大小格式化
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
