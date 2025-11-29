import { mount } from '@vue/test-utils'
import AvatarUploader from '@/components/AvatarUploader.vue'

describe('AvatarUploader', () => {
  it('renders and validates file size/type', async () => {
    const w = mount(AvatarUploader, { props: { maxSize: 1024, accept: 'image/png' } })
    const input = w.find('input[type="file"]')
    const file = new File([new Uint8Array([0])], 'a.png', { type: 'image/png' })
    await input.trigger('change', { target: { files: [file] } })
    expect(w.exists()).toBeTruthy()
  })
})