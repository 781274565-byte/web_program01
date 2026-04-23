$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$preferredJavaHome = "C:\Program Files\Java\jdk-23"
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    if (-not (Test-Path (Join-Path $preferredJavaHome "bin\java.exe"))) {
        throw "Cannot find a usable JDK. Please install JDK 23 or update run-app.ps1."
    }
    $env:JAVA_HOME = $preferredJavaHome
}

$javaBin = Join-Path $env:JAVA_HOME "bin"
$env:Path = "$javaBin;$mysqlBin;$env:Path"

Set-Location $projectRoot
& ".\mvnw.cmd" spring-boot:run
