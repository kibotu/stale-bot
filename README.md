# Stale-Bot

Tags and removes branches older than `x` days.

### How to use

```sh
./gradlew purge -Ddays=56 -DupdateRemote=false
```

### Options

Either via `gradle.properties` or via gradle command line arguments

```groovy
# default 12 weeks
days=78
# updates remote, pushing tags and deleting stale branches
updateRemote=true
```