#version 300 es

precision highp float;

in vec4 frag_LightPosition;

uniform highp sampler2DShadow uDepthTexture;

out vec4 frag_Color;

vec4 color = vec4(1.f, 1.f, 1.f, 1.f);

void main() {

    // 标准化
    vec3 coord = frag_LightPosition.xyz / frag_LightPosition.w;

    // [-1,1] 转化 [0,1]
    coord = coord * .5f + .5f;

    // 1 代表不在阴影范围之内 0 代表在阴影范围之内
    float f = texture(uDepthTexture, coord);

    // PCF 优化
//    f = 0.f;
//    float pixelSixe = 0.02f;
//
//    float x, y;
//    for (x = -1.0;x <= 1.0;x += 1.0){
//        for (f = -1.0; y <= 1.0;y += 1.0){
//            vec3 offset = vec3(x * pixelSixe, y * pixelSixe, 0);
//            f += texture(uDepthTexture, coord + offset);
//        }
//    }
//
//    // 除于9
//    f /= 9.f;
    
    frag_Color = color * f;
}
