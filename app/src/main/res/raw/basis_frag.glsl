#version 300 es

precision highp float;

in vec4 vert_color;

out vec4 frag_color;

void main() {
    frag_color = vert_color;
}