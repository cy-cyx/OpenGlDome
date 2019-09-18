#version 300 es

precision highp float;

in vec4 v_Color;

out vec4 g_Color;

void main() {
    g_Color = v_Color;
}
