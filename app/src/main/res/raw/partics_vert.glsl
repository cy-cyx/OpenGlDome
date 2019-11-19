#version 300 es

in vec4 vStartPoint;
in vec4 vEndPoint;
in float vContinuedTime;

uniform float uCurTime;

out float vert_continueTime;

void main() {
    if (uCurTime <= vContinuedTime){
        gl_Position = vStartPoint + (vEndPoint - vStartPoint) * uCurTime / vContinuedTime;
        gl_PointSize = 50.0f - 30.f * uCurTime / vContinuedTime;
    } else {
        gl_Position = vec4(-1000, -1000, 0, 0);
    }

    vert_continueTime = vContinuedTime;
}
