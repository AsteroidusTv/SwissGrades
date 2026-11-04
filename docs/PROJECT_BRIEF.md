# SwissGrades — refonte

Statut : direction Carnet choisie et première tranche Android en développement, 21 octobre 2026.

## Décisions du propriétaire

- Android uniquement.
- Public : élèves des lycées neuchâtelois. Interprétation du périmètre existant : maturité gymnasiale, pas ECG ni maturité professionnelle.
- Repenser l'application et l'interface, en conservant les fonctionnalités et le but de SwissGrades.
- Priorité : ergonomie et UI.
- Mise à jour de l'application existante avec conservation des données et lecture des sauvegardes précédentes.
- Version utilisée : 1.6.0.
- Direction choisie : Carnet. Plan détaillé dans `CARNET_COMPONENT_PLAN.md` ; développement autorisé après ce plan.

## Référence vérifiée

Release : https://github.com/AsteroidusTv/SwissGrades/releases/tag/v1.6.0

Le tag correspond à `be8a8c4165b01ed955c0cb4271129352e7404a5d`. Le README annonce encore 1.5.0 ; le tag et le code sont la référence de comparaison. Le dossier de travail local était un dépôt vide sans commit ni remote.

## Parcours et critères d'acceptation

1. Ouvrir l'application et retrouver son année, sa période et ses matières.
2. Depuis une matière, ouvrir la saisie en une action ; saisir note et poids, enregistrer. Depuis l'accueil : accès direct à la saisie avec choix de matière.
3. Comprendre la moyenne calculée et arrondie, consulter/modifier/supprimer les notes et leurs pièces jointes.
4. Comprendre chaque condition de promotion, les données manquantes et les limites de la projection.
5. Définir un objectif et simuler une à trois notes sans modifier les résultats enregistrés.
6. Importer PlusPoints, restaurer/exporter une sauvegarde et produire un PDF.
7. Retrouver après mise à jour tous les identifiants, notes, poids, dates, descriptions, périodes, matières, options, objectifs, réglages et pièces jointes.

La parité inclut options simples/composées, couleurs/icônes, inclusion et panier, historique, impact actuel des notes, français/anglais et thèmes système/clair/sombre. Le prototype ne prétend pas implémenter cette parité.

## Non-objectifs

Pas de serveur, compte, synchronisation, publicité, analytics, nouvelles plateformes ou moteur de règles multi-cantons. Maintien du modèle local existant par défaut, la réponse « 1.6.0 » étant une précision de version et pas une décision sur la confidentialité.

Pas de décision officielle de promotion ou de calcul du certificat final de maturité : suivi personnel et projection sur les résultats saisis.

## Première tranche recommandée

Valider la navigation et la saisie sur la maquette, puis livrer un parcours Android natif matières → détail → saisie → moyenne, branché sur un stockage compatible 1.6.0. Étendre ensuite au bilan, simulateur et outils, sans retirer les fonctions existantes de la version livrable.
