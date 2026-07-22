# Remediation Backlog

Status: initial backlog created from confirmed findings.

## Priority Order

### P0: Restore Verifiable Baseline

- Findings: TEST-EXEC-001.
- Priority rationale: no remediation can be considered complete until build/test validation is runnable.
- Scope: run validation in CI or a normal local shell with JDK 21 and normal Gradle cache/network permissions.
- Acceptance criteria: `./scripts/release-check.sh` completes; managed-device tests are either run or explicitly deferred with CI evidence.
- Status: resolved and verified.

### P1: Harden Untrusted Imports

- Findings: SEC-001, SEC-002, TEST-003.
- Priority rationale: import surfaces process user-selected files and can affect personal educational data and local attachments.
- Scope: harden PlusPoints XML parser; add XML size guard; canonicalize backup manifest attachment paths; reject unsafe note/attachment ids for filesystem restore; add negative tests.
- Acceptance criteria: malicious XML with DOCTYPE/entities is rejected; oversized XML is rejected; backup paths using absolute or traversal syntax are rejected; valid backup/PlusPoints imports still pass.
- Status: resolved and verified.

### P2: Fix Target Simulator Synchronization

- Findings: FUNC-001, UI-001, TEST-001.
- Priority rationale: simulator is a high-value recent feature and stale target values can produce misleading required-grade output.
- Scope: update simulator state sync policy and add regression coverage.
- Acceptance criteria: after editing a branch target, expanding simulator uses the saved target unless the user is actively editing the simulator.
- Status: resolved and verified.

### P3: Fix Dashboard Status Tone Mapping

- Findings: FUNC-002, UI-002, TEST-002.
- Priority rationale: academic status severity must be visually unambiguous.
- Scope: replace string-based status tone mapping or patch French `Bloqué`; add regression test.
- Acceptance criteria: French blocked status maps to `DashboardStatusTone.NEGATIVE`; promoted and incomplete still map correctly.
- Status: resolved and verified.

### P4: Privacy Decision For Android Backup

- Findings: SEC-003.
- Priority rationale: grades/photos are sensitive; current behavior may be acceptable but needs an explicit product/privacy decision.
- Scope: decide between disabling cloud backup, keeping device-transfer only, or updating disclosure in privacy/settings.
- Acceptance criteria: manifest/rules and policy copy match the intended privacy stance.
- Status: resolved and verified.

### P5: Architecture Cleanup After Risk Fixes

- Findings: TECH-001, TECH-002, TECH-003, UI-004.
- Priority rationale: maintainability matters, but should follow correctness/security fixes.
- Scope: continue extracting large files and resolve the legacy simulation package decision.
- Acceptance criteria: smaller focused modules, preserved behavior, tests updated.
- Status: deferred to a dedicated refactor cycle; too large to mix with release-hardening fixes.

### P6: Accessibility Polish

- Findings: UI-003, TEST-004.
- Priority rationale: important but not currently blocking a core workflow for all users.
- Scope: add useful attachment semantics and targeted UI smoke tests.
- Acceptance criteria: attachment viewer exposes localized semantic labels; managed-device test covers at least one attachment flow.
- Status: open.
