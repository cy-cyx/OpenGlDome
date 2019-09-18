#version 300 es

precision highp float;

in vec4 gColor;
out vec4 frag_color;

void main() {
    frag_color = gColor;
}
