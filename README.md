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

![Image text](https://github.com/cy-cyx/OpenGlDome/blob/master/img/QQ图片20191105114256.png)

**utils文件下**

通用工具

