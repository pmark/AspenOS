PUBDIR='/opt/aspenos/apps/AOSSystemConsole/lib/'
WEBDIR='/opt/apache139/htdocs/aos/AOSSystemConsole/lib/'

chmod a+r ../lib/*.*

echo
echo Publishing lib/ to $PUBDIR
cp ../lib/* $PUBDIR
echo Publishing lib/ to $WEBDIR
cp ../lib/* $WEBDIR
