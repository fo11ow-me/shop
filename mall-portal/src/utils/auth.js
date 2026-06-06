const KEY = 'portal'

export const getAuth = () => {
  try { return JSON.parse(localStorage.getItem(KEY)) || {} } catch { return {} }
}

export const setAuth = (data) => {
  localStorage.setItem(KEY, JSON.stringify({ ...getAuth(), ...data }))
}

export const removeAuth = () => {
  localStorage.removeItem(KEY)
}
