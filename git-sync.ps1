<# Version 1.2.2 #>
$originalLocation = Get-Location
Set-Location ..\..\islaterm.github.io

git.exe fetch
git.exe add .
git.exe commit -m "Updated files"
git.exe pull
git.exe push

Set-Location $originalLocation
exit