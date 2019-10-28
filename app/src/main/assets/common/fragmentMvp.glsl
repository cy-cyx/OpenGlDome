#version 300 es

precision highp float;

in vec4 gColor;

out vec4 g_Color;

void main() {
    g_Color = gColor;
}
