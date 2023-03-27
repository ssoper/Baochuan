# Baochuan 寶船

Collection of indicators. Runs as a web service.

## Architecture

```mermaid
---
title: Simple sample
---
stateDiagram-v2
    [*] --> Still
    Still --> [*]

    Still --> Moving
    Moving --> Still
    Moving --> Crash
    Crash --> [*]
```

## TODO

* Consider using [Jackson](https://ktor.io/docs/jackson.html#register_jackson_converter)
* [Limit CORS](https://ktor.io/docs/cors.html#methods) to localhost and/or AWS host