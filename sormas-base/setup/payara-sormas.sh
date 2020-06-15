#!/bin/sh
### BEGIN INIT INFO
# Provides:          payara-sormas
# Required-Start:    postgresql
# Required-Stop:     postgresql
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Payara-Server SORMAS
### END INIT INFO


ASADMIN="/opt/payara5/bin/asadmin"
ASADMIN_PORT=6048
LOGIN_USER="payara"
DOMAIN_DIR="/opt/domains"
DOMAIN_NAME="sormas"

case "$1" in
start)
    su --login "$LOGIN_USER" --command "$ASADMIN --port $ASADMIN_PORT start-domain --domaindir \"$DOMAIN_DIR\" \"$DOMAIN_NAME\""
    ;;
stop)
    su --login "$LOGIN_USER" --command "$ASADMIN --port $ASADMIN_PORT stop-domain --domaindir \"$DOMAIN_DIR\" \"$DOMAIN_NAME\""
    ;;
restart)
    su --login "$LOGIN_USER" --command "$ASADMIN --port $ASADMIN_PORT restart-domain --domaindir \"$DOMAIN_DIR\" \"$DOMAIN_NAME\""
    ;;
*)
    echo "usage: $0 (start|stop|restart|help)"
esac
