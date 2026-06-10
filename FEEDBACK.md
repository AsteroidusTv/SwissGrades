# SwissGrades Feedback Triage

Use this file to keep tester feedback actionable while the release is being tested.

## Intake Rules

- Keep raw tester wording when it describes confusion or expectations.
- Add exact app version, device, Android version, language, theme, and steps when available.
- Do not decide on a fix before the issue is reproducible or clearly scoped.
- Prefer small patch releases for crashes/data-loss bugs only.

## Priority

- `P0`: Crash, data loss, app cannot be used.
- `P1`: Core calculation, semester, backup/import, or reset behavior is wrong.
- `P2`: Confusing UX, visual bug, localization issue, non-blocking workflow issue.
- `P3`: Feature request, polish, or low-frequency edge case.

## Status

- `New`: Not triaged yet.
- `Needs Info`: Missing device, steps, screenshots, or expected behavior.
- `Reproducible`: Confirmed locally or by multiple testers.
- `Ready`: Fix is clear and scoped.
- `Fixed`: Fixed locally.
- `Released`: Included in a published version.
- `Won't Fix Now`: Valid but intentionally postponed.

## Bugs

| ID | Priority | Status | Version | Device / Android | Language / Theme | Summary | Repro Steps | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| B-001 |  | New |  |  |  |  |  |  |

## Confusing UX

| ID | Priority | Status | Version | Screen | Tester Quote | Expected | Decision |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UX-001 |  | New |  |  |  |  |  |

## Calculation / Data Checks

| ID | Priority | Status | Version | Area | Input Data | Expected Result | Actual Result |
| --- | --- | --- | --- | --- | --- | --- | --- |
| C-001 |  | New |  |  |  |  |  |

## Feature Requests

| ID | Priority | Status | Version | Request | User Value | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| F-001 | P3 | New |  |  |  |  |

## Patch Release Candidates

Only add items here if they justify a patch release before the next planned feature version.

| ID | Reason | Risk If Not Fixed | Fix Size | Decision |
| --- | --- | --- | --- | --- |
|  |  |  |  |  |
