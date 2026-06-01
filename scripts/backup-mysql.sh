#!/usr/bin/env bash
# Logical MySQL backup from the running Compose MySQL container.
# Usage: ./scripts/backup-mysql.sh [output-dir]
set -euo pipefail

OUT_DIR="${1:-./backups}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT_DIR"

CONTAINER="${MYSQL_CONTAINER:-hotel-booking-mysql}"
DB_NAME="${DB_NAME:-hotel_booking}"
DB_PASSWORD="${DB_PASSWORD:-root}"

FILE="${OUT_DIR}/hotel_booking_${STAMP}.sql.gz"

echo "Dumping ${DB_NAME} from ${CONTAINER} → ${FILE}"
docker exec "$CONTAINER" \
  mysqldump -uroot -p"${DB_PASSWORD}" --single-transaction --routines --triggers "${DB_NAME}" \
  | gzip -c > "$FILE"

echo "OK ($(du -h "$FILE" | awk '{print $1}'))"
