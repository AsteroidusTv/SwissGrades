# July 2026 Remediation Summary

This document is the durable summary of the July 2026 repository audit. Detailed working notes and cycle logs remain available in Git history but are not maintained as active documentation.

## Scope

The pass inspected the Android application, pure calculation domain, local persistence, imports and backups, attachments, localization, Compose flows, tests, CI, release configuration, privacy behavior, and product coherence.

## Completed remediation

- Hardened PlusPoints XML parsing against DTD/entities and bounded selected-file reads.
- Validated and canonicalized backup attachment paths and filesystem identifiers.
- Added adversarial import and backup regression tests.
- Disabled Android cloud backup for grades and attachments while preserving documented device-transfer behavior.
- Removed presentation strings and string-based decisions from domain behavior.
- Fixed target simulator synchronization and French promotion-status tone.
- Restricted official average targets to whole and half grades, including legacy normalization.
- Added promotion setup guidance, multi-grade planning, and current saved-grade impact calculations with tests.
- Established repeatable unit, release, lint, R8, bundle, and managed-device validation.

## Security and privacy result

No unresolved Critical or High security finding remained in the inspected scope. SwissGrades remains local-first, has no account or backend, and processes imported files as untrusted input. Manual exports remain user-controlled and are covered by the privacy policy.

## Remaining work

- Incrementally split oversized application and UI coordinators by coherent capability.
- Clarify promotion basket/domain roles before supporting additional school models.
- Improve attachment accessibility semantics and critical UI smoke coverage.
- Continue visual token consolidation.
- Validate roadmap features with testers before implementation.

## Verification baseline

Before repository cleanup, the validated baseline was:

- `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`: passed.
- `./scripts/release-check.sh`: passed.
- `./scripts/run-managed-device-tests.sh`: passed with 44 tests on `pixel2Api36` and 0 failures.

The repository-cleanup cycle removed only an unreachable legacy simulation prototype and its isolated tests. Post-cleanup validation passed:

- `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`: passed.
- `./scripts/release-check.sh`: passed.
- `./scripts/run-managed-device-tests.sh`: passed with 14 reachable-product tests on `pixel2Api36` and 0 failures.

The managed-device count decreased because 30 tests belonged exclusively to the unreachable prototype, not because active-product coverage was disabled.

## Conclusion

The inspected scope reached the audit’s convergence criteria: no unresolved Critical or High finding, no known core calculation defect, passing validation, and explicit deferral of remaining lower-severity work. This is not a claim that SwissGrades is bug-free.
