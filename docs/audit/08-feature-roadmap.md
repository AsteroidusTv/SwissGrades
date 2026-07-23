# Feature Roadmap

Status: initial roadmap created. These are not remediation tasks and should not be implemented during audit cleanup unless explicitly selected later.

## Near-Term

- Feature: Promotion setup assistant.
- Status: implemented and verified after the audit remediation pass.
- Problem addressed: users must know which three branches belong in the basket and why promotion may be locked.
- Target user: students configuring the app for the first time or changing subjects.
- Evidence from existing product or audit: current app requires exactly three non-option basket subjects; FUNC-003 shows the mental model is simplified but still domain-sensitive.
- Expected value: fewer configuration mistakes and clearer promotion summaries.
- Implementation complexity: Medium.
- Dependencies: stable basket/promotion model; localized copy.
- Risks: overcomplicating the currently simple add-subject flow.
- Privacy implications: none beyond existing local data.
- Suggested release phase: near-term after remediation.
- Minimum viable scope: dashboard checklist that says how many basket branches are selected and which grades are still missing.
- Success metric: fewer tester reports about promotion being “not calculable” unexpectedly.

- Feature: Human-readable export.
- Problem addressed: current backup is app-oriented, not useful for reviewing or sharing grades.
- Target user: students who want a report for themselves, parents, or tutoring.
- Evidence from existing product or audit: app already has local backup/export and computed summaries.
- Expected value: high practical utility without cloud services.
- Implementation complexity: Medium.
- Dependencies: stable formatting/localization and privacy warning.
- Risks: users may share sensitive data accidentally.
- Privacy implications: export must be explicit and clearly user-controlled.
- Suggested release phase: near-term.
- Minimum viable scope: local CSV or PDF summary export with subjects, notes, averages, points, semester.
- Success metric: tester usage/requests and successful export open rate.

## Medium-Term

- Feature: Grade impact explanations.
- Problem addressed: users see averages/points but may not understand which grade moved the result.
- Target user: students tracking progress over time.
- Evidence from existing product or audit: app already stores grade history and shows a basic evolution chart.
- Expected value: makes results more actionable.
- Implementation complexity: Medium.
- Dependencies: reliable grade history ordering and visual design pass.
- Risks: misleading explanations if weighting/composite rules are not described precisely.
- Privacy implications: local-only analysis.
- Suggested release phase: medium-term.
- Minimum viable scope: per-branch “latest grade changed official average from X to Y” and “largest weight” note.
- Success metric: users can explain why an average changed during testing.

- Feature: Configurable grading rules.
- Problem addressed: Swiss schools/classes may vary in weighting and promotion interpretations.
- Target user: students outside the currently assumed rule set.
- Evidence from existing product or audit: domain model currently hardcodes 1.0-6.0 quarter steps, half-point official rounding, 16-point basket threshold, and insufficiency rules.
- Expected value: broader correctness across schools.
- Implementation complexity: High.
- Dependencies: explicit product research and migration design.
- Risks: configuration complexity can make the app less approachable and calculations harder to trust.
- Privacy implications: local settings only.
- Suggested release phase: medium-term or later.
- Minimum viable scope: documented presets rather than fully custom formulas.
- Success metric: verified compatibility with at least two concrete school rule variants.

## Long-Term

- Feature: Privacy-preserving sync.
- Problem addressed: local-only data can be lost if the device is lost and manual backup is forgotten.
- Target user: students using multiple devices or wanting recovery.
- Evidence from existing product or audit: backup/import exists; Android backup privacy decision is open.
- Expected value: data durability and cross-device continuity.
- Implementation complexity: Very high.
- Dependencies: account model, encryption design, privacy policy, backend/infra, deletion/export rights.
- Risks: grades are sensitive; weak sync would be worse than no sync.
- Privacy implications: major; requires explicit consent, minimization, encryption, and account deletion.
- Suggested release phase: long-term only.
- Minimum viable scope: encrypted user-controlled cloud backup, not social sharing.
- Success metric: successful restore rate and no privacy incidents.

- Feature: Timetable or assessment reminders.
- Problem addressed: students may forget to enter grades or prepare for upcoming tests.
- Target user: students who want proactive tracking.
- Evidence from existing product or audit: grade entry is manual and date is currently creation/import date.
- Expected value: retention and practical study support.
- Implementation complexity: High.
- Dependencies: notification permissions, scheduling, recurrence model, UX for reminders.
- Risks: notification fatigue and scope creep toward full planner.
- Privacy implications: local notifications; no server needed.
- Suggested release phase: long-term.
- Minimum viable scope: local reminder to enter a grade after a test date.
- Success metric: reminder-created grades and opt-in retention.

## Experimental

- Feature: What-if multi-grade planning.
- Problem addressed: a single-next-test simulator does not model several future assessments.
- Target user: students planning a semester strategy.
- Evidence from existing product or audit: target simulator is a high-value feature and now has a saved target per branch.
- Expected value: strong “wow” feature if kept understandable.
- Implementation complexity: Medium to High.
- Dependencies: current simulator must be stable and trusted first.
- Risks: users may over-trust speculative plans.
- Privacy implications: local-only speculative data.
- Suggested release phase: experimental after current simulator is verified.
- Minimum viable scope: add temporary planned grades that do not affect real averages until confirmed.
- Success metric: testers understand distinction between real and simulated grades.

- Feature: Accessibility preferences.
- Problem addressed: high-density dark UI and animations may not fit all users.
- Target user: users with low vision, motion sensitivity, or accessibility tooling.
- Evidence from existing product or audit: UI uses custom animations and dense bespoke cards.
- Expected value: inclusive use and better review quality.
- Implementation complexity: Medium.
- Dependencies: accessibility audit and design tokens.
- Risks: too many preferences if not grounded in real needs.
- Privacy implications: local setting only.
- Suggested release phase: experimental/medium-term.
- Minimum viable scope: reduced-motion support and larger text density adjustments.
- Success metric: successful core-flow completion with TalkBack and large font settings.
