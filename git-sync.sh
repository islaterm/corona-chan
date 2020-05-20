# Version 1.0.5
originalLocation=$(pwd)
cd ../../corona-chan || exit

npx prettier --vue-indent-script-and-style ./**/*.vue
npx prettier ./**/*.html

git fetch heroku master
git add .
git commit -m "Updated files"
git pull heroku master
git push heroku master

cd "$originalLocation" || exit
