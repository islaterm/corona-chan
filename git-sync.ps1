<# Version 1.2.2 #>
$originalLocation = Get-Location
Set-Location ..\..\corona-chan

npx prettier --vue-indent-script-and-style ./**/*.vue
npx prettier ./**/*.html

git.exe fetch heroku master
git.exe add .
git.exe commit -m "Updated files"
git.exe pull heroku master
git.exe push heroku master

Set-Location $originalLocation
exit