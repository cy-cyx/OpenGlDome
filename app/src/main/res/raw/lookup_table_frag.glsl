#version 300 es

precision highp float;

in vec2 vert_texcoord;

uniform sampler2D uSourceImage;
uniform sampler2D uLookupTexture;

uniform float uAlpha;

out vec4 frag_color;

/*
* table长宽为64*64 通过蓝色通道进行颜色查找
*/
vec4 getLookUpColor(vec4 textureColor, sampler2D lookUpTable) {

    float blueColor = textureColor.b * 15.0;

    vec2 quad1;
    quad1.y = floor(floor(blueColor) / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);

    vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);

    vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

    vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

    vec4 newColor1 = texture(lookUpTable, texPos1);
    vec4 newColor2 = texture(lookUpTable, texPos2);

    return mix(newColor1, newColor2, fract(blueColor));
}

void main() {

    vec4 textureColor = texture(uSourceImage, vert_texcoord);
    vec4 newColor = getLookUpColor(textureColor, uLookupTexture);

    frag_color = mix(textureColor, vec4(newColor.rgb, textureColor.w), uAlpha);// 内建函数 mix（x,y,a）  x * (1 - a) + y * a
}