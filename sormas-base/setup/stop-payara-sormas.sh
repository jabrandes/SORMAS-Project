#*******************************************************************************
# SORMAS® - Surveillance Outbreak Response Management & Analysis System
# Copyright © 2016-2018 Helmholtz-Zentrum f�r Infektionsforschung GmbH (HZI)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#*******************************************************************************

#!/bin/bash

DIR=$(dirname "$0")
ASADMIN="${DIR}/../../payara5/bin/asadmin"
ASADMIN_PORT=6048
DOMAIN_DIR="${DIR}/../"
DOMAIN_NAME="sormas"

"${ASADMIN}" --port $ASADMIN_PORT stop-domain --domaindir "$DOMAIN_DIR" "$DOMAIN_NAME"
