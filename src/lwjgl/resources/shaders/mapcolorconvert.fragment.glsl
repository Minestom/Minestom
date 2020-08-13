#version 330

in vec2 fragCoords;
out vec4 fragColor;

uniform sampler2D frame;
uniform sampler2D palette;
uniform float paletteSize;

void main() {
    vec2 uv = fragCoords;
    uv.y = -uv.y;
    vec3 fragmentColor = texture(frame, uv).rgb;

    // render in map colors
    int closest = 0;
    uint closestDistance = uint(2147483647);
    for(int i = 4; i < paletteSize; i++) {
        vec3 mapColor = texture(palette, vec2((i+0.5f)/paletteSize, 0.0)).rgb;
        int dr = int((mapColor.r - fragmentColor.r)*255);
        int dg = int((mapColor.g - fragmentColor.g)*255);
        int db = int((mapColor.b - fragmentColor.b)*255);

        uint d = uint(dr*dr)+uint(dg*dg)+uint(db*db);
        if(d < closestDistance) {
            closestDistance = d;
            closest = i;
        }
    }

    fragColor = vec4(closest/255.0, closest/255.0, closest/255.0, 1.0);
}