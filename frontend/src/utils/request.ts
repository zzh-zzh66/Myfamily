import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 根据业务码处理
    if (res.code === 200 || res.code === 201 || res.code === 0) {
      return response.data
    }

    // 处理特定的业务错误
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push({ name: 'Login' })
      return Promise.reject(new Error(res.message || '登录已过期'))
    }

    if (res.code === 403) {
      ElMessage.error('没有权限访问该资源')
      return Promise.reject(new Error(res.message || '没有权限'))
    }

    if (res.code === 404) {
      ElMessage.error('请求的资源不存在')
      return Promise.reject(new Error(res.message || '资源不存在'))
    }

    if (res.code === 500) {
      ElMessage.error('服务器错误，请稍后重试')
      return Promise.reject(new Error(res.message || '服务器错误'))
    }

    // 其他业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 处理网络错误
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误')
          break
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          router.push({ name: 'Login' })
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误，请稍后重试')
          break
        case 502:
          ElMessage.error('网关错误，请稍后重试')
          break
        case 503:
          ElMessage.error('服务暂时不可用，请稍后重试')
          break
        case 504:
          ElMessage.error('网关超时，请稍后重试')
          break
        default:
          ElMessage.error(error.message || '网络错误')
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置出错
      ElMessage.error(error.message || '请求配置错误')
    }

    return Promise.reject(error)
  }
)

// 封装GET请求
export function get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request.get(url, config).then((res) => res.data)
}

// 封装POST请求
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request.post(url, data, config).then((res) => res.data)
}

// 封装PUT请求
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request.put(url, data, config).then((res) => res.data)
}

// 封装DELETE请求
export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request.delete(url, config).then((res) => res.data)
}

export default request
