## 安装配置参考

https://blog.csdn.net/dream_summer/article/details/108867317?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522163798343216780261975280%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fall.%2522%257D&request_id=163798343216780261975280&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~first_rank_ecpm_v1~rank_v31_ecpm-1-108867317.pc_search_result_cache&utm_term=vue%E5%AE%89%E8%A3%85&spm=1018.2226.3001.4187

## vue项目结构

1、build：构建脚本目录

1）build.js ==> 生产环境构建脚本；
2）check-versions.js ==> 检查npm，node.js版本；
3）utils.js ==> 构建相关工具方法；
4）vue-loader.conf.js ==> 配置了css加载器以及编译css之后自动添加 前缀；
5）webpack.base.conf.js ==> webpack基本配置；
6）webpack.dev.conf.js ==> webpack开发环境配置；
7）webpack.prod.conf.js ==> webpack生产环境配置；

2、config：项目配置

1）dev.env.js ==> 开发环境变量；
2）index.js ==> 项目配置文件；
3）prod.env.js ==> 生产环境变量；

3、node_modules：npm 加载的项目依赖模块

4、src：这里是我们要开发的目录，基本上要做的事情都在这个目录里。里面包含了几个目录及文件：

1）assets：资源目录，放置一些图片或者公共js、公共css。但是因为它们属于代码目录下，所以可以用 webpack 来操作和处理。意思就是你可以使用一些预处理比如 Sass/SCSS 或者 Stylus。
2）components：用来存放自定义组件的目录，目前里面会有一个示例组件。
3）router：前端路由目录，我们需要配置的路由路径写在index.js里面；
4）App.vue：根组件；这是 Vue 应用的根节点组件，往下看可以了解更多关注 Vue 组件的信息。
5）main.js：应用的入口文件。主要是引入vue框架，根组件及路由设置，并且定义vue实例，即初始化 Vue 应用并且制定将应用挂载到index.html 文件中的哪个 HTML 元素上。通常还会做一些注册全局组件或者添额外的 Vue 库的操作。

5、static：静态资源目录，如图片、字体等。不会被webpack构建

6、index.html：首页入口文件，可以添加一些 meta 信息等。 这是应用的模板文件，Vue 应用会通过这个 HTML 页面来运行，也可以通过 lodash 这种模板语法在这个文件里插值。 注意：这个不是负责管理页面最终展示的模板，而是管理 Vue 应用之外的静态 HTML 文件，一般只有在用到一些高级功能的时候才会修改这个文件。

7、package.json：npm包配置文件，定义了项目的npm脚本，依赖包等信息

8、README.md：项目的说明文档，markdown 格式

9、.xxxx文件：这些是一些配置文件，包括语法配置，git配置等

1）.babelrc:babel编译参数
2）.editorconfig:编辑器相关的配置，代码格式
3）.eslintignore : 配置需要或略的路径，一般build、config、dist、test等目录都会配置忽略
4）.eslintrc.js : 配置代码格式风格检查规则
5）.gitignore:git上传需要忽略的文件配置
6）.postcssrc.js ：css转换工具

## 基础语法参考

https://blog.csdn.net/dream_summer/article/details/116752074