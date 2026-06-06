const KEY = 'admin'

export const getAuth = () => {
    return JSON.parse(localStorage.getItem(KEY)) || {}
}

export const setAuth = (data) => {
    localStorage.setItem(KEY, JSON.stringify({...getAuth(), ...data}))
}

export const removeAuth = () => {
    localStorage.removeItem(KEY)
}
