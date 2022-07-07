# bottle-examples
Examples for the [Bottle](https://github.com/radgeRayden/bottle/) game framework

## Building

The script `bootstrap.sh` takes care of downloading and building all direct dependencies. It does not however install build-time dependencies, since those will 
differ from OS to OS, and I don't think people like when a script installs system packages.

Bottle (and these examples) currently support Windows (MinGW) and Linux. The build will not successfully complete without:
- gcc
- a rust toolchain

This may not be a complete list, open an issue if it fails on your particular system.

## Running

After all the dependencies are taken care of, simply run each example's `main.sc` file with Scopes in project mode (`-e` flag), like so:

``` sh
scopes -e 01-hello-gpu/main.sc
```

