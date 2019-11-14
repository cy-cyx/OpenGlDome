#version 300 es

precision highp float;

in vec4 vert_coord;

out vec4 frag_Color;

uniform sampler2D uTexture;

void main() {
    frag_Color = texture(uTexture, vert_coord.xy);
}
