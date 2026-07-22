# UI/UX Audit

Status: initial static audit pass in progress.

## Findings

### UI-001: Target Card And Simulator Can Present Conflicting Target Values

- ID: UI-001
- Title: target card and simulator can present conflicting target values.
- Category: UI/UX, grades-specific usability.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: branch detail screen, average target card, grade simulator.
- Observed behavior: the branch target card displays the saved target, while the simulator owns a separate local input initialized only once per subject. The two can diverge after editing the saved target.
- Expected behavior: the saved target should clearly feed the simulator, or the simulator should explicitly show that it uses a separate ad-hoc value.
- Reproduction steps where applicable: same as FUNC-001.
- Technical or UX impact: users may trust a simulated required grade calculated from an old target.
- Root cause, when known: separate local input state without synchronization rules.
- Proposed remediation: synchronize collapsed simulator default from the saved target and keep active expanded edits local.
- Verification method: manual branch-detail check and automated UI test.
- Status: resolved and verified.

### UI-002: Blocked Status In French Loses Warning Visual Emphasis

- ID: UI-002
- Title: blocked status in French loses warning visual emphasis.
- Category: Visual design consistency and grades-specific usability.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: dashboard summary promotion chip.
- Observed behavior: French `Bloqué` maps to neutral tone because the implementation checks `Bloque`.
- Expected behavior: a blocked promotion status should be visually warning-colored in every language.
- Reproduction steps where applicable: same as FUNC-002.
- Technical or UX impact: the visual hierarchy understates an important negative result.
- Root cause, when known: status tone is derived from localized text instead of structured state.
- Proposed remediation: use structured state for visual tone or add the correct localized string immediately.
- Verification method: UI/unit test that `Bloqué` maps to `DashboardStatusTone.NEGATIVE`.
- Status: resolved and verified.

### UI-003: Attachment Viewer Images Lack Useful Screen-Reader Labels

- ID: UI-003
- Title: attachment viewer images lack useful screen-reader labels.
- Category: Accessibility.
- Severity: Low.
- Confidence: High.
- Affected files or screens: attachment viewer dialog in `GradeTrackerBranchDetailScreen.kt`.
- Observed behavior: full-screen attachment images use `contentDescription = null`; draft thumbnails all use the generic attached-photos title.
- Expected behavior: image controls should expose useful labels such as “Exam photo 1 of 3” and the close button should remain discoverable.
- Reproduction steps where applicable: open a grade attachment viewer and inspect semantics.
- Technical or UX impact: screen-reader users get less context when reviewing attached exam photos.
- Root cause, when known: visual image preview was implemented without per-image semantic labels.
- Proposed remediation: add localized `examPhotoPosition` copy and apply it to thumbnails/viewer images where meaningful.
- Verification method: Compose semantics test or manual Accessibility Scanner/TalkBack inspection.
- Status: open.

### UI-004: Main Visual System Is Bespoke But Not Fully Tokenized

- ID: UI-004
- Title: main visual system is bespoke but not fully tokenized.
- Category: Visual design consistency and maintainability.
- Severity: Low.
- Confidence: High.
- Affected files or screens: shared cards, buttons, forms, dialogs across `ui/app`.
- Observed behavior: key colors and shape constants exist in `GradeTrackerUiTokens.kt`, but many spacings, heights, icon sizes, and text styles are repeated directly in screen files.
- Expected behavior: repeated interaction and layout primitives should use stable tokens/components, especially for cards, bottom buttons, text fields, and chip controls.
- Reproduction steps where applicable: inspect repeated `RoundedCornerShape`, `height`, and `padding` values across app screens.
- Technical or UX impact: small alignment regressions are likely when one screen is adjusted without the others.
- Root cause, when known: visual polish was iterated directly in screens.
- Proposed remediation: extract card/header/form/bottom-button primitives gradually; avoid a large redesign.
- Verification method: screenshot/manual comparison of core screens after extraction.
- Status: open.
