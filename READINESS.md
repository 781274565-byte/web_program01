# Project Readiness

Verified in this worktree on 2026-04-20 with `JAVA_HOME` overridden to `C:\Program Files\Java\jdk-23`.

## Java Prerequisite

- The machine-local `JAVA_HOME` is currently `E:\java`, which is not a valid JDK for this project.
- Before running `.\mvnw.cmd test` or `.\mvnw.cmd spring-boot:run`, set `JAVA_HOME=C:\Program Files\Java\jdk-23`.

## What Passed

- With `JAVA_HOME` pointed at the installed JDK, `.\mvnw.cmd test` completed successfully with `32` tests passing.
- The test runtime uses in-memory H2, so the automated suite does not require a local MySQL server.

## Runtime Requirement

- MySQL client/server binaries were not available on the PATH in this environment.
- TCP connection to `localhost:3306` failed, so a local MySQL instance is not currently running here.
- Local application startup is configured for MySQL at `jdbc:mysql://localhost:3306/campus_fast_transfer`.

## Manual Smoke Test Paths

- Register a normal user: `/register`
- Login: `/login`
- Upload a file: `/files`
- Enable share, look up share code, and download shared file: `/share`
- Admin login and dashboard: `/login`, then `/admin` after signing in as an admin user

## Notes For Final Delivery

- If you want the app to run locally outside tests, first set `JAVA_HOME` to the installed JDK, then start MySQL and ensure the `campus_fast_transfer` database exists.
- The current runtime credentials in `src/main/resources/application.yml` are `root` / `123456`.
