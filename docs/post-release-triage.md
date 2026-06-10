# Post-Release Triage Checklist

Use this during the tester window after a release.

## Daily Checks

- Check Play Console crashes and ANRs.
- Check tester feedback and screenshots.
- Move raw feedback into `FEEDBACK.md`.
- Mark anything data-loss or calculation-related as at least `P1` until proven otherwise.

## Patch Release Rules

Ship a patch quickly only for:

- Crash on startup or common navigation.
- Data loss or backup/import corruption.
- Incorrect averages, promotion points, semesters, or option composite calculations.
- Broken reset or migration behavior.

Avoid patch releases for:

- Cosmetic changes.
- New features.
- Dependency upgrades.
- Large refactors.

## Reproduction Standard

Before fixing a bug, capture:

- App version.
- Device and Android version.
- Language and theme.
- Exact screen.
- Exact input data.
- Expected result.
- Actual result.

## Fix Workflow

1. Reproduce or reduce the report to a clear hypothesis.
2. Add or update a test when the bug is logic, persistence, import/export, reset, or localization.
3. Keep the fix minimal.
4. Run `./scripts/release-check.sh`.
5. Commit locally.
6. Decide whether it belongs in a patch release.
