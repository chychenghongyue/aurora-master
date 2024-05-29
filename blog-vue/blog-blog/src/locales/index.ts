import { createI18n } from 'vue-i18n'
import cookies from 'js-cookie'

// 加载本地化消息
function loadLocaleMessages() {
  const locales = require.context('../locales/languages', true, /[A-Za-z0-9-_,\s]+\.json$/i)
  const messages: { [key: string]: any } = {}
  locales.keys().forEach((key) => {
    const matched = key.match(/([A-Za-z0-9-_]+)\./i)
    if (matched && matched.length > 1) {
      const locale = matched[1]
      messages[locale] = locales(key).default || locales(key)
    }
  })
  return messages
}

// 创建 i18n 实例
export const i18n = createI18n({
  legacy: false, // 确保关闭 legacy 模式
  locale: cookies.get('locale') ? String(cookies.get('locale')) : 'en',
  fallbackLocale: cookies.get('locale') ? String(cookies.get('locale')) : 'en',
  messages: loadLocaleMessages(),
})
