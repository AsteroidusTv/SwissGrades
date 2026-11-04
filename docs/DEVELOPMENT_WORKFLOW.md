# Développement

Source de référence figée au tag v1.6.0, intégrée dans la branche locale `codex/carnet-refonte`. Dépôt local initialement vide. Le prototype de conversation est conservé séparément ; l'application Android est maintenant modifiée dans cette branche.

Ne pas pousser ni créer de commit sans demande. Préserver les données et l'historique du dépôt d'origine. Développer la refonte dans une branche `codex/` lorsque les sources Android seront intégrées.

Séquence : proposition UX → parcours Android de bout en bout → bilan/règles validées → outils et parité → essais de compatibilité → release. Chaque tranche doit compiler et rester testable.

L'environnement de référence demande JDK 21 et SDK Android 36. Les scripts documentés sont `scripts/gradlew21.sh`, `scripts/release-check.sh` et `scripts/run-managed-device-tests.sh`. Disponibilité locale à vérifier lors de l'intégration ; ne pas annoncer des builds réussis sans exécution.
