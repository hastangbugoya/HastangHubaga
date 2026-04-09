# AK → HH Import System (Overview)

## Purpose
This guide defines the full import pipeline from AdobongKangkong (AK) into HastangHubaga (HH).

## Core Philosophy
- AK is the source of truth
- HH stores snapshot copies
- Imported data is read-only
- Replace-by-day (not merge)

## Flow Summary
HH → AK Provider → JSON → Parse → Map → Persist → Timeline → UI
