// copied from chat/frontend/src/stores/chat.js
import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    currentConversation: null,
    messages: [],
    isLoading: false,
    isConnected: false
  }),
  actions: {
    setConversations(list) { this.conversations = list || [] },
    setCurrentConversation(conv) { this.currentConversation = conv || null },
    setMessages(list) { this.messages = list || [] },
    addMessage(msg) { this.messages.push(msg) },
    setLoading(v) { this.isLoading = !!v },
    setConnected(v) { this.isConnected = !!v }
  }
})

