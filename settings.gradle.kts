rootProject.name = "core"
include(":core-common")
include(":core-velocity")
include(":core-paper")
include(":core-survival")
project(":core-common").projectDir = file("Core-common")
project(":core-velocity").projectDir = file("Core-velocity")
project(":core-paper").projectDir = file("Core-paper")
project(":core-survival").projectDir = file("Core-survival")
