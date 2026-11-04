# Carnet — plan de composition et de développement

Direction choisie par le propriétaire le 21 octobre 2026. Ce document fixe la cible complète ; la colonne « lot » distingue les étapes, pas des fonctionnalités supprimées. La première implémentation conserve les parcours existants derrière la nouvelle entrée Carnet.

## 1. Cadre commun

Ordre vertical sur téléphone : barre système → en-tête → contexte année/période → contenu défilant → action principale si nécessaire → navigation → zone système. Aucun bouton flottant ne doit recouvrir une note ou la dernière ligne. Un seul propriétaire des insets par fenêtre ; les feuilles modales gèrent leur propre clavier.

Trois destinations racines : **Matières**, **Bilan**, **Réglages**. Démarrage sur Matières. Le détail conserve la navigation racine comme la maquette ; la saisie et les formulaires la masquent pendant un brouillon. Retour : fermer la photo, puis la feuille, puis le détail ; Bilan/Réglages reviennent aux matières. Retour et clic sur l'onglet courant ne doivent ni réimporter ni réenregistrer les données.

L'année/période est une seule source de vérité partagée par Matières et Bilan. L'onglet et le défilement sont des états de présentation, sans changement du JSON 1.6.0. Le défilement de chaque liste est conservé lors d'un aller-retour ; un changement d'année/période crée un contexte distinct.

## 2. Dimensions et règles anti-débordement

| Élément | Contrat |
| --- | --- |
| Zone utile | Largeur disponible après safeDrawing, y compris découpes latérales en paysage. Contenu centré, largeur maximale 720 dp sur grand écran. |
| Marges | 16 dp à moins de 360 dp ; 20 dp sinon ; espacement entre sections 24 dp, dans une section 12 dp, icône/texte 8–12 dp. |
| Cibles | Minimum 48 × 48 dp. Icônes 24 dp ; les visuels plus petits gardent une grande cible. |
| Typographie | Corps 14 sp, secondaire 13 sp, titres 28 sp, moyenne principale 56 sp, valeurs de lignes 20–22 sp, comme la maquette. Taille système respectée ; aucune réduction automatique pour faire tenir un texte. |
| Textes | Titres longs sur plusieurs lignes. Ellipse réservée à la ligne d'aperçu secondaire si le détail complet est accessible. Jamais sur erreur, action, valeur ni coefficient. |
| Lignes de matière | Nom dans l'espace restant ; moyenne protégée. À police ≥ 1,5 ou largeur < 340 dp : moyenne sous le nom, pas de compétition entre colonnes. |
| Boutons | Hauteur minimale, jamais maximale. Libellé centré et pouvant passer sur deux lignes. Bouton large sous le formulaire. |
| Sélecteurs | Petits choix courts en ligne ; autres choix empilés ou FlowRow. Pas de grille à colonnes rigides pour des intitulés traduits. |
| Listes | Une seule liste verticale par écran ; clés stables par identifiant. Pas de LazyColumn non bornée imbriquée dans un scroll vertical. |
| Formulaire et clavier | Feuille entièrement dépliée, contenu vertical défilant et espace IME. Tous les champs, erreurs et CTA doivent rester atteignables en paysage et à police 200 %. |
| Thèmes | Tokens sémantiques clair/sombre ; pas de couleur de texte claire codée en dur sur un fond pouvant changer. Icônes de matière personnalisables dans le vert du thème. |
| Mouvement | Fondu d'entrée de 180 ms et de sortie de 100 ms, sans translation ni animation de taille. En-tête stable (ligne de marque d'au moins 48 dp, avec ou sans action) sur les écrans après l'accueil initial. Défilement distinct par onglet et conservé au retour du formulaire ; respect des animations système désactivées. |

## 3. Catalogue des composants

| Composant | Emplacement et responsabilité | États / prévention |
| --- | --- | --- |
| `CarnetNavigation` | Bas des trois destinations, icône + libellé toujours visible | Sélection sémantique ; tailles extensibles ; désactivée pendant une opération de fichiers incompatible |
| `CarnetHeader` | Haut de Matières et Bilan : titre, puis bouton période pleine largeur | Aucun titre en concurrence avec plusieurs boutons ; année et période renvoient à un seul sélecteur |
| `CarnetSubjectRow` | Liste des matières et choix de matière pour une note | Repère, nom, statut secondaire, moyenne ; vide affiché explicitement ; nom non utilisé pour calculer le rôle |
| `SubjectActions` | Menu de la matière, proche du nom | Modifier ; suppression par glissement avec confirmation, sans poubelle permanente ; action sémantique pour les lecteurs d’écran ; option protégée selon règles existantes |
| `PrimaryActionBar` | Sous la liste de matières : « Ajouter une note » ; sous les formulaires longs selon place | Hors superposition ; désactivée pendant sauvegarde ; toujours accessible au clavier |
| `PeriodContext` | En-tête et destination de la note | Distinguer période consultée et semestre d'enregistrement ; la vue S2 précise le cumul annuel |
| `PromotionOverview` | Bilan : moyenne puis état de projection et conditions | Valeurs nulles ≠ zéro ; informations manquantes visibles ; état expliqué par texte et icône |
| `PromotionConditionRow` | Quatre lignes : panier, compensation, insuffisances, minimum | Condition évaluée / non satisfaite / indéterminée ; détail accessible ; jamais de jauge décorative à seuil inventé |
| `FormField` | Saisie note, description, nom, objectif | Label permanent, valeur conservée, erreur au champ et message lisible ; clavier adapté |
| `WeightPicker` | Après la note | Valeurs 1 / 0,5 / 0,25 visibles ; label « Coefficient » ; description complète pour TalkBack |
| `AttachmentStrip` | Sous description, avant validation | Miniatures à ratio constant, suppression indépendante, indication 0–5 ; chargement/échec ne modifient pas la hauteur |
| `ConfirmationDialog` | Suppression, remplacement, reset, changement d'option | Objet/conséquence nommés ; annuler par défaut ; long contenu défilant ; actions non tronquées |
| `OperationFeedback` | Près de l'action import/export | Progression, succès, échec et réessai ; pas de faux succès si l'écriture échoue |

## 4. Composition de chaque écran

### Matières — lot 1

1. Titre « Mes matières » ; action secondaire « Ajouter une matière » dans le contenu, distincte de l'ajout de note.
2. Bouton année/période, sur sa propre ligne.
3. Liste des matières, sans métriques de promotion avant elle. Ordre stable ; conserver l'option et toutes les matières existantes, même exclues du bilan.
4. Chaque ligne ouvre le détail. Moyenne absente : « Aucune note » ; matière exclue : statut explicite. Personnalisation de l’icône conservée, avec le vert du thème ; anciennes couleurs conservées dans les données.
5. Lien compact vers Bilan après la liste ; si configuration incomplète, invitation concise, pas une checklist occupant l'accueil.
6. Bouton dans la liste après les matières, avant le résumé de promotion, conformément à la maquette : « Ajouter une note ». Ouvre le choix de matière ; ensuite la saisie existante, avec semestre présélectionné. Pour une option composée, laisser choisir explicitement la sous-discipline dans le parcours de détail.
7. Aucun utilisateur sans matière : l'option existe après onboarding ; l'action d'ajout de matière reste immédiatement disponible.

### Bilan — lots 1 puis 2

1. Même année/période et même largeur de contenu que Matières.
2. Moyenne générale et nombre de branches contributrices.
3. État « projection », jamais décision officielle ; détails manquants affichés.
4. Quatre conditions en lignes verticales, valeurs et seuils voisins.
5. Matières responsables d'une insuffisance, lien vers leur détail.
6. Assistant de configuration existant lorsque le panier n'est pas exploitable ; ses actions ouvrent le formulaire/détail concerné et le retour restaure Bilan.

Lot 1 : déplacement du bilan existant vers une destination dédiée ; correction du cas de compensation négative. Lot 2 : remplacer sa composition par les quatre lignes et une explication précise de chaque état.

### Détail de matière — lots 1 puis 2

1. Retour, titre complet, menu modifier/supprimer ; titre prend la largeur restante et peut grandir.
2. Contexte année/période et, pour une option composée, sous-disciplines avec sélection explicite.
3. Moyenne arrondie estimée dominante ; moyenne brute, nombre de notes et contribution en dessous.
4. Action « Ajouter une note » près de ce résumé ; objectif/simulation en action secondaire.
5. Historique récent puis liste des notes. Graphique secondaire, jamais nécessaire pour lire les données ; état vide utile.
6. Ligne de note : valeur, coefficient, description, date/période, indication photos. Modifier reste accessible depuis la ligne et le crayon. Supprimer passe par glissement et confirmation ; une action sémantique permet la même opération au lecteur d’écran, sans petite poubelle.
7. Impact actuel accessible depuis la note ; ne pas le présenter comme évolution historique.

Lot 1 conserve le détail fonctionnel et corrige la saisie ; lot 2 applique cette nouvelle hiérarchie et adapte les métriques/graphes aux grandes polices.

### Ajouter / modifier une note — lot 1 puis finition lot 2

Écran complet de saisie défilant, comme dans la maquette. Ordre : titre contextualisé → année et semestre de destination → sélecteur S1/S2 → valeur → coefficient → description facultative → photos → erreurs → enregistrer. En modification, l'impact est une information secondaire repliable à terme.

La note est validée avant toute écriture. Le clavier décimal accepte virgule et point. Un choix de poids ne réinitialise pas la valeur. Enregistrement invalide : conserver tout le brouillon, message annoncé et atteignable. Fermer la feuille abandonne les nouveaux fichiers temporaires, pas les fichiers déjà enregistrés. La protection explicite d'un brouillon modifié sera traitée au lot 2.

### Ajouter / modifier une matière — lot 2

Retour + titre → nom → inclusion dans le bilan → panier et explication → icône → erreur → enregistrer. Les rôles de panier ne sont pas déduits du nom. Les lignes de switch réservent leur largeur au contrôle ; le texte occupe un espace pondéré et peut grandir. Icônes en grille adaptative ; la sélection est indiquée par coche et sémantique.

### Objectif et simulation — lot 2

Depuis le détail. Cible enregistrée séparée du plan temporaire. Ordre : cible → une/deux/trois notes → poids commun → résultat et conditions de calcul. Résultat : atteignable / déjà atteint / impossible / données insuffisantes. Les notes projetées ne sont jamais sauvegardées. Les contrôles passent à la ligne à petite largeur ; aucune animation du champ actif.

### Année et période — lot 2

Retour/annuler → année 1/2/3 → premier semestre ou vue annuelle cumulée → explication → appliquer. Changer le brouillon ne modifie pas les écrans sous-jacents avant confirmation. Le semestre d'une note reste distinct du filtre de consultation. L'initialisation conserve les comportements 1.6.0.

### Réglages — lots 1 puis 3

Titre sans retour redondant sur la destination racine. Sections dans cet ordre : apparence/langue → parcours scolaire/option → données (PDF, sauvegarde, restauration, PlusPoints) → réinitialisation séparée visuellement. Lot 1 : navigation racine et outils existants ; lot 3 : remplacer les longues cartes de choix par lignes et dialogues ciblés.

Une opération en cours ne peut pas être lancée deux fois. Quitter un import préparé doit le nettoyer ; pendant application/restauration, protéger la navigation et afficher la progression. Les confirmations précisent si les données sont remplacées ou fusionnées.

### Première utilisation — lot 3

Langue accessible → choix importer ou démarrer → option → année/période → accès au carnet. Une étape par décision importante ; progression compréhensible et retour sans écritures partielles. Aucun compte ni permission avant une action qui l'exige. Un échec de chargement existant ne doit pas devenir un faux « nouvel utilisateur » : traiter ce cas dans le lot fiabilité.

### Photos, imports, sauvegardes, PDF — lot 3 / fiabilité

- Source photo : caméra/galerie et annuler ; refus de permission compréhensible, retour au brouillon intact.
- Visionneuse : image contenue, zoom existant conservé, fermer et compteur accessibles ; ratio stable pendant chargement/échec.
- Import : fichier choisi → aperçu (source, période, contenu) → confirmation → progression → bilan ; échec sans perdre les données précédentes.
- Export : contexte visible → destination système → progression → succès/échec. Annuler le sélecteur n'affiche pas un succès.
- Sauvegarde/restauration : compatibilité `.sgb` 1.6.0 ; limites et validation des entrées, transaction état/photos et récupération à finaliser avant livraison.

## 5. Matrice de validation

| Cas | Vérification attendue |
| --- | --- |
| 320 / 360 / 412 dp, paysage | Pas de chevauchement, bouton final atteignable, safe areas respectées |
| 600 / 840 dp | Contenu centré et largeur bornée, pas d'étirement inutile |
| Police 100 / 150 / 200 % | Titres et valeurs visibles ; lignes passent en disposition verticale ; contrôles ≥ 48 dp |
| Français / anglais, noms très longs | Pas d'ellipse sur action, valeur, erreur ; titre complet dans le détail |
| Clair / sombre | Contraste de texte et statut ; aucune couleur seule porteuse de sens |
| IME visible, description multilignes, 5 photos | Dernier champ et Enregistrer accessibles au défilement ; pas de double espace clavier |
| Zéro / une / nombreuses notes | État vide utile, liste stable, aucune division par zéro |
| Navigation et rotation | Onglet, défilement et brouillon conservés selon leur cycle de vie ; aucune duplication de sauvegarde |
| Compatibilité | Schéma, identifiants, poids, pièces jointes inchangés sur le lot UI ; fixtures 1.6.0 et essais de mise à jour avant release |

Tests Compose ciblés : accès saisie depuis accueil, conservation du choix d'onglet au retour, petites largeurs et police agrandie, bouton enregistrer après défilement. Compléter par captures sur émulateur réel ; des assertions de clic ne prouvent pas le rendu visuel.

## 6. Séquence et décisions techniques

1. **Lot 1 maintenant** : sources 1.6.0 dans une branche isolée, thème Carnet, accueil/lignes, navigation, sélecteur de matière, saisie défilante, compensation négative, tests ciblés et build.
2. **Lot 2** : détail, bilan à quatre conditions, formulaires et simulation selon ce plan ; vérifications visuelles étendues.
3. **Lot 3** : réglages, onboarding, photos et outils ; rendre explicites les erreurs de stockage et les transactions de restauration.
4. **Livraison** : parité, sécurité des imports, mise à jour depuis 1.6.0 signée, tests de récupération, validation release et accessibilité.

Pas de nouvelle dépendance de production ni changement de format pour le lot 1. La restructuration des responsabilités se fait par composants cohérents, sans réécrire les règles composées non encore validées. Le choix de Carnet est acquis ; aucune nouvelle validation de direction n'est requise pour ce lot.


## 7. Avancement vérifié

Le lot 1 est implémenté dans `codex/carnet-refonte` : accueil et lignes adaptatives, trois destinations, choix de matière pour la saisie, suppression explicite et swipe conservé, formulaire défilant avec confirmation fixe au-dessus du clavier, thème Carnet, correction de compensation et tests. Les vérifications finales réussies sont décrites dans `TEST_STRATEGY.md` (136 tests JVM, 14 tests Android, build debug et lint). Les lots 2–4 restent à réaliser ; le détail et les outils existants restent accessibles.


## 8. Alignement sur la maquette et décision de suppression

Le propriétaire demande de reprendre précisément Carnet et impose la suppression par glissement pour notes et matières, sans petite poubelle permanente. L'en-tête SwissGrades, la typographie, la navigation compacte, les boutons à coins 16 dp, les surfaces à coins 22 dp, les lignes de notes, le bilan à quatre conditions et les réglages en lignes sont implémentés selon cette référence. Les pictogrammes de suppression ne sont révélés que dans le fond du glissement, comme dans l'ancienne app ; les lecteurs d'écran disposent d'une action sémantique avec la même confirmation.

La saisie passe en plein écran. Le défilement du détail est mémorisé en dehors de la condition d'affichage du formulaire pour retrouver la note éditée. Les options composées affichent leur moyenne combinée avec le compteur total des deux sous-disciplines. Le changement de période du bandeau est disponible seulement depuis l'accueil pour préserver les brouillons et les opérations de fichiers.
