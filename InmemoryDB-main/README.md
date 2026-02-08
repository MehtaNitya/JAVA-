# In-Memory DB (Single + Multi Thread)

This project implements a command-driven in-memory database with TTL support, lifecycle control, and a concurrency demo.

## OOP Design
- `CommandService` parses raw input into `Command` objects for clean separation of parsing and execution.
- `DbService` implements the database API and encapsulates storage, TTL behavior, lifecycle, and cleanup.
- `Entry<T>` models a stored value with an expiry timestamp (`-1` means no expiry).
- Custom exceptions provide clear error boundaries for invalid input and runtime DB state.

## Thread-Safety Strategy
- Phase 6 uses `synchronized` methods to make `put/get/delete` atomic.
- Phase 10 upgrades storage to `ConcurrentHashMap` to improve concurrency while keeping correctness.

Lock choice:
- The synchronized methods lock on the `DbService` instance (`this`) to serialize mutations and TTL checks.
- Tradeoff: GET can block PUT/DELETE, but correctness is guaranteed.

## Volatile Usage
- `running` is `volatile` to ensure STOP/START visibility across threads.
- Without `volatile`, threads could keep using stale state and ignore STOP.

## Commands
```
PUT <key> <value> [ttl]
GET <key>
DELETE <key>
START
STOP
EXIT
```

## Sample Input
```
PUT 1 hello
PUT 2 100 3000
GET 1
GET 2
DELETE 1
GET 1
STOP
START
EXIT
```

## Multi-Threaded Demo
Run:
```
java -cp target/classes org.example.Main demo
```

The demo starts multiple threads that parse and execute command batches concurrently.

## Notes
- TTL is stored as epoch time to compare with current time easily.
- Lazy expiration happens on `GET`, while a background cleanup thread also removes expired keys.

