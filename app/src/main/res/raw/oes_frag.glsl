#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;

in vec2 vert_texcoord;
uniform samplerExternalOES uSourceImage;

out vec4 frag_Color;

void main() {
    frag_Color = texture(uSourceImage, vert_texcoord);
}