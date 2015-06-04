JARNAME='systemconsole.jar'
BASECLASSDIR='/opt/aspenos/WEB-INF/classes'
APPCLASSDIR='org/aspenos/app/aossystemconsole'
BASEDIR='..'

jar cvf $BASEDIR/lib/$JARNAME \
-C $BASEDIR bin/dojar.sh \
-C $BASEDIR config/system_console.properties \
-C $BASEDIR config/install.properties \
-C $BASEDIR config/cmd_line_install.properties \
-C $BASEDIR data \
-C $BASEDIR doc \
-C $BASEDIR sql \
-C $BASEDIR templates \
-C $BASEDIR xml \
#-C $BASECLASSDIR $APPCLASSDIR

chmod 664 $BASEDIR/lib/$JARNAME

