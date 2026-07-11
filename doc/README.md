# KFlite Documentation Module

This module contains the documentation for KFlite, built using Compose Multiplatform for Wasm (WebAssembly).

## How to run locally

Run the following command to start a development server:

```bash
./gradlew :doc:wasmJsBrowserRun
```

## How to deploy to GitHub Pages

1. Generate the production distribution:
   ```bash
   ./gradlew :doc:wasmJsBrowserDistribution
   ```
2. The output will be located in:
   `doc/build/dist/wasmJs/productionExecutable/`
3. Copy these files to your GitHub Pages branch (e.g., `gh-pages`) or to a `docs/` folder in your main branch, depending on your GitHub settings.
4. Ensure you include a `.nojekyll` file in the root of your GitHub Pages site to prevent GitHub from ignoring files starting with an underscore (which are common in Wasm builds).

## Content Structure

The documentation is organized into sections:
- **Intro**: General overview and installation.
- **Runtimes**: Details on TFLite and LiteRT support.
- **Postprocessing**: Guides on Reshaping and Normalization.
- **Preprocessing**: Image preparation guides.

Each section follows the format:
1. **What is this?**
2. **When you need this?**
3. **How to use it?**
