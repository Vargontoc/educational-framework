import { ref } from 'vue'
import { defineStore } from 'pinia'

type ChannelStatus = 'disconnected' | 'connecting' | 'connected'

export const useWSStore = defineStore('ws', () => {
  const gameChannelStatus = ref<ChannelStatus>('disconnected')
  const parentChannelStatus = ref<ChannelStatus>('disconnected')

  return { gameChannelStatus, parentChannelStatus }
})
