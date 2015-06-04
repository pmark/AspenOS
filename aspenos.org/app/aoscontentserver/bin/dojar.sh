JARNAME='contentserver.jar'
APPCLASSDIR='org/aspenos/app/aoscontentserver'
BASEDIR='..'

jar cvf $BASEDIR/lib/$JARNAME \
-C $BASEDIR bin/dojar.sh \
-C $BASEDIR bin/pubjar.sh \
-C $BASEDIR config \
-C $BASEDIR data \
-C $BASEDIR doc \
-C $BASEDIR sql \
-C $BASEDIR templates \
-C $BASEDIR xml
#-C /opt/aspenos/WEB-INF/classes $APPCLASSDIR

chmod 664 ../lib/$JARNAME

