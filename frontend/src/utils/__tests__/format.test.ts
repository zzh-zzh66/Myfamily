import { describe, it, expect } from 'vitest'
import {
  formatDate,
  formatRelativeTime,
  formatYearRange,
  truncateText,
  getGenerationTitle,
  formatGender,
  formatFileSize
} from '../format'

describe('formatDate', () => {
  it('should format date with default format', () => {
    const date = new Date('2024-01-15T10:30:00')
    expect(formatDate(date)).toBe('2024-01-15')
  })

  it('should format date with custom format', () => {
    const date = new Date('2024-01-15T10:30:00')
    expect(formatDate(date, 'YYYY/MM/DD')).toBe('2024/01/15')
  })

  it('should format date with time', () => {
    const date = new Date('2024-01-15T10:30:45')
    expect(formatDate(date, 'YYYY-MM-DD HH:mm:ss')).toBe('2024-01-15 10:30:45')
  })

  it('should handle string date input', () => {
    expect(formatDate('2024-01-15')).toBe('2024-01-15')
  })

  it('should return empty string for null input', () => {
    expect(formatDate(null as any)).toBe('')
  })

  it('should return empty string for undefined input', () => {
    expect(formatDate(undefined as any)).toBe('')
  })

  it('should return empty string for invalid date', () => {
    expect(formatDate('invalid-date')).toBe('')
  })
})

describe('formatRelativeTime', () => {
  it('should return "刚刚" for very recent time', () => {
    const now = new Date()
    expect(formatRelativeTime(now)).toBe('刚刚')
  })

  it('should return minutes ago', () => {
    const date = new Date(Date.now() - 5 * 60 * 1000)
    expect(formatRelativeTime(date)).toBe('5分钟前')
  })

  it('should return hours ago', () => {
    const date = new Date(Date.now() - 3 * 60 * 60 * 1000)
    expect(formatRelativeTime(date)).toBe('3小时前')
  })

  it('should return days ago', () => {
    const date = new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)
    expect(formatRelativeTime(date)).toBe('2天前')
  })

  it('should return months ago', () => {
    const date = new Date(Date.now() - 45 * 24 * 60 * 60 * 1000)
    expect(formatRelativeTime(date)).toBe('1个月前')
  })

  it('should return years ago', () => {
    const date = new Date(Date.now() - 2 * 365 * 24 * 60 * 60 * 1000)
    expect(formatRelativeTime(date)).toBe('2年前')
  })

  it('should return empty string for null input', () => {
    expect(formatRelativeTime(null as any)).toBe('')
  })
})

describe('formatYearRange', () => {
  it('should format year range with both start and end', () => {
    expect(formatYearRange('1990', '2020')).toBe('1990-2020')
  })

  it('should format year range with Date objects', () => {
    expect(formatYearRange(new Date('1990'), new Date('2020'))).toBe('1990-2020')
  })

  it('should format year range with only start date', () => {
    expect(formatYearRange('1990')).toBe('1990-今')
  })

  it('should format year range with null end', () => {
    expect(formatYearRange('1990', null)).toBe('1990-今')
  })
})

describe('truncateText', () => {
  it('should return original text if shorter than maxLength', () => {
    expect(truncateText('hello', 10)).toBe('hello')
  })

  it('should truncate text if longer than maxLength', () => {
    expect(truncateText('hello world', 5)).toBe('hello...')
  })

  it('should handle equal length', () => {
    expect(truncateText('hello', 5)).toBe('hello')
  })

  it('should handle empty string', () => {
    expect(truncateText('', 10)).toBe('')
  })

  it('should handle null input', () => {
    expect(truncateText(null as any, 10)).toBe('')
  })
})

describe('getGenerationTitle', () => {
  it('should return correct title for early generations', () => {
    expect(getGenerationTitle(0)).toBe('鼻祖')
    expect(getGenerationTitle(1)).toBe('始祖')
    expect(getGenerationTitle(2)).toBe('二世')
    expect(getGenerationTitle(5)).toBe('五世')
  })

  it('should return numbered title for later generations', () => {
    expect(getGenerationTitle(11)).toBe('第11世')
    expect(getGenerationTitle(15)).toBe('第15世')
  })
})

describe('formatGender', () => {
  it('should return "男" for male', () => {
    expect(formatGender('male')).toBe('男')
  })

  it('should return "女" for female', () => {
    expect(formatGender('female')).toBe('女')
  })
})

describe('formatFileSize', () => {
  it('should format bytes', () => {
    expect(formatFileSize(0)).toBe('0 B')
  })

  it('should format kilobytes', () => {
    expect(formatFileSize(1024)).toBe('1 KB')
    expect(formatFileSize(1536)).toBe('1.5 KB')
  })

  it('should format megabytes', () => {
    expect(formatFileSize(1048576)).toBe('1 MB')
    expect(formatFileSize(1572864)).toBe('1.5 MB')
  })

  it('should format gigabytes', () => {
    expect(formatFileSize(1073741824)).toBe('1 GB')
  })

  it('should handle edge cases', () => {
    expect(formatFileSize(1)).toBe('1 B')
    expect(formatFileSize(1023)).toBe('1023 B')
  })
})
