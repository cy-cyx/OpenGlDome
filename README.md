项目中现有的功能模块

**1、fbo文件下**

使用帧缓冲区，使用一个纹理作为帧缓冲的颜色缓冲区

注意：安卓的纹理的原点是在左上角，fbo的纹理的原点是在左下角

**2、blend文件下**

混合模式的使用

**3、light文件下**

冯氏光照模型

分为 *环境光  反射光 镜面高光*

**4、mvp文件下**

视角-模式矩阵

**5、texture文件下**

纹理贴图

*要注意纹理单元和纹理的概念，纹理默认绑定纹理单元0*

**6、shadow文件**

阴影贴图

两步渲染：
1、使用帧缓冲区附着深度纹理，通过光的角度，绘制一遍物体
2、画阴影平面，通过光的角度，利用上一次深度纹理，对比平面上点的深度，判断是否被遮盖，
确定是否在阴影里面
3、最后画光照的物体（照搬*冯式光照模式*）

阴影部分使用PCF优化，模糊过渡

![Image text](https://github.com/cy-cyx/OpenGlDome/blob/master/img/QQ图片20191115103923.png)

**7、用obj文件显示模型**

(1) 解obj文件数据（测试素材下载网站 https://free3d.com/3d-models/obj）

(2) 按照点、纹理、法线信息绘制模型

(3) 利用法线加上光照 （法线贴图）

![Image text](https://github.com/cy-cyx/OpenGlDome/blob/master/img/QQ图片20191115103942.png)

**8、粒子系统**

将一堆粒子起点、终点、持续时间 当作顶点属性输入 再传入当前时间作为变量
在片元着色器进行计算（代替在代码中更新点位置）

![Image text](https://github.com/cy-cyx/OpenGlDome/blob/master/img/QQ%E5%9B%BE%E7%89%8720191119015745.png)

**9、GLTextureView**

仿照GlSurfaceView的写法，大体结构一致

（1）主线程和gl线程之间，指令发出到执行需要等待（wait notify）

（2）gl线程使用了嵌套循环写法，内循环加锁（在内循环中进行条件的判断 生命周期、surface） 在外循环执行渲染

（3）EGLHelp中封装EGL环境构建所需要的所有逻辑

（4）充分利用TextureView的作为一个View的生命流程

**10、beauty文件下**

颜色滤镜

![img](https://github.com/cy-cyx/OpenGlDome/blob/master/img/201911261803.gif)

**11、egl包下**

构建egl环境需要的最基本代码

**utils文件下**

通用工具

