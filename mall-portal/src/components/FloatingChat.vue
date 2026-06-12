<template>
  <div class="floating-chat">
    <!-- 悬浮按钮 -->
    <button v-if="!isOpen" class="chat-trigger" @click="openChat" title="智能客服">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        <line x1="9" y1="9" x2="15" y2="9"/>
        <line x1="9" y1="13" x2="13" y2="13"/>
      </svg>
    </button>

    <!-- 对话面板 -->
    <transition name="chat-slide">
      <div v-if="isOpen" class="chat-panel">
        <div class="chat-header">
          <span>💬 智能客服</span>
          <div class="chat-header-actions">
            <button class="chat-reset" @click="resetChat" title="新对话">↺</button>
            <button class="chat-close" @click="isOpen = false">✕</button>
          </div>
        </div>

        <div class="chat-body" ref="bodyRef">
          <div v-for="(msg, i) in messages" :key="i"
            :class="['msg-row', msg.role === 'user' ? 'msg-user' : 'msg-bot']">
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="msg-row msg-bot">
            <div class="msg-bubble typing"><span></span><span></span><span></span></div>
          </div>
        </div>

        <form class="chat-input-area" @submit.prevent="send">
          <input v-model="input" placeholder="输入您的问题..." class="chat-input"
            :disabled="loading" autocomplete="off" />
          <button type="submit" class="chat-send" :disabled="loading || !input.trim()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M2 21l21-9L2 3v7l15 2-15 2v7z"/>
            </svg>
          </button>
        </form>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const isOpen = ref(false)
const input = ref('')
const messages = ref([])
const loading = ref(false)
const bodyRef = ref(null)

// P0: 生成会话 ID，维持多轮对话上下文
const chatId = ref('chat-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8))

const scrollToBottom = async () => {
  await nextTick()
  if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
}

const openChat = () => {
  isOpen.value = true
  if (messages.value.length === 0) {
    messages.value.push({ role: 'bot', content: '你好！我是 mall 商城智能客服。你可以问我商品信息、热销排行、订单查询等问题~' })
  }
  scrollToBottom()
}

const resetChat = async () => {
  try {
    await fetch('/dev/ai/chat/' + chatId.value, { method: 'DELETE' })
  } catch {}
  chatId.value = 'chat-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8)
  messages.value = []
  loading.value = false
  openChat()
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  // 添加 bot 占位消息，用于流式更新
  const botIdx = messages.value.length
  messages.value.push({ role: 'bot', content: '' })

  try {
    const params = 'message=' + encodeURIComponent(text) + '&chatId=' + encodeURIComponent(chatId.value)
    const resp = await fetch('/dev/ai/chat?' + params, {
      method: 'POST',
      headers: { 'satoken': localStorage.getItem('satoken') || '' }
    })
    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        const data = line.startsWith('data:') ? line.slice(5).trim() : ''
        if (!data) continue
        if (data === '[DONE]') break
        if (data.startsWith('[ERROR]')) {
          messages.value[botIdx].content = '抱歉，出了点问题：' + data.slice(8)
          break
        }
        messages.value[botIdx].content += data
      }
      await nextTick()
      scrollToBottom()
    }
  } catch {
    messages.value[botIdx].content = '网络异常，请稍后重试'
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.floating-chat { position: fixed; bottom: 24px; right: 24px; z-index: 9999; font-family: 'Microsoft YaHei', sans-serif; }

/* 悬浮按钮 */
.chat-trigger { width: 52px; height: 52px; border-radius: 50%; background: #A10000; color: #fff;
  border: none; cursor: pointer; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 16px rgba(161,0,0,0.35); transition: all .2s; }
.chat-trigger:hover { transform: scale(1.08); box-shadow: 0 6px 24px rgba(161,0,0,0.45); }

/* 对话面板 */
.chat-panel { position: absolute; bottom: 0; right: 0; width: 360px; height: 480px;
  background: #fff; border-radius: 12px; box-shadow: 0 8px 40px rgba(0,0,0,0.12);
  display: flex; flex-direction: column; overflow: hidden; }

.chat-header { display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; background: linear-gradient(135deg, #A10000, #C10000); color: #fff;
  font-size: 15px; font-weight: 500; }
.chat-header-actions { display: flex; gap: 6px; align-items: center; }
.chat-reset, .chat-close { background: none; border: none; color: #fff; font-size: 18px;
  cursor: pointer; opacity: 0.8; transition: opacity .15s; }
.chat-reset:hover, .chat-close:hover { opacity: 1; }

/* 消息区 */
.chat-body { flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 10px;
  background: #f8f9fa; }
.msg-row { display: flex; }
.msg-user { justify-content: flex-end; }
.msg-bot { justify-content: flex-start; }
.msg-bubble { max-width: 78%; padding: 10px 14px; border-radius: 16px; font-size: 13px;
  line-height: 1.6; word-break: break-word; }
.msg-user .msg-bubble { background: #A10000; color: #fff; border-bottom-right-radius: 4px; }
.msg-bot .msg-bubble { background: #fff; color: #333; border: 1px solid #eee; border-bottom-left-radius: 4px; }

/* 打字动画 */
.typing { display: flex; align-items: center; gap: 4px; padding: 14px 16px; }
.typing span { width: 7px; height: 7px; border-radius: 50%; background: #bbb;
  animation: typing 1.4s infinite ease-in-out; }
.typing span:nth-child(2) { animation-delay: .2s; }
.typing span:nth-child(3) { animation-delay: .4s; }
@keyframes typing { 0%,60%,100% { transform: translateY(0); } 30% { transform: translateY(-6px); } }

/* 输入区 */
.chat-input-area { display: flex; padding: 10px 12px; border-top: 1px solid #eee; gap: 8px; }
.chat-input { flex: 1; border: 1px solid #e0e0e0; border-radius: 20px; padding: 8px 14px;
  font-size: 13px; outline: none; transition: border-color .2s; }
.chat-input:focus { border-color: #A10000; }
.chat-send { width: 36px; height: 36px; border-radius: 50%; background: #A10000; color: #fff;
  border: none; cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background .2s; flex-shrink: 0; }
.chat-send:hover:not(:disabled) { background: #C10000; }
.chat-send:disabled { background: #ccc; cursor: not-allowed; }

/* 动画 */
.chat-slide-enter-active { transition: all .25s ease-out; }
.chat-slide-leave-active { transition: all .2s ease-in; }
.chat-slide-enter-from, .chat-slide-leave-to { opacity: 0; transform: translateY(12px) scale(0.95); }
</style>
