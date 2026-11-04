# Vérification

## Prototype

Vérifier affichage étroit et large, navigation matières/détail/bilan/saisie, validation de note, mise à jour des valeurs de démonstration et absence de débordement. Cette vérification ne valide pas Android ni une migration.

## Production, avant livraison

- Exemples métier : seuils d'arrondi, poids, note absente, cumul S1/S2, options composées, cibles impossibles, compensation négative avec panier suffisant, seuil 16, quatre/cinq insuffisances, note sous 3.
- Compatibilité : fixtures synthétiques produites au format 1.6.0, lecture et round-trip avec toutes les options, objectifs, paramètres et pièces jointes ; ancien JSON sans champs facultatifs.
- Sécurité : JSON tronqué/schéma futur, chemins malveillants, archive volumineuse, doublons, pièces jointes manquantes, erreur disque et interruption pendant commit/rollback. L'état antérieur doit rester récupérable.
- Parcours Android : onboarding, ajouter/modifier/supprimer une note, modifier période, simulation sans écriture, restauration, PDF ; retours système et recréation du processus.
- Accessibilité : TalkBack, texte agrandi, cibles 48 dp, contraste, états non fondés sur la couleur, alternative aux gestes de balayage.
- Release : tests JVM, build debug et tests instrumentés ciblés, puis lint/release/R8 et tests gérés du projet. Répétition de mise à jour depuis APK 1.6.0 avec signature compatible et données de test.

Les contrôles de la première tranche sont consignés ci-dessous ; les contrôles de livraison restent à exécuter.

## Vérification du prototype effectuée le 21 octobre 2026

- Syntaxe JavaScript vérifiée avec `node --check` : réussite.
- Aperçu dans le navigateur : matières → Mathématiques → saisie.
- Note 6,25 refusée avec message ; ajout de 5,5 accepté et moyenne calculée passée de 4,5 à 4,75, arrondie à 5,0.
- Simulation consultée ; objectif 6 avec une note supplémentaire signalé hors de portée.
- Bilan mis à jour (panier 18,5 et compensation +3,5 sur le jeu de démonstration modifié).
- Navigation entre les propositions Carnet et Vue d'ensemble vérifiée.
- Rendu observé à 390 et 320 pixels, sans débordement horizontal visible sur l'accueil.

Limites : thème sombre observé ; contraste complet, thème clair et lecteur d'écran non testés. Prototype sans stockage Android, pièces jointes ou migration.


## Première tranche Android Carnet — 21 octobre 2026

Commande exécutée avec succès :

```bash
./scripts/gradlew21.sh testDebugUnitTest assembleDebug lintDebug pixel2Api36DebugAndroidTest --no-parallel -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect -Pandroid.testInstrumentationRunnerArguments.class=me.asteroidus.swissgrades.ui.app.CarnetInstrumentedTest,me.asteroidus.swissgrades.ui.app.GradeTrackerAppInstrumentedTest
```

- 136 tests JVM : zéro échec ; navigation et brouillon sans écriture, retour après reset, compensation négative et seuil zéro inclus.
- 14 tests Android sur Pixel 2 API 36 : zéro échec ; parcours existants et deux tests Carnet.
- Build debug et lint debug : réussite. Un lint réussi ne signifie pas absence de tous les avertissements historiques.
- Captures natives inspectées : accueil clair, accueil contraint à 320 dp avec police 180 %, saisie avec clavier et bouton d'enregistrement entièrement visible. Captures conservées dans `qa/carnet-first-slice/`.
- Les tests historiques du bilan ouvrent désormais l'onglet Bilan ; la section des réglages est atteinte par défilement.

Limites : la contrainte 320 dp porte sur l'accueil ; la feuille de saisie utilise sa fenêtre modale et n'est pas démontrée à cette largeur. Le rendu sombre, le paysage, la police système réelle à 200 %, TalkBack et la matrice tablette restent à vérifier dans les lots suivants. La parité de release, la signature et la mise à jour depuis une APK 1.6.0 installée ne sont pas validées par ces tests. Aucun changement du format de stockage ni des règles d'options composées dans ce lot.


## Alignement visuel Carnet et suppression par glissement — 21 octobre 2026

- Passage complet réussi : 139 tests JVM, 17 tests Android sur Pixel 2 API 36, assembleDebug et lintDebug.
- Les tests supplémentaires couvrent l'annulation puis confirmation du glissement sur une note et une matière, le maintien des autres données, le retour à la même note après une édition dans un historique de 30 notes, la saisie plein écran, le thème sombre et le simulateur.
- Le test de projection vérifie quatre statuts structurés, notamment la compensation négative malgré un panier suffisant et l'exclusion d'une matière hors bilan.
- Les tests de scroll ciblent les éléments de la liste ; ils ne dépendent plus de coordonnées fixes de balayage pour atteindre les éléments.
- Les captures de l'accueil, du détail, du bilan, des réglages, du simulateur sombre et de la saisie avec clavier ont été inspectées. Les couleurs personnalisées de matière et les données de test peuvent différer de celles du prototype.
- Les anciens réglages et le simulateur utilisent désormais des dialogues/menus ciblés : les tests ouvrent ces contrôles avant de sélectionner leurs valeurs.
- Les textes Carnet ont été regroupés pour éviter la limite de signature JVM du constructeur d'AppStrings ; aucun format de données n'a changé.

La vérification est fonctionnelle et visuelle manuelle, sans comparaison automatique pixel par pixel. La matrice complète tablette/paysage et la mise à jour signée depuis 1.6.0 restent des contrôles de livraison.


Dernier contrôle après finition du bouton de confirmation et de la légende de période : les 139 tests JVM, les 5 tests Carnet sur émulateur, assembleDebug et lintDebug réussissent. Les 12 autres tests d'interface avaient réussi lors du passage complet de 17 tests. Les captures finales sont conservées dans `qa/carnet-reference/`.
