# HastangHubaga (HH)

> System-first Android application modeling real-world behavior through a unified, extensible timeline engine.

---

## 🚀 Overview

HastangHubaga (HH) is an offline-first Android application that unifies meals, supplements, and activities into a single timeline-driven system.

Unlike traditional tracker apps, HH is built around a domain-first architecture where scheduling, execution, and history are modeled explicitly.

---

## 🎯 Key Impact

- Designed and implemented a scalable domain-driven architecture for lifestyle tracking
- Built a unified timeline engine merging planned and actual events deterministically
- Replaced rigid schema design with normalized, extensible models
- Enabled multi-plan nutrition system with conflict resolution logic
- Implemented cross-app data ingestion via content provider integration

---

## 🧠 Core Architecture

### Template → Occurrence → Log Model

- Template: defines the entity
- Occurrence: defines scheduled instance
- Log: defines actual execution

Enables:
- accurate historical reconstruction
- flexible scheduling updates
- deterministic state generation

---

### Timeline Engine

Central orchestration via:

BuildTodayTimelineUseCase

- merges planned occurrences and logs
- logs override planned items
- supports ad-hoc events

---

### Nutrition System (Normalized Design)

- Multiple concurrent plans
- Per-nutrient constraints (min / target / max)
- Conflict detection (min > max)
- Source-aware design for external integration

---

## 🏗️ Tech Stack

- Kotlin
- Jetpack Compose
- Coroutines + Flow
- Room (SQLite)
- Hilt (Dagger)
- WorkManager

---

## 🧩 Engineering Strengths

- Clean Architecture (Domain/Data/UI separation)
- Repository + Use Case pattern
- Strong data modeling and normalization
- Reactive data flow with Flow
- Offline-first design

---

## 🔗 Integration

- Cross-app integration with AdobongKangkong (AK)
- JSON-based content provider ingestion
- Designed for forward-compatible syncing

---

## 📌 Status

Active development focused on:
- architecture robustness
- correctness and consistency
- extensibility for future features

---

## ⭐ Summary

HastangHubaga demonstrates strong system design, domain modeling, and scalable Android architecture suitable for complex real-world applications.
