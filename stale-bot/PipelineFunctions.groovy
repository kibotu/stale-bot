import java.text.SimpleDateFormat

def purgeBranchesOlderThan(days, updateRemote) {

    // everything higher than ~ 1643384300 will cause overflow
    if (days == null || days <= 0 || days > 1600000000) {
        error("Invalid days `$days`")
    }

    if (updateRemote == null) {
        error("Invalid updateRemote `$updateRemote`")
    }

    println("Trying to purge branches that are older than `$days`. Updating remote: `${updateRemote == true}`")

    // fetch on CI
    sh(script: "git config core.sparsecheckout false", returnStdout: true)

    // checkout master
    sh(script: "git checkout master", returnStdout: true)
    sh(script: "git pull", returnStdout: true)

    // get all branches
    def branches = sh(script: "git branch -r", returnStdout: true).trim().split("\n")

    println("\nStale Branches (older than $days days)\n")

    // run through all branches
    for (branch in branches) {

        println(branch)

        // remove white spaces at the edges
        branch = branch.trim()

        // ignore master and develop
        if (branch.contains("master")) continue
        if (branch.contains("develop")) continue

        // figure out if there are any commits ahead master since x days
        def logs = sh(script: "git log master..$branch --pretty=oneline --since=${days}.days.ago --stat", returnStdout: true)

        // tag and remove branches where there were no commits since x days
        if (logs.isEmpty()) {

            printStatsFor(branch)
            try {
                tag(branch, updateRemote)
                close(branch, updateRemote)
            } catch (Exception e) {
                unstable(e.localizedMessage)
            }
        }
    }
}

def tag(branch, updateRemote) {
    branch = branch.replace("origin/", "")

    // tag
    sh(script: "git tag stale/$branch", returnStdout: true)

    if (!updateRemote) return

    // push to origin
    sh(script: "git push origin stale/$branch", returnStdout: true)
}

def close(branch, updateRemote) {
    branch = branch.replace("origin/", "")

    if (!updateRemote) return

    sh(script: "git push origin --delete $branch", returnStdout: true)
}

def printStatsFor(branch) {

    // --format=%ai : 2022-01-30 14:45:33 +0100
    // def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
    // the following doesn't work due the way sh in jenkins pipeline works due to `--format=%ai` causing expected named arguments
    // def lastCommit = sh(script: "git log master..$branch -1 --format=%ai", returnStdout: true).trim()

    // Sun Jan 30 14:45:33 2022 +0100
    def dateFormatter = new SimpleDateFormat("E MMM d HH:mm:ss yyyy z")
    def lastCommit = sh(script: "git log master..$branch -1 | grep \"Date:\"", returnStdout: true).trim().replace("Date:", "").trim()

    println("last commit `$lastCommit`")

    if (lastCommit == "") return

    def commitDate = dateFormatter.parse(lastCommit)
    def duration = groovy.time.TimeCategory.minus(new Date(), commitDate)

    def behind = sh(script: "git rev-list $branch..master", returnStdout: true).trim().split('\n').size()
    def ahead = sh(script: "git rev-list master..$branch", returnStdout: true).trim().split('\n').size()

    println("Last commit before $duration | Ahead: $ahead | Behind: $behind | `$branch`")
}

return this