import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    currentConversation: null,
    messages: [],
    isLoading: false,
    isConnected: false,
    eventSource: null
  }),
  
  actions: {
    setConversations(conversations) {
      this.conversations = conversations
    },
    
    addConversation(conversation) {
      this.conversations.unshift(conversation)
    },
    
    removeConversation(conversationId) {
      this.conversations = this.conversations.filter(c => c.id !== conversationId)
      if (this.currentConversation?.id === conversationId) {
        this.currentConversation = null
        this.messages = []
      }
    },
    
    setCurrentConversation(conversation) {
      this.currentConversation = conversation
    },
    
    setMessages(messages) {
      this.messages = messages
    },
    
    addMessage(message) {
      this.messages.push(message)
    },
    
    updateLastMessage(content) {
      if (this.messages.length > 0) {
        const lastMessage = this.messages[this.messages.length - 1]
        if (lastMessage.role === 'assistant') {
          lastMessage.content += content
        }
      }
    },
    
    setLoading(loading) {
      this.isLoading = loading
    },
    
    connectSSE(conversationId) {
      if (this.eventSource) {
        this.eventSource.close()
      }
      
      this.eventSource = new EventSource(`/api/chat/stream/${conversationId}`)
      this.isConnected = true
      
      return this.eventSource
    },
    
    disconnectSSE() {
      if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
      }
      this.isConnected = false
    }
  }
})