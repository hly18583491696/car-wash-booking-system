<template>
  <div class="avatar-uploader" :class="{ dragOver }" @dragover.prevent="onDragOver" @dragleave.prevent="onDragLeave" @drop.prevent="onDrop">
    <el-avatar :size="size" :src="previewUrl || src">
      <el-icon size="40"><User /></el-icon>
    </el-avatar>
    <div class="actions">
      <el-button size="small" circle @click="selectFile"><el-icon><Camera /></el-icon></el-button>
    </div>
    <input ref="fileInput" type="file" :accept="accept" class="hidden-input" @change="onFileChange" />

    <el-dialog v-model="cropperOpen" title="裁剪头像" width="520px">
      <div class="cropper-area">
        <img v-if="previewUrl" :src="previewUrl" ref="cropImg" class="crop-image" />
      </div>
      <template #footer>
        <el-button @click="cropperOpen=false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="confirmCrop">保存并上传</el-button>
      </template>
    </el-dialog>

    <el-progress v-if="uploading" :percentage="progress" :stroke-width="6" style="margin-top:8px" />
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request.js'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

export default {
  name: 'AvatarUploader',
  props: {
    src: { type: String, default: '' },
    uploadUrl: { type: String, default: '/api/user/avatar/upload' },
    size: { type: Number, default: 80 },
    maxSize: { type: Number, default: 2 * 1024 * 1024 },
    accept: { type: String, default: 'image/jpeg,image/png,image/gif' }
  },
  emits: ['success','error'],
  setup(props, { emit }) {
    const fileInput = ref(null)
    const cropImg = ref(null)
    const previewUrl = ref('')
    const cropperOpen = ref(false)
    const cropper = ref(null)
    const uploading = ref(false)
    const progress = ref(0)
    const dragOver = ref(false)
    const csrfToken = ref('')

    const selectFile = () => fileInput.value && fileInput.value.click()
    const onDragOver = () => dragOver.value = true
    const onDragLeave = () => dragOver.value = false
    const onDrop = (e) => { dragOver.value = false; handleFile(e.dataTransfer.files?.[0]) }
    const onFileChange = (e) => handleFile(e.target.files?.[0])

    const handleFile = (file) => {
      if (!file) return
      if (!props.accept.split(',').includes(file.type)) {
        ElMessage.error('仅支持 JPG/PNG/GIF 图片')
        return
      }
      if (file.size > props.maxSize) {
        ElMessage.error('图片大小需在 2MB 以内')
        return
      }
      previewUrl.value = URL.createObjectURL(file)
      cropperOpen.value = true
    }

    // 监听对话框打开状态，延迟初始化Cropper
    watch(cropperOpen, (newVal) => {
      if (newVal) {
        // 对话框打开后，等待DOM渲染完成再初始化
        nextTick(() => {
          setTimeout(() => initCropper(), 300)
        })
      } else {
        // 对话框关闭时销毁Cropper实例
        destroyCropper()
      }
    })

    const destroyCropper = () => {
      if (cropper.value) {
        try {
          cropper.value.destroy()
        } catch (e) {
          console.warn('Cropper销毁失败:', e)
        }
        cropper.value = null
      }
    }

    const initCropper = () => {
      destroyCropper()
      
      if (!cropImg.value) {
        console.warn('cropImg元素未就绪，延迟初始化')
        setTimeout(() => initCropper(), 100)
        return
      }
      
      try {
        cropper.value = new Cropper(cropImg.value, {
          aspectRatio: 1,
          viewMode: 1,
          dragMode: 'move',
          autoCropArea: 0.8,
          responsive: true
        })
      } catch (e) {
        console.error('Cropper初始化失败:', e)
        ElMessage.error('图片裁剪组件初始化失败')
      }
    }

    const fetchCsrf = async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录后再上传头像')
        throw new Error('NOT_AUTHENTICATED')
      }
      const r = await request.post('/auth/csrf', {})
      if (r && r.code === 200) csrfToken.value = r.data
      else throw new Error(r?.message || '获取CSRF令牌失败')
    }

    const confirmCrop = async () => {
      // 检查Cropper实例是否有效
      if (!cropper.value || typeof cropper.value.getCroppedCanvas !== 'function') {
        ElMessage.error('图片裁剪组件未就绪，请重新选择图片')
        return
      }
      
      try {
        uploading.value = true
        progress.value = 0
        await fetchCsrf()
        
        const canvas = cropper.value.getCroppedCanvas({ width: 256, height: 256 })
        if (!canvas) {
          throw new Error('裁剪图片失败')
        }
        
        const blob = await new Promise((resolve, reject) => {
          canvas.toBlob((b) => {
            if (b) resolve(b)
            else reject(new Error('生成图片失败'))
          }, 'image/jpeg', 0.9)
        })
        const fd = new FormData()
        fd.append('file', blob, 'avatar.jpg')
        fd.append('fileName', 'avatar')
        const r = await request.post(props.uploadUrl, fd, {
          headers: { 'X-CSRF-Token': csrfToken.value },
          onUploadProgress: (e) => { if (e.total) progress.value = Math.floor((e.loaded / e.total) * 100) }
        })
        if (r && r.code === 200) {
          emit('success', r.data.avatarUrl)
          ElMessage.success('头像上传成功')
          cropperOpen.value = false
        } else {
          const msg = r?.message || '上传失败'
          emit('error', msg)
          ElMessage.error(msg)
        }
      } catch (err) {
        if (err?.response?.status === 401) {
          ElMessage.warning('登录已过期，请重新登录后上传头像')
          const current = window.location.pathname + window.location.search
          window.location.href = `/login?redirect=${encodeURIComponent(current)}`
          return
        }
        emit('error', err.message)
        ElMessage.error(err.message || '上传失败')
      } finally {
        uploading.value = false
        progress.value = 0
      }
    }

    onMounted(() => {})
    return { fileInput, cropImg, previewUrl, cropperOpen, uploading, progress, dragOver, selectFile, onDragOver, onDragLeave, onDrop, onFileChange, confirmCrop }
  }
}
</script>

<style scoped>
.avatar-uploader { position: relative; display: inline-flex; align-items: center; gap: 8px; }
.hidden-input { display: none; }
.actions { position: absolute; right: -6px; bottom: -6px; }
.cropper-area { width: 100%; max-height: 360px; overflow: auto; }
.crop-image { max-width: 100%; }
.dragOver { outline: 2px dashed var(--primary-color); border-radius: 50%; }
</style>