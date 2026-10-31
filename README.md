# Warehouse

This project uses [Gradle](https://gradle.org/).

This project is a single-module build. The build logic lives in `build.gradle.kts` and the entry point is
`src/main/kotlin/org/warehouse/App.kt`.

```
./gradlew run
echo "sensor_id=t1; value=36" | nc -u -w1 localhost 3344
```

```
WARN ConsoleAlarmNotifier - ALARM [warehouse=wh-1] [sensor=t1] TEMPERATURE 36.0 °C exceeds threshold 35.0 °C at 2026-10-30T09:08:09Z
```

---