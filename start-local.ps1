$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$preferredJavaHome = "C:\Program Files\Java\jdk-23"
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"
$mysqldPath = Join-Path $mysqlBin "mysqld.exe"
$mysqlConfig = "C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"
$appUrl = "http://127.0.0.1:8080/login"

function Set-UpJava {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        $javaHome = $env:JAVA_HOME
    } elseif (Test-Path (Join-Path $preferredJavaHome "bin\java.exe")) {
        $javaHome = $preferredJavaHome
    } else {
        throw "Cannot find a usable JDK. Please install JDK 23 or update start-local.ps1."
    }

    $env:JAVA_HOME = $javaHome
    $javaBin = Join-Path $javaHome "bin"
    $env:Path = "$javaBin;$mysqlBin;$env:Path"
    Write-Host "JAVA_HOME = $javaHome"
}

function Test-PortOpen([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue)
}

function Wait-ForPort([int]$Port, [int]$TimeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortOpen $Port) {
            return $true
        }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Get-ProcessForPort([int]$Port) {
    $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty OwningProcess
    if (-not $connection) {
        return $null
    }
    return Get-CimInstance Win32_Process -Filter "ProcessId = $connection"
}

function Ensure-MySqlRunning {
    if (Test-PortOpen 3306) {
        Write-Host "MySQL is already listening on port 3306."
        return
    }

    if (-not (Test-Path $mysqldPath)) {
        throw "Cannot find mysqld.exe at $mysqldPath"
    }
    if (-not (Test-Path $mysqlConfig)) {
        throw "Cannot find MySQL config file at $mysqlConfig"
    }

    Write-Host "Starting MySQL..."
    Start-Process -FilePath $mysqldPath -ArgumentList @("--defaults-file=`"$mysqlConfig`"") -WindowStyle Hidden | Out-Null

    if (-not (Wait-ForPort 3306 20)) {
        throw "MySQL did not start listening on port 3306 within 20 seconds."
    }

    Write-Host "MySQL is ready."
}

function Ensure-AppRunning {
    if (Test-PortOpen 8080) {
        $process = Get-ProcessForPort 8080
        if ($process -and $process.CommandLine -like "*com.campusfasttransfer.CampusFastTransferApplication*") {
            Write-Host "Campus Fast Transfer is already running on port 8080."
            Start-Process $appUrl | Out-Null
            return
        }
        throw "Port 8080 is already in use by another process. Please free the port and try again."
    }

    $runner = Join-Path $projectRoot "run-app.ps1"
    Write-Host "Starting Spring Boot in a new PowerShell window..."
    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-File", $runner
    ) -WorkingDirectory $projectRoot | Out-Null

    if (-not (Wait-ForPort 8080 30)) {
        throw "Spring Boot did not start listening on port 8080 within 30 seconds."
    }

    Write-Host "Application is ready: $appUrl"
    Start-Process $appUrl | Out-Null
}

Set-UpJava
Ensure-MySqlRunning
Ensure-AppRunning
