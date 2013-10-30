# typescript

* files in app/assets/javascript/*.ts
* <script src='@routes.Assets.at("javascript/app.js")'></script>

# auth

* add auth from https://bitbucket.org/log4900/secret/raw/db63b881a3bed303ae7d32d57163ea9c382f2846/securesocial.conf (private repo)
* to logout: https://accounts.google.com/b/0/IssuedAuthSubTokens

curl 'http://localhost:9000/snippets' \
-X POST \
-H 'Content-Type: application/json; charset=UTF-8' \
-H 'Accept: application/json' \
-H 'Cookie: id=e49ec11218a55011582d3234d396532464ee8339e686afbd5b8ca03cae135c91c5bd42582cc07cc9fa9b60d807b261027ad21a7834697ead9c789f35c39bdb01ac4114394a90d5dcc76ead0946f1522307cec6b98b7e6799b6e01fc63cc149bdb6ffa303d211e4872967567d09ff29c8909609259f1f97ec497a28907c4d20ba' \
--data-binary '"1+1"'