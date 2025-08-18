param(
  [string]$SeriesPath = 'c:/dev/Librarie/backend/data/series.csv',
  [string]$BooksPath  = 'c:/dev/Librarie/backend/data/books.csv'
)

$ErrorActionPreference = 'Stop'

if (!(Test-Path $SeriesPath)) { throw "Series CSV not found: $SeriesPath" }
if (!(Test-Path $BooksPath))  { throw "Books CSV not found: $BooksPath" }

$series = Import-Csv -Delimiter ';' -Path $SeriesPath
$books  = Import-Csv -Delimiter ';' -Path $BooksPath

$bookSeriesUuids = ($books | Where-Object { $_.series_uuid -and $_.series_uuid.Trim() -ne '' } | Select-Object -ExpandProperty series_uuid) | Sort-Object -Unique
$missing = $series | Where-Object { $_.series_uuid -notin $bookSeriesUuids }

Write-Output ("Total series: {0}" -f $series.Count)
Write-Output ("Total books: {0}" -f $books.Count)
Write-Output ("Series with no books: {0}" -f $missing.Count)

if ($missing.Count -gt 0) {
  $missing | Select-Object series_uuid,series_name | Format-Table -AutoSize | Out-String | Write-Output | Out-Host
  exit 2
} else {
  Write-Output 'All series have >= 1 book.'
}
