#version 300 es

precision highp float;

in vec4 vert_Color;

out vec4 frag_Color;

void main() {
    frag_Color= vert_Color;
}
