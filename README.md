# Stale-Bot

Tags and removes branches older than `x` days.

### How to use

```sh
./gradlew purge
```

### Options

Either via `gradle.properties`

```groovy
# a branch is stale after x days
days=78
# updates remote, pushing tags and deleting stale branches, default true
updateRemote=true
```

 or via gradle command line arguments

```sh
./gradlew purge -Ddays=78 -DupdateRemote=false
```
