
// copied from chat/frontend/src/stores/chat.js
import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    currentConversation: null,
    messages: [],
    isLoading: false,
    isConnected: false,
    references: [],
    availableModels: [],
    selectedProvider: '',
    selectedModel: ''
  }),
  actions: {
    setConversations(list) { this.conversations = list || [] },
    setCurrentConversation(conv) { this.currentConversation = conv || null },
    setMessages(list) { this.messages = list || [] },
    addMessage(msg) { this.messages.push(msg) },
    setLoading(v) { this.isLoading = !!v },
    setConnected(v) { this.isConnected = !!v },
    setReferences(list) { this.references = Array.isArray(list) ? list : [] },
    setAvailableModels(list) { this.availableModels = Array.isArray(list) ? list : [] },
    setSelectedProvider(value) { this.selectedProvider = value || '' },
    setSelectedModel(value) { this.selectedModel = value || '' }
  }
})
