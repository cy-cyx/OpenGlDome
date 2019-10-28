#version 300 es

precision highp float;

in vec4 vert_normal;// 法线
in vec4 vert_position;

uniform vec4 uObjectColor;// 物体颜色
uniform vec4 uLightColor;// 光颜色
uniform vec4 uLightDir;// 光的方向
uniform vec4 uEyeLocal;// 眼睛位置

out vec4 frag_color;

/*
冯式光照模型
*/
void main() {

    // 环境光，环境必定存在环境光照
    vec4 ambienceColor;
    float ambientStrength = 0.2f;
    vec3 ambient = ambientStrength * uLightColor.rgb;
    vec3 result = ambient * uObjectColor.rgb;
    ambienceColor = vec4(result, 1.f);

    // 反射光 光与法线的夹角
    vec4 normal = normalize(vert_normal);
    vec4 lightdir = normalize(uLightDir);
    float diff = max(dot(normal, lightdir), 0.0f);//负数没有意义
    vec4 diffusColor = diff * uLightColor;

    // 镜面高光（Specular）  就是反射光和眼睛成的比例
    float specularStrength = 0.5f;
    vec4 viewDir = normalize(vert_position - uEyeLocal);// 眼睛方向
    vec4 reflectDir = normalize(reflect(uLightDir, vert_normal));// 反射光方向
    float spec  = pow(max(dot(viewDir, reflectDir), 0.f), 4.f);/*平方值与网上不同32*/
    vec4 specularColor = specularStrength * spec * uLightColor;

    frag_color = ambienceColor + diffusColor + specularColor;
}
