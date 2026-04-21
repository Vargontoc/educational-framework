#!/usr/bin/env bash
# Simple example backup script using pg_dump
set -euo pipefail

PGHOST=${PGHOST:-localhost}
PGPORT=${PGPORT:-5432}
PGUSER=${PGUSER:-edu}
PGDATABASE=${PGDATABASE:-edu_db}
BACKUP_DIR=${BACKUP_DIR:-./backups}
KEEP_DAYS=${KEEP_DAYS:-30}

mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date -u +"%Y%m%dT%H%M%SZ")
FNAME="$BACKUP_DIR/${PGDATABASE}_$TIMESTAMP.sql.gz"

echo "Backing up $PGDATABASE to $FNAME"
PGPASSWORD=${PGPASSWORD:-} pg_dump -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" "$PGDATABASE" | gzip > "$FNAME"

echo "Pruning backups older than $KEEP_DAYS days"
find "$BACKUP_DIR" -type f -name "${PGDATABASE}_*.sql.gz" -mtime +$KEEP_DAYS -delete

echo "Backup complete"
