param(
  [string]$BooksPath = 'c:/dev/Librarie/backend/data/books.csv'
)
$ErrorActionPreference = 'Stop'
if (!(Test-Path $BooksPath)) { throw "books.csv not found: $BooksPath" }

$rows = Import-Csv -Delimiter ';' -Path $BooksPath
if (-not $rows -or $rows.Count -eq 0) { Write-Output 'books.csv is empty'; exit 0 }

# 1) Duplicate book_uuid
$dupBookIds = $rows | Group-Object -Property book_uuid | Where-Object { $_.Count -gt 1 }
# 2) Duplicate (series_uuid, series_index)
$dupSeriesIndex = $rows |
  Where-Object { $_.series_uuid -and $_.series_uuid.Trim() -ne '' } |
  ForEach-Object {
    [PSCustomObject]@{
      series_uuid = $_.series_uuid
      series_index = $_.series_index
      title = $_.title
      book_uuid = $_.book_uuid
    }
  } |
  Group-Object -Property series_uuid,series_index |
  Where-Object { $_.Count -gt 1 }
# 3) Duplicate titles within the same series (case-insensitive)
$dupTitleInSeries = $rows | ForEach-Object {
  $titleVal = if ($null -ne $_.title) { [string]$_.title } else { '' }
  [PSCustomObject]@{
    key = ($_.series_uuid + '::' + $titleVal).ToLower()
    series_uuid = $_.series_uuid
    title = $titleVal
    book_uuid = $_.book_uuid
    series_index = $_.series_index
  }
} | Group-Object -Property key | Where-Object { $_.Count -gt 1 }
# 4) Duplicate ISBN (non-empty) with normalization (remove dashes/spaces)
$dupIsbn = $rows |
  Where-Object { $_.isbn -and $_.isbn.Trim() -ne '' } |
  ForEach-Object {
    $norm = ([string]$_.isbn).ToUpper().Replace('-', '').Replace(' ', '')
    [PSCustomObject]@{
      isbn = $norm
      book_uuid = $_.book_uuid
      title = $_.title
      series_index = $_.series_index
    }
  } |
  Group-Object -Property isbn |
  Where-Object { $_.Count -gt 1 }

Write-Output "books.csv rows: $($rows.Count)"
Write-Output ("Duplicate book_uuid groups: {0}" -f ($dupBookIds | Measure-Object).Count)
if ($dupBookIds) {
  $dupBookIds | ForEach-Object {
    Write-Output ("- book_uuid {0} appears {1} times" -f $_.Name, $_.Count)
  }
}
Write-Output ("Duplicate (series_uuid, series_index) groups: {0}" -f ($dupSeriesIndex | Measure-Object).Count)
if ($dupSeriesIndex) {
  $dupSeriesIndex | ForEach-Object {
    $pair = $_.Group[0]
    Write-Output ("- series_uuid={0} index={1} -> {2} entries" -f $pair.series_uuid, $pair.series_index, $_.Count)
    $_.Group | Select-Object book_uuid,title,series_index | Format-Table -AutoSize | Out-String | Write-Output | Out-Host
  }
}
Write-Output ("Duplicate titles within same series: {0}" -f ($dupTitleInSeries | Measure-Object).Count)
if ($dupTitleInSeries) {
  $dupTitleInSeries | ForEach-Object {
    $first = $_.Group[0]
    Write-Output ("- series_uuid={0}, title='{1}' -> {2} entries" -f $first.series_uuid, $first.title, $_.Count)
    $_.Group | Select-Object book_uuid,series_index,title | Sort-Object series_index | Format-Table -AutoSize | Out-String | Write-Output | Out-Host
  }
}
Write-Output ("Duplicate non-empty ISBNs: {0}" -f ($dupIsbn | Measure-Object).Count)
if ($dupIsbn) {
  $dupIsbn | ForEach-Object {
    Write-Output ("- ISBN {0} -> {1} entries" -f $_.Name, $_.Count)
    $_.Group | Select-Object book_uuid,title,series_index | Format-Table -AutoSize | Out-String | Write-Output | Out-Host
  }
}

# Exit code 2 if any duplicates detected
$dupCount = (($dupBookIds | Measure-Object).Count) + (($dupSeriesIndex | Measure-Object).Count) + (($dupTitleInSeries | Measure-Object).Count) + (($dupIsbn | Measure-Object).Count)
if ($dupCount -gt 0) { exit 2 } else { Write-Output 'No duplicate keys detected.' }
