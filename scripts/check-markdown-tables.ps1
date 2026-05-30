param(
    [string[]] $Path = @(".")
)

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]

function Get-MarkdownFiles {
    param([string] $InputPath)

    if (Test-Path -LiteralPath $InputPath -PathType Leaf) {
        $file = Get-Item -LiteralPath $InputPath
        if ($file.Extension -eq ".md") {
            $file
        }
        return
    }

    if (Test-Path -LiteralPath $InputPath -PathType Container) {
        Get-ChildItem -LiteralPath $InputPath -Recurse -File -Filter "*.md" |
            Where-Object {
                $_.FullName -notmatch '[\\/](\.git|\.gradle|build|out)[\\/]'
            }
    }
}

foreach ($root in $Path) {
    foreach ($file in Get-MarkdownFiles $root) {
        $lines = Get-Content -LiteralPath $file.FullName
        $insideFence = $false

        for ($lineNumber = 0; $lineNumber -lt $lines.Count; $lineNumber++) {
            $line = $lines[$lineNumber]
            $trimmed = $line.TrimStart()

            if ($trimmed.StartsWith('```')) {
                $insideFence = -not $insideFence
                continue
            }

            if ($insideFence -or -not $trimmed.StartsWith("|")) {
                continue
            }

            $insideCode = $false
            $insideAngle = $false

            for ($index = 0; $index -lt $line.Length; $index++) {
                $char = $line[$index]

                if ($char -eq '`') {
                    $insideCode = -not $insideCode
                    continue
                }

                if (-not $insideCode) {
                    if ($char -eq '<') {
                        $insideAngle = $true
                    } elseif ($char -eq '>') {
                        $insideAngle = $false
                    }
                }

                if ($char -ne '|') {
                    continue
                }

                $escaped = $index -gt 0 -and $line[$index - 1] -eq '\'
                if (($insideCode -or $insideAngle) -and -not $escaped) {
                    $relative = Resolve-Path -LiteralPath $file.FullName -Relative
                    $failures.Add("${relative}:$($lineNumber + 1): raw pipe inside a markdown table cell; use /, words, or escape as \|")
                }
            }
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Error ("Markdown table check failed:`n" + ($failures -join "`n"))
    exit 1
}

Write-Host "Markdown table check passed."
