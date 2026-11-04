# Décisions et points ouverts

## Réglés

Android ; lycées neuchâtelois ; continuité avec 1.6.0 et anciennes sauvegardes ; liberté visuelle ; priorité ergonomie/UI.

## Hypothèses explicites

- Maturité gymnasiale, périmètre de l'application actuelle. ECG et maturité professionnelle hors scope. À corriger si le propriétaire entend autre chose par lycée.
- Fonctionnement local sans services additionnels, selon la parité demandée.
- Reprendre Android minimum 24 et français/anglais.
- La vue cumulée sera appelée « Année complète », et la période de saisie restera explicitement S1 ou S2.

## À résoudre avant le code concerné

1. Parts d'enseignement des options PYAM/BICH/économie-droit selon année/lycée et pratique d'arrondi : rechercher les directives, puis question ciblée si les sources ne suffisent pas. Ne bloque pas la maquette ; bloque un changement de moteur composé.
2. Correspondance des rôles du panier sur les données héritées : définir le cas ambigu et la confirmation minimale. Ne jamais classer par nom traduit.
3. Distribution et continuité de signature : vérifier pipeline et APK de la 1.6.0 avant la répétition de mise à jour, sans demander de secrets dans la conversation.
4. RÉSOLU : Carnet choisi ; navigation Matières / Bilan / Réglages et développement autorisés. Plan complet : `CARNET_COMPONENT_PLAN.md`.

## État du gate

Lot UI initial autorisé, sur les sources 1.6.0 sans changement du format de stockage. Règles composées et stratégie détaillée de restauration explicitement différées au code concerné ; aucun changement de ces règles ni du format pendant le lot UI. Le schéma, l'identité d'application et les chemins existants sont conservés. La mise à jour signée depuis l'ancienne APK reste une validation de livraison.
