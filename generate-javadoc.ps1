[CmdletBinding()]
param()

$sourceRoot = Join-Path $PSScriptRoot 'src'
$outputDirectory = Join-Path $PSScriptRoot 'doc'
$overviewFile = Join-Path $sourceRoot 'overview.html'

$javadocCommand = Get-Command javadoc -ErrorAction SilentlyContinue
if ($javadocCommand) {
    $javadocExecutable = $javadocCommand.Source
} elseif ($env:JAVA_HOME) {
    $javadocExecutable = Join-Path $env:JAVA_HOME 'bin\javadoc.exe'
} else {
    throw 'Javadoc was not found. Install a JDK and configure JAVA_HOME.'
}

if (-not (Test-Path -LiteralPath $javadocExecutable -PathType Leaf)) {
    throw "Javadoc was not found at: $javadocExecutable"
}

$sourceFiles = @(
    Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java' -File |
        Sort-Object FullName |
        Select-Object -ExpandProperty FullName
)

if ($sourceFiles.Count -eq 0) {
    throw "No Java source files were found under: $sourceRoot"
}

$expectedOutput = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'doc'))
$resolvedOutput = [IO.Path]::GetFullPath($outputDirectory)
if ($resolvedOutput -ne $expectedOutput) {
    throw "Refusing to clean unexpected output directory: $resolvedOutput"
}

if (Test-Path -LiteralPath $resolvedOutput) {
    Remove-Item -LiteralPath $resolvedOutput -Recurse -Force
}

$javadocArguments = @(
    '-d', $resolvedOutput,
    '-sourcepath', $sourceRoot,
    '-overview', $overviewFile,
    '-encoding', 'UTF-8',
    '-charset', 'UTF-8',
    '-docencoding', 'UTF-8',
    '-Xdoclint:all',
    '-use',
    '-notimestamp',
    '-windowtitle', 'SpaceshooterHD API Documentation',
    '-doctitle', 'SpaceshooterHD API Documentation'
) + $sourceFiles

& $javadocExecutable @javadocArguments
if ($LASTEXITCODE -ne 0) {
    throw "Javadoc generation failed with exit code $LASTEXITCODE."
}

Write-Host "Generated API documentation for $($sourceFiles.Count) Java files in $resolvedOutput"
