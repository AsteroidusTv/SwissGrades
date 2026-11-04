# Architecture proposée

Statut : architecture cible et première tranche en cours. Kotlin et Jetpack Compose sont conservés : Android reste la seule plateforme, aucune justification d'un changement de framework.

## Première tranche implémentée

`CarnetComponents.kt` porte l'accueil, les lignes adaptatives, le choix de matière pour une note et la navigation racine. `GradeTrackerApp` assemble ces composants avec les parcours existants. `GradeTrackerViewModel` conserve un `MainSection` temporaire, distinct de `GradeTrackerAppState` ; changer d'onglet n'écrit pas les données scolaires. Le format JSON et les frontières de fichiers restent ceux de la 1.6.0. Le formulaire de note a un contenu défilant et une action d'enregistrement hors de la zone défilante, avec espace clavier.

Le détail, les lignes de notes, les réglages, le bilan et les formulaires ont été recomposés selon Carnet. Les outils de fichiers gardent leurs frontières existantes. La correction des points de compensation ajoute un motif bloquant structuré au moteur pur ; elle ne modifie aucune note source.

## Responsabilités

- `domain` : notes, moyennes, cibles, périodes et évaluation de promotion, sans dépendance Android/UI/stockage.
- `data` : lecture/écriture de l'état compatible, pièces jointes, import PlusPoints, sauvegardes ; résultats d'erreur explicites.
- `features` : matières, saisie, détail, bilan, simulation, réglages. État d'écran et brouillons par parcours, pas de ViewModel propriétaire de toutes les fonctions.
- `app` : assemblage des dépendances, navigation et contexte année/période.

Un seul dépôt fait autorité pour l'état enregistré ; les écrans observent des projections immuables. Les mutations s'exécutent séquentiellement et ne sont confirmées dans l'UI qu'après une sauvegarde réussie. Les événements UI ne portent pas de règle métier.

## Persistance et compatibilité

Préférer d'abord un adaptateur au format existant plutôt qu'introduire une base de données sans besoin établi. Nom de préférences `grade_tracker_app_prefs`, clé `app_state`, schéma JSON 1 ; archives `.sgb` avec `manifest.json`, version 1 et pièces jointes.

Préserver l'applicationId `me.asteroidus.swissgrades`. La mise à jour installable nécessite la continuité de signature et un versionCode supérieur à celui distribué. Ces éléments seront vérifiés avec les APK et le pipeline avant livraison ; aucune clé privée ne doit être demandée dans le chat.

Si un nouveau champ métier est indispensable : migration explicite versionnée, copie de l'original, validation complète et commit atomique. Aucun reset silencieux ni écrasement d'un schéma inconnu. Ne pas déduire une correspondance scolaire ambiguë : demander uniquement les rôles manquants à l'utilisateur final.

## Alternatives et coûts

- Réécriture totale : augmente le risque sans avantage démontré pour le moteur pur ; reprendre les invariants vérifiés et réorganiser les responsabilités.
- Room : différé, car le volume et les requêtes ne justifient pas encore une migration supplémentaire. Réexaminer si l'état devient volumineux ou les écritures trop lentes.
- Multiplication de modules Gradle et framework d'injection : différée. Des packages cohérents et l'injection par constructeur suffisent au premier parcours.

## Gate

Le cadrage et le choix du propriétaire autorisent le lot UI Carnet sur les sources 1.6.0. Le stockage, les règles composées et les imports restent inchangés pendant ce lot ; leurs évolutions sont différées au code concerné. Avant toute migration : caractériser les formats et les échecs de restauration, résoudre les pondérations composées et figer les tests de migration. Avant livraison : parité complète, contrôles Android et répétition de mise à jour depuis 1.6.0.


## Présentation Carnet

`CarnetDetailComponents` sépare le résumé et les lignes de notes des lanceurs de fichiers du détail. Les quatre conditions de promotion sont projetées sous forme de valeurs et statuts structurés dans le ViewModel ; leur couleur ne dépend jamais du texte traduit. Les compteurs de notes et le contexte de période sont dérivés des données existantes.

`CarnetLocalization` regroupe les textes propres à cette interface dans une valeur immuable portée par `AppStrings`. Ce regroupement évite la limite JVM de paramètres de méthode qu'atteignait le constructeur de la grande classe de traductions. Aucune donnée stockée ni langue supplémentaire n'est introduite.
