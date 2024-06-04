module.exports = {
  // 密码MD5加密参数
  cryptoKey: 'lingshulian',

  // 数据库配置
  dbConfig: {
    host: "47.95.215.231",
    user: "root", // 数据库登录用户
    password: "chy211985CHY@!!(*%", // 数据库登录密码
    database: "blog" // 数据库名称
  },

  // s3对象存储配置 https://www.lingshulian.com 超低成本的对象存储平台
  s3Config: {
    config: {
      credentials: {
        accessKeyId: 'a5281d03705c92f971f7ecbbc4327ced', // secretId
        secretAccessKey: '8d13b863ae15c41506b7017b878845c5631c2ca9002dad7e698f6100f2d4896d' // secretKey
      },
      endpoint: 'https://s3-us-east-1.ossfiles.com', // https:// + 获取的的端点
      region: 'us-east-1'
    },
    // 上传目录 可自定义
    path: 'chat',
    // 上传桶
    bucket: "lingshuliantest" // 存储桶
  },

  // jwt认证配置
  jwtConfig: {
    // 加密和解密 Token 的秘钥
    secret: 'lingshulian',
    // 加密方式
    algorithms: ["HS256"]
  },

  // token过期时间
  expiresIn: '8h',

  // 发送邮箱配置
  emailConfig: {
    host: "smtp.qq.com",//邮箱服务器  这里我用的QQ邮箱
    port: 465,//邮箱使用端口
    secure: true,//是否使用默认的465端口
    auth: {
      user: "3319023171@qq.com", // 发送方邮箱地址
      pass: "yvrlgpqwifjncjgf" // smtp 验证码
    }
  },

  // 用户默认头像配置
  newUserConfig: {
    avatar: ["/avatar/1.jpg","/avatar/2.jpg","/avatar/3.jpg","/avatar/4.jpg","/avatar/5.jpg"],
  }
}