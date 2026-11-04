# Sécurité et restauration

Frontières : JSON stocké/importé, ZIP `.sgb`, XML PlusPoints, images, URI Android et destinations d'export. Tous les fichiers importés sont non fiables.

Exigences de mise en œuvre : limites de taille et de nombre d'entrées, validation du schéma/version, identifiants uniques, valeurs et énumérations valides, refus DTD/entités XML, chemins canonicalisés sous la racine contrôlée, rejet traversées et doublons d'archives. Limites numériques à choisir et documenter avant implémentation, pas de valeurs implicites.

Restauration : préparer et valider dans une zone temporaire, puis coordonner l'état et les pièces jointes avec récupération après interruption. Garder l'ancienne copie jusqu'à confirmation durable du nouvel état. Ne pas supprimer le rollback si la restauration échoue.

Constat préliminaire : le coordinateur 1.6.0 copie les entrées ZIP sans bornes et traite le remplacement des pièces jointes séparément de l'enregistrement de l'état. Le repository masque les erreurs de chargement derrière null. Ces chemins nécessitent tests négatifs et analyse de récupération avant réutilisation ; aucun incident utilisateur n'est affirmé.
