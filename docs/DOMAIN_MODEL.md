# Modèle métier et sources

## Sources

- Code de référence : tag v1.6.0, commit `be8a8c4`.
- Règlement RSN 411.110, état au 1er janvier 2026, consulté le 21 octobre 2026 : https://rsn.ne.ch/DATA/program/books/rsne/pdf/411.110.pdf, articles 7 à 13.

## Faits établis

Le règlement définit le panier avec langue 1, langue 2, mathématiques et option spécifique. Le seuil est 16. Les déficits sous 4 comptent double dans la compensation, quatre insuffisances au plus sont admises, et aucune moyenne ne doit être inférieure à 3. L'éducation physique est exclue. La conférence prend la décision de promotion ; l'application produit une projection.

La note annuelle se fonde sur les résultats de toute l'année ; une note est aussi donnée au premier semestre. Les options composées sont pondérées selon la part d'enseignement de chaque discipline. Les pratiques précises d'arrondi et les parts d'enseignement ne sont pas déterminées par cette seule lecture.

## Contrat 1.6.0 à préserver sauf correction documentée

- Années 1, 2, 3 ; branches partagées entre les semestres d'une année.
- Notes de 1 à 6 par quarts ; poids 1, 0,5 ou 0,25.
- S1 : notes S1 ; vue actuelle S2 : notes S1 + S2. Proposer le libellé « Année complète » pour la vue cumulée, tout en conservant S2 comme période de saisie.
- Moyennes arrondies au demi-point ; cibles au demi-point.
- Deux sous-disciplines par option composée ; comportement historique à caractériser avant changement.
- Brouillons et simulations temporaires ; aucune note fictive ajoutée aux données.

## Modèle proposé

Une matière possède son identité stable, sa configuration et ses évaluations ; une évaluation possède valeur, coefficient, période, description, horodatage et références de pièces jointes. Les moyennes, impacts et bilans sont dérivés.

Distinguer rôle scolaire et nom affiché. Une matière renommée ne change pas de rôle. Ne pas déduire langue 2 de « allemand », de sa position ou de la traduction.

Séparer statut de configuration, complétude et conditions satisfaites/non satisfaites. Des résultats absents ne valent jamais zéro. Une projection incomplète doit garder visibles les conditions déjà non satisfaites.

## Correction intégrée au premier lot Carnet

`PromotionEvaluator.evaluate` calcule `promotionPointsTotal`, mais ne l'ajoute pas aux motifs bloquants. Exemple : panier [4, 4, 4, 4], autre branche 3,5. Panier 16, une insuffisance, aucune note sous 3, mais compensation −1. Le code 1.6.0 pouvait produire PROMOTED ; l'article 11 ne permet pas une projection positive sur ces données.

Le moteur ajoute désormais `NegativeCompensation` aux motifs bloquants quand le total est inférieur à zéro. Les tests couvrent le panier suffisant avec compensation −1 et le seuil exact de zéro. Les notes sources sont préservées, les résultats dérivés sont recalculés. Les règles composées restent inchangées dans ce lot.
