#version 300 es

precision highp float;

in float vert_continueTime;

uniform sampler2D uTexture;

uniform float uCurTime;

out vec4 frag_color;

void main() {
    vec4 color = texture(uTexture, gl_PointCoord);
    if (uCurTime <= vert_continueTime)
    color.a = color.a * (1.f - uCurTime / vert_continueTime);
    frag_color = color;
}
