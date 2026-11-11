#version 330 core

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;

out vec4 FragColor;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;

uniform int shadingModel; // 0 Phong, 1 Blinn-Phong
uniform int shadingMode;  // 0 Flat, 1 Smooth

uniform bool useTexture;
uniform sampler2D texture1;

uniform bool useShadow;
uniform sampler2D shadowMap;
uniform mat4 lightSpaceMatrix;

float hitungBayangan(vec4 posisiCahaya, vec3 normal, vec3 lightDir)
{
    vec3 koordinat = posisiCahaya.xyz / posisiCahaya.w;
    koordinat = koordinat * 0.5 + 0.5;

    if (koordinat.z > 1.0 ||
        koordinat.x < 0.0 || koordinat.x > 1.0 ||
        koordinat.y < 0.0 || koordinat.y > 1.0)
        return 0.0;

    float kedalamanTerdekat = texture(shadowMap, koordinat.xy).r;
    float bias = max(0.003 * (1.0 - dot(normal, lightDir)), 0.0007);
    return koordinat.z - bias > kedalamanTerdekat ? 1.0 : 0.0;
}

void main()
{
    vec3 norm = normalize(Normal);

    if (shadingMode == 0) {
        norm = normalize(cross(dFdx(FragPos), dFdy(FragPos)));
    }

    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 viewDir = normalize(viewPos - FragPos);

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 ambient = 0.22 * lightColor;
    vec3 diffuse = diff * lightColor;

    float spec = 0.0;
    if (shadingModel == 0) {
        vec3 reflectDir = reflect(-lightDir, norm);
        spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    } else {
        vec3 halfwayDir = normalize(lightDir + viewDir);
        spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
    }

    vec3 specular = 0.35 * spec * lightColor;

    float shadow = 0.0;
    if (useShadow) {
        shadow = hitungBayangan(
            lightSpaceMatrix * vec4(FragPos, 1.0),
            norm,
            lightDir
        );
    }

    vec3 baseColor = objectColor;
    if (useTexture) {
        baseColor = texture(texture1, TexCoord).rgb;
    }

    vec3 lighting = ambient + (1.0 - shadow) * (diffuse + specular);
    FragColor = vec4(lighting * baseColor, 1.0);
}
