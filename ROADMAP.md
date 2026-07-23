# SwissGrades Roadmap

This roadmap lists hypotheses, not commitments. New work should be validated with tester evidence and kept separate from security or architecture remediation.

## Active candidates

### Human-readable export

- Problem: `.sgb` backups are intended for the app, not for users who want to review or share a summary.
- MVP: an explicit local CSV or PDF export containing subjects, grades, weights, official averages, points, and period.
- Risk: grades are sensitive; sharing must remain an explicit user action with clear privacy copy.
- Validation: measure tester demand and successful opening of generated exports before expanding formats.

### Accessibility pass

- Problem: custom cards, attachments, motion, and dense layouts have not been fully verified with TalkBack, large fonts, or reduced motion.
- MVP: attachment semantics, large-font checks on critical screens, reduced-motion behavior, and a small stable UI test set.
- Risk: isolated fixes can create inconsistent interaction patterns; verify complete journeys.

### Configurable promotion presets

- Problem: grading and promotion rules may differ between Swiss schools.
- MVP: documented, researched presets rather than arbitrary formulas.
- Dependencies: concrete school-rule evidence, compatibility behavior, and migration design.
- Risk: incorrect configurability is worse than a clearly scoped fixed model.

## Later candidates

### Historical grade impact

Current grade impact compares the official average with and without a saved grade. A true “this grade changed the average from X to Y” feature would require explicit historical snapshots or event semantics; it must not infer history from current data.

### Local reminders

An optional reminder to enter a result after an assessment could improve retention without requiring a server. Notification permissions, scheduling, recurrence, and notification fatigue must be validated first.

### Privacy-preserving sync

Cross-device recovery has value, but accounts and cloud storage would introduce a backend, encryption, deletion/export rights, operational maintenance, and substantial privacy obligations. Do not start without a dedicated architecture and privacy decision.

## Implemented hypotheses

- Promotion setup assistant.
- Saved branch average targets.
- One-to-three-grade what-if planning with temporary, non-persisted inputs.
- Current saved-grade impact explanations.
