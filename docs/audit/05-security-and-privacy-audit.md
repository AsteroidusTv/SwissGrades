# Security And Privacy Audit

Status: initial static audit pass in progress.

## Findings

### SEC-001: PlusPoints XML Import Parser Is Not Hardened

- ID: SEC-001
- Title: PlusPoints XML import parser is not hardened.
- Category: Security and privacy.
- Severity: High.
- Confidence: Medium.
- Affected files or screens: PlusPoints import; `app/src/main/java/me/asteroidus/swissgrades/ui/app/PlusPointsImportCoordinator.kt`.
- Observed behavior: untrusted XML is read with `readBytes()` and parsed by `DocumentBuilderFactory.newInstance()` without disabling DOCTYPE declarations, external entities, XInclude, external DTD loading, or entity expansion.
- Expected behavior: XML imported from user-selected files should reject DTD/entity features and should have a defensible size limit.
- Reproduction steps where applicable: inspect `parsePlistRoot()` around lines 371-374; no hardening features are set.
- Technical or UX impact: a malicious PlusPoints-like file may trigger unsafe XML parser behavior or resource exhaustion. Exploitability depends on Android parser defaults and available URI/file access, so this is not classified Critical.
- Root cause, when known: DOM parser was used directly for a trusted-format import path without defensive parser configuration.
- Proposed remediation: add a hardened XML parser factory, reject oversized imports before parsing, and add negative tests for DOCTYPE/entity input.
- Verification method: unit tests that malicious XML with DOCTYPE/external entities fails before parsing; successful tests for valid PlusPoints samples.
- Status: resolved and verified.

### SEC-002: Backup Attachment Paths And IDs Are Not Canonicalized

- ID: SEC-002
- Title: backup attachment paths and IDs are not canonicalized.
- Category: Security, privacy, data integrity.
- Severity: Medium.
- Confidence: Medium.
- Affected files or screens: backup import/export and attachment restore; `AppBackupCoordinator.kt`, `GradeAttachmentStorage.kt`, `GradeTrackerRepository.kt`.
- Observed behavior: imported `attachment.filePath`, `note.id`, and `attachment.id` values from backup JSON are used to build `File` paths. Zip entries are protected against zip-slip, but manifest values are not constrained to safe relative archive paths or safe identifier components.
- Expected behavior: backup manifests should accept only expected relative attachment paths and safe id components before restoring files.
- Reproduction steps where applicable: inspect `validateImportedAttachments()` and `restoreAttachmentsIntoPreparedRoot()` around lines 250-281.
- Technical or UX impact: a malformed or malicious backup can create broken attachment paths, restore files outside the intended prepared note directory, or persist unsafe path strings that later affect attachment deletion/display.
- Root cause, when known: zip entry validation was implemented, but manifest-path validation was not equivalently canonicalized.
- Proposed remediation: validate that manifest attachment paths are relative under `attachments/`, reject path separators in note/attachment ids used for filesystem paths, canonicalize restore targets under `preparedAttachmentsRoot`, and add instrumented tests.
- Verification method: instrumented backup import tests for `../`, absolute paths, separator-containing ids, and a valid round-trip backup.
- Status: resolved and verified.

### SEC-003: Platform Backup Includes Grades And Photos

- ID: SEC-003
- Title: platform backup includes grades and photos.
- Category: Privacy.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: `AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml`, `PRIVACY_POLICY.md`.
- Observed behavior: Android Backup was enabled and explicitly included the grade SharedPreferences file and `attachments/`. The privacy policy said data was local-first and noted platform-level backups may depend on Android settings, but it did not explicitly state that grades/photos were included in Android cloud backup when enabled.
- Expected behavior: privacy-sensitive educational data should either be excluded from cloud backup by default or disclosed very explicitly in privacy copy and release notes.
- Reproduction steps where applicable: inspect `android:allowBackup="true"` and backup include rules.
- Technical or UX impact: users may interpret “local-first” as “never leaves device,” while Android cloud/device-transfer backup may copy grades and attachments according to OS settings.
- Root cause, when known: backup convenience was prioritized, with privacy disclosure left broad.
- Proposed remediation: disable Android cloud backup for local grade data and attachments, keep Android 12+ device-transfer restore, and update privacy policy copy.
- Verification method: manifest/rules review plus privacy-policy copy review.
- Status: resolved and verified.
