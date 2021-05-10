#version 330

in vec2 uv;

out vec4 fragColor;

uniform sampler2D box;

void main() {
    vec3 vertexColor = texture(box, uv).rgb;
    fragColor = vec4(vertexColor, 1.0);
}