#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform ExposureColorConfig {
    vec4 Mul;
    vec4 Add;
    float Saturation;
    float Contrast;
};

out vec4 fragColor;

// This shader is a duplicate of minecraft:color_convolve but with Contrast and without InSize to stop the warnings about not finding uniform InSize.
void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    vec3 RGB = InTexel.rgb * Mul.rgb + Add.rgb;

    //Saturation
    vec3 Gray = vec3(0.3, 0.59, 0.11);
    float Luma = dot(RGB, Gray);
    vec3 Chroma = RGB - Luma;
    RGB = (Chroma * Saturation) + Luma;

    // Contrast
    RGB = (RGB - 0.5) * Contrast + 0.5;

    fragColor = vec4(RGB, 1.0);

}
