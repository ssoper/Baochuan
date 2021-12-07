# Baochuan 寶船

Collection of indicators. Runs as a web service.

## Architecture

```text
             Market Data Provider
                   Polygon
                     ⬇️ 
Broker ↔️ Batil ↔️ Baochuan ➡️ Profit 🤑
```

## TODO

* Consider using [Jackson](https://ktor.io/docs/jackson.html#register_jackson_converter)
* [Limit CORS](https://ktor.io/docs/cors.html#methods) to localhost and/or AWS host