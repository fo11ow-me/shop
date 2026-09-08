import {defineStore} from 'pinia'
import {getAuth, setAuth} from '@/utils/auth'

const defaultTags = [{id: 0, name: '首页', path: '/home', code: 'home'}]

export const useTagStore = defineStore('tag', {
    state: () => ({
        tagList: getAuth().tagList || defaultTags
    }),
    actions: {
        addTag(tag) {
            if (tag.code === 'home' || this.tagList.some(t => t.code === tag.code)) return
            this.tagList.push(tag)
            setAuth({tagList: this.tagList})
        },
        closeTag(code) {
            this.tagList = this.tagList.filter(t => t.code !== code || t.code === 'home')
            setAuth({tagList: this.tagList})
        },
        clearTag() {
            this.tagList = [...defaultTags]
            setAuth({tagList: null})
        }
    }
})
