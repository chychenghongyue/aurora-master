export default {
  qqLogin: {
    QQ_APP_ID: '102076958',
    QQ_REDIRECT_URI: 'http://www.chyblog.top/oauth/login/qq'
  },
  routes: [
    {
      name: 'Home',
      path: '/',
      i18n: {
        cn: '首页',
        en: 'Home'
      },
      children: []
    },
    {
      name: 'Tags',
      path: '/tags',
      i18n: {
        cn: '标签',
        en: 'Tags'
      },
      children: []
    },
    {
      name: 'Archives',
      path: '/archives',
      i18n: {
        cn: '归档',
        en: 'Archives'
      },
      children: []
    },
    {
      name: 'Talks',
      path: '/talks',
      i18n: {
        cn: '公告',
        en: 'announcement'
      },
      children: []
    },
    // {
    //   name: 'About',
    //   path: '/about',
    //   i18n: {
    //     cn: '关于',
    //     en: 'About'
    //   },
    //   children: []
    // },
    // {
    //   name: 'Message',
    //   path: '/message',
    //   i18n: {
    //     cn: '留言',
    //     en: 'Message'
    //   },
    //   children: []
    // },
    // {
    //   name: 'Friends',
    //   path: '/friends',
    //   i18n: {
    //     cn: '友链',
    //     en: 'Friends'
    //   },
    //   children: []
    // }
  ]
}
