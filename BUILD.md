You need:
* jdk-21 (e.g. `sudo apt install openjdk-21-jdk`)
* maven (e.g. `sudo apt install maven`)
* Rust toolchain (cargo)
* CMake + a C++23 compiler, OpenCV (core, imgcodecs), NLopt, DIPlib (built from source, see native/cpp/lib/CMakeLists.txt)

Note tests are currently not fully working.

Build the native binaries and drop them into the plugin's resources, then build the jar:

```
cargo build --release --manifest-path native/rust/Cargo.toml

cmake -S native/cpp/tools -B native/cpp/tools/build -DCMAKE_BUILD_TYPE=Release
cmake --build native/cpp/tools/build --config Release

# copy the built exes (assessment, frc_this, f2i, split from native/rust/target/release,
# hawkman, squirrel from native/cpp/tools/build) into
# imagej/src/main/resources/{windows,nix,mac}/bin for your platform

cd imagej
mvn -Denforcer.skip -Dmaven.test.skip=true install
```

CI builds all four platforms and assembles them into one jar - see .github/workflows.
