# Persistence

## Tables
ak_imported_logs
ak_imported_meals

## Strategy
DELETE WHERE date
INSERT new snapshot

## Why
Ensures consistency and avoids stale data
