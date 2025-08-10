# GraalVM Native Image Metadata

This directory contains automatically generated metadata for GraalVM native image compilation.

## How to generate metadata

Run the following command to generate metadata by running tests with the GraalVM tracing agent:

```bash
./gradlew testWithAgent
```

This will:
1. Run all tests with the GraalVM native agent enabled
2. Collect reflection, resource, and JNI configuration data
3. Generate the necessary JSON files in this directory

## Generated files

- `reflect-config.json` - Reflection configuration
- `resource-config.json` - Resource access configuration  
- `jni-config.json` - JNI configuration
- `proxy-config.json` - Dynamic proxy configuration
- `serialization-config.json` - Serialization configuration