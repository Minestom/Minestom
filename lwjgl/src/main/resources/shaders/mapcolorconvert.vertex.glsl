#version 330

layout(location = 0) in vec2 pos;

out vec2 fragCoords;

void main() {
    fragCoords = (pos+vec2(1.0))/2.0;
    gl_Position = vec4(pos, 0.0, 1.0);
}