# Weasis Rust Full-Parity Rebuild (Web UI, FE+BE Rust) Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Rebuild Weasis 4.7.3-1 (ELF arm64 + Java/OSGi) as full-parity Rust workspace: Rust backend + Rust→WASM web UI, covering all DICOM features (local read/view, DIMSE C-FIND/C-MOVE/C-GET/C-STORE, DICOMweb QIDO/WADO/STOW), launcher/config/prefs, auth, packaging.

**Architecture:** Cargo workspace `weasis-rs/`: `crates/` backend (launcher, config, dicom-core, dimse, dicomweb, auth, storage, api-server), `frontend/` Leptos WASM SPA served by Axum, shared `weasis-types` serde structs. Axum REST + WebSocket for progress/events; Leptos canvas viewer (2D first, 3D via wgpu later). No SQLite (parity: original uses file prefs + Felix cache only).

**Tech Stack:** Rust stable, Axum + Tokio, Tower-HTTP, reqwest, serde/serde_json, dicom-rs (`dicom-core`, `dicom-object`, `dicom-dictionary-std`, `dicom-encoding`, `dicom-ul`, `dicom-json`, `dicom-pixeldata`), Leptos 0.6 + `wasm-bindgen`/`web-sys`, tracing, clap, toml, semver, cargo-deb.

---

## Phase 0 — Toolchain + workspace skeleton

### Task 1: Install Rust toolchain + wasm target
**Objective:** `cargo` available with wasm32 target.
**Files:** none (env only).
**Step 1:** Run: `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y && rustup target add wasm32-unknown-unknown && cargo install cargo-leptos cargo-deb`
**Step 2:** Run: `rustc --version && cargo --version && rustup target list --installed`
Expected: rustc + cargo present, `wasm32-unknown-unknown` listed.
**Step 3:** Commit: n/a (env).

### Task 2: Create Cargo workspace layout
**Objective:** Workspace compiles empty.
**Files:**
- Create: `weasis-rs/Cargo.toml`
- Create: `weasis-rs/crates/weasis-types/src/lib.rs`
- Create: `weasis-rs/crates/weasis-api/src/main.rs`
- Test: `weasis-rs/crates/weasis-types/tests/smoke.rs`
**Step 1: Write failing test**
```rust
// crates/weasis-types/tests/smoke.rs
#[test]
fn version_constant_exists() {
    assert_eq!(weasis_types::APP_VERSION, "4.7.3");
}
```
**Step 2: Run test to verify failure**
Run: `cargo test -p weasis-types`
Expected: FAIL — `APP_VERSION` not defined.
**Step 3: Write minimal implementation**
```toml
# weasis-rs/Cargo.toml
[workspace]
resolver = "2"
members = ["crates/*"]
```
```rust
// crates/weasis-types/src/lib.rs
pub const APP_VERSION: &str = "4.7.3";
```
```rust
// crates/weasis-api/src/main.rs
fn main() { println!("weasis-api {}", weasis_types::APP_VERSION); }
```
**Step 4: Run test to verify pass**
Run: `cargo test -p weasis-types && cargo run -p weasis-api`
Expected: PASS + prints `weasis-api 4.7.3`.
**Step 5: Commit**
```bash
git add weasis-rs && git commit -m "feat: rust workspace skeleton"
```

### Task 3: Health endpoint (Axum skeleton)
**Objective:** `GET /api/health` returns version JSON.
**Files:**
- Create: `weasis-rs/crates/weasis-api/src/health.rs`
- Modify: `weasis-rs/crates/weasis-api/src/main.rs`
- Test: `weasis-rs/crates/weasis-api/tests/health.rs`
**Step 1: Write failing test**
```rust
// tests/health.rs — use axum-test or reqwest against spawned server
#[tokio::test]
async fn health_returns_version() {
    // spawn app on 127.0.0.1:0, GET /api/health, assert {"version":"4.7.3"}
    todo!()
}
```
**Step 2:** Run: `cargo test -p weasis-api` Expected: FAIL.
**Step 3: Minimal implementation**
```rust
// health.rs
use axum::{routing::get, Json, Router};
use serde_json::{json, Value};
pub fn router() -> Router {
    Router::new().route("/api/health", get(|| async { Json(json!({"version": weasis_types::APP_VERSION})) }))
}
```
Wire into `main.rs` with `tokio::net::TcpListener`.
**Step 4:** Run: `cargo test -p weasis-api` Expected: PASS.
**Step 5: Commit** `feat: axum health endpoint`.

## Phase 1 — Config + prefs (parity: ConfigData, RemotePrefService)

### Task 4: `weasis-config` — weasis:// URI + wcfg/cdb/auth/pro/arg params
**Objective:** Parse `weasis://...$wcfg=..$cdb=..$auth=..$pro=k+v$arg=..` per SPEC §4.2/5.1.
**Files:** Create `crates/weasis-config/src/lib.rs`, Test `crates/weasis-config/tests/uri.rs`.
Test: input `weasis://open$cdb=https://x$arg=weasis:ui` → `{codebase_url: "https://x", args: ["weasis:ui"]}`.
Impl: split `$`, url-decode, match prefix. Minimal only (no FS yet).
Verify: `cargo test -p weasis-config`. Commit.

### Task 5: Local codebase + prefs paths
**Objective:** Resolve `{home}/.weasis/preferences/{user}/{profile}/` + `weasis.properties` read/write.
**Files:** Modify `crates/weasis-config/src/lib.rs`, Test `tests/prefs_path.rs`.
Test: `prefs_dir("alice","default")` ends with `.weasis/preferences/alice/default`.
Impl: `dirs::home_dir()` + join; `weasis.properties` via simple key=value parse (no new dep: hand-roll 20 lines).
Verify: `cargo test -p weasis-config`. Commit.

### Task 6: Remote prefs fetch
**Objective:** `GET {pref_url}?user=&profile=` Accept `text/x-java-properties`, timeouts 7s/10s.
**Files:** Create `crates/weasis-config/src/remote.rs`, Test with `wiremock` or local axum mock.
Verify: `cargo test -p weasis-config`. Commit.

## Phase 2 — DICOM core (parity: TagW/TagD, hierarchy, manifest)

### Task 7: Tag registry + VR→type mapping
**Objective:** `tag_type(vr, tag_id)` matches SPEC §3.1 truth table.
**Files:** Create `crates/dicom-core/src/tag.rs`, Test `tests/tag_type.rs` (DS→DOUBLE, PN→PERSON_NAME, 0x100040→SEX, SQ→SEQUENCE, null→STRING, SL/SS/UL/US→INTEGER...).
Impl: enum `TagType{...}` + `from_vr(vr: Option<&str>, tag: u32)`.
Verify: `cargo test -p weasis-dicom-core`. Commit. Deps: none (pure).

### Task 8: VM + multi-value split
**Objective:** `parse_vm("1-n")==(1,u32::MAX)`, split `\` multi-values, DS empty→NaN, IS empty→0.
**Files:** Modify `crates/dicom-core/src/tag.rs`, Test `tests/vm.rs`.
Verify: `cargo test -p weasis-dicom-core`. Commit.

### Task 9: dataelements.xml loader
**Objective:** Load `dataelements.xml` (copy from `dicom-dictionary-std` or ship `assets/dataelements.xml`), XXE-safe, UNKNOWN defaults for PatientID/Name/StudyUID/SeriesUID/Modality.
**Files:** Create `crates/dicom-core/src/dict.rs`, Test: `lookup(0x00100020).keyword=="PatientID"`.
Deps: add `dicom-dictionary-std`, `quick-xml` (or reuse dicom crate dict — prefer reuse: `dicom_dictionary_std::data_element_by_tag`; document choice).
Verify: `cargo test -p weasis-dicom-core`. Commit.

### Task 10: Patient→Study→Series→Instance hierarchy
**Objective:** In-memory tree insert/get/remove matching SPEC §3.2.
**Files:** Create `crates/dicom-core/src/model.rs`, Test `tests/hierarchy.rs` (insert patient/study/series/instance, remove series, lookup).
Impl: `HashMap`-nested structs with serde derives (shared with frontend via `weasis-types`).
Verify: `cargo test -p weasis-dicom-core`. Commit.

### Task 11: DICOM file read (local)
**Objective:** Open `.dcm` → Attributes + pixel stub (rows/cols/bits) via `dicom-object`.
**Files:** Create `crates/dicom-core/src/reader.rs`, Test with repo fixture (generate 1 minimal implicit-VR file in `testdata/`, or use `dicom-test-files`? keep 1 generated fixture checked in).
Deps: `dicom-object`, `dicom-core`.
Verify: `cargo test -p weasis-dicom-core`. Commit.

## Phase 3 — Storage (parity: no SQLite, file prefs + cache + session temp)

### Task 12: Cache dirs + session temp
**Objective:** Create `{home}/.weasis/cache-{sourceID}/`, `{home}/.weasis/log/`, temp session dir; boot log rotate stub.
**Files:** Create `crates/weasis-storage/src/lib.rs`, Test `tests/dirs.rs` (uses `tempfile::tempdir` + env override `WEASIS_HOME`).
Verify: `cargo test -p weasis-storage`. Commit.

### Task 13: Bundle/plugin registry (OSGi-lite)
**Objective:** Static registry of 39 bundles with start levels (SPEC §2.3 table) + enable/disable per profile (default/dicomizer/base-explorer).
**Files:** Create `crates/weasis-storage/src/registry.rs`, Test: dicomizer profile excludes viewer2d/qr.
Impl: `const BUNDLES: &[(&str,&str,u8)]` + profile filter fn. No dynamic loading yet (YAGNI).
Verify: `cargo test -p weasis-storage`. Commit.

## Phase 4 — DIMSE (parity: C-FIND/C-MOVE/C-GET/C-STORE, dcm4che params)

### Task 14: `dicom-ul` association + C-ECHO smoke
**Objective:** Associate to test SCP + C-ECHO succeeds (against `dicom-ul` test helper or local `storescp` stub in tests).
**Files:** Create `crates/weasis-dimse/src/lib.rs`, Test `tests/echo.rs` (feature-gated `#[ignore]` if no SCP; default uses in-process stub).
Deps: `dicom-ul`, `dicom-core`.
Verify: `cargo test -p weasis-dimse`. Commit.

### Task 15: C-FIND SERIES query builder
**Objective:** Build SERIES keys (StudyUID, SeriesUID, Modality, SeriesNumber, SeriesDescription) + parse responses into hierarchy.
**Files:** Create `crates/weasis-dimse/src/find.rs`, Test with mocked dataset vec.
Verify: `cargo test -p weasis-dimse`. Commit.

### Task 16: C-MOVE/C-GET retrieve + StoreSCP dir
**Objective:** Retrieve to `storageDir` (C-MOVE needs listener running check; else error `msg_start_listener`).
**Files:** Create `crates/weasis-dimse/src/retrieve.rs`, Test state machine (listener missing → error string).
Verify: `cargo test -p weasis-dimse`. Commit.

### Task 17: C-STORE send (Dicomizer path, AET DCM4CHEE:11112 default)
**Objective:** Send file list to `host:port:AET` with progress callback.
Verify: `cargo test -p weasis-dimse`. Commit.

## Phase 5 — DICOMweb (parity: QIDO/WADO/STOW)

### Task 18: QIDO-RS URL builder + JSON parse
**Objective:** `{base}/studies?{TAGHEX=enc&...}{study_query}{query.ext}{page.size}`, PatientID `^^^`→`&00100021=` split; parse JSON array into Attributes.
**Files:** Create `crates/weasis-dicomweb/src/qido.rs`, Test `tests/qido_url.rs` (exact string match).
Deps: `reqwest`, `dicom-json`, `urlencoding`.
Verify: `cargo test -p weasis-dicomweb`. Commit.

### Task 19: WADO-RS/URI fetch
**Objective:** `WadoParameters` struct + GET multipart/related + thumbnail `image/jpeg` path.
**Files:** Create `crates/weasis-dicomweb/src/wado.rs`, Test with wiremock (multipart body assert).
Verify: `cargo test -p weasis-dicomweb`. Commit.

### Task 20: STOW-RS POST
**Objective:** `POST {url}/studies` multipart/related boundary `weasisDicomBoundary`, `User-Agent: Weasis STOW-RS Client`; handle 200/202/409/401(reset token).
**Files:** Create `crates/weasis-dicomweb/src/stow.rs`, Test with wiremock (header + boundary asserts, 202→partial parse).
Verify: `cargo test -p weasis-dicomweb`. Commit.

## Phase 6 — Auth (parity: OAuth2 loopback)

### Task 21: Auth types + Keycloak URL builder
**Objective:** `AuthProvider/AuthRegistration`, `build_keycloak_provider` → `{base}/realms/{realm}/protocol/openid-connect/{auth,token,revoke}`; Google template constants.
**Files:** Create `crates/weasis-auth/src/lib.rs`, Test `tests/provider.rs`.
Verify: `cargo test -p weasis-auth`. Commit.

### Task 22: Loopback callback + token exchange (mocked)
**Objective:** Bind `127.0.0.1:{port|0}`, capture `code`, exchange via mock token endpoint.
**Files:** Create `crates/weasis-auth/src/flow.rs`, Test with local mock server.
Deps: `axum` (callback), `reqwest`, `oauth2` crate? Prefer hand-rolled minimal (2 endpoints) to avoid heavy dep — decide in task, document.
Verify: `cargo test -p weasis-auth`. Commit.

## Phase 7 — API server wiring

### Task 23: REST surface (studies/series/query/retrieve/store/jobs)
**Objective:** `GET /api/studies`, `POST /api/query/qido`, `POST /api/retrieve`, `POST /api/store`, `GET /api/jobs/:id` (SSE/WebSocket progress).
**Files:** Create `crates/weasis-api/src/routes/*.rs`, Tests per route (mock backends).
Verify: `cargo test -p weasis-api`. Commit.

## Phase 8 — Frontend (Leptos WASM, full UI)

### Task 24: Leptos shell + router
**Objective:** `frontend/` builds to WASM, served by Axum; routes: Explorer / Viewer / QR / Send / Prefs.
**Files:** Create `frontend/Cargo.toml`, `frontend/src/{main.rs,app.rs,pages/*}`.
Run: `cargo leptos build` (or `trunk build` — pick one in task, default cargo-leptos).
Verify: `cargo leptos build` + `curl localhost:3000 | grep Weasis`. Commit.

### Task 25: Explorer (patient→study→series tree)
**Objective:** Tree from `GET /api/studies`, backed by shared `weasis-types` structs.
Verify: manual + `cargo leptos build`. Commit.

### Task 26: 2D viewer (canvas, WW/WL, pan/zoom)
**Objective:** Render instance via `<canvas>`, window/level slider, pan/zoom/measure stub.
Verify: open testdata study in browser, screenshot. Commit.

### Task 27: QR/Send/Prefs pages
**Objective:** QIDO form → results; STOW upload form; prefs editor (user/profile/nodes/endpoints).
Verify: e2e against mock server. Commit.

### Task 28 (deferred): 3D/MPR, SR/RT/AU/ECG overlays, ISO writer
Mark `ponytail:` — wgpu volume, PR serializer, ISO burn; add when 2D stable.

## Phase 9 — Packaging (parity: .deb, desktop, mime)

### Task 29: `cargo-deb` + desktop/mime
**Objective:** `.deb` installs `/opt/weasis-rs/bin/weasis-rs`, desktop `weasis-rs.desktop` (MimeType `application/dicom;x-scheme-handler/weasis;`), postinst xdg-desktop-menu/xdg-mime.
**Files:** Create `weasis-rs/debian/*`, `assets/weasis-rs.desktop`, `assets/mime.xml`.
Verify: `cargo deb && dpkg-deb -c target/debian/*.deb`. Commit.

## Phase 10 — Parity gate

### Task 30: Parity checklist + clippy + fmt
Run: `cargo test --workspace && cargo clippy -- -D warnings && cargo fmt --check`
Expected: all green. Update `SPECIFICATION.md` §6 checkboxes. Commit `chore: parity gate`.

---

## Risks / tradeoffs
- `dicom-rs` DIMSE upper-layer vs dcm4che quirks: isolate in `weasis-dimse`, mock-SCP tests.
- WASM image decode perf: decode server-side, ship 8-bit render + lut; raw pixels later (`ponytail:`).
- 3D (jogamp→wgpu) deferred; 2D parity first.
- OAuth loopback port random: keep `weasis.auth.back.port` pref.
- Scope guard: each task ≤5 min; if a task balloons, split before implementing.
