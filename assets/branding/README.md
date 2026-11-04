# SwissGrades · identité Carnet

Le motif vient du logo de la [fiche Google Play officielle](https://play.google.com/store/apps/details?id=me.asteroidus.swissgrades), consultée le 22 octobre 2026. Le fichier vectoriel original était déjà présent dans le dépôt sous `Icon.svg` : deux feuilles, une note de 6 et trois barres de progression.

L'adaptation conserve ce dessin, avec le vert Carnet `#254F40`, un vert sauge `#B7CCB6` et le papier `#F6F7F4`. Le nom de l'application reste SwissGrades.

## Sources et exports

- `../../Icon.svg` : source vectorielle du logo en couleur.
- `play-store-icon.png` : export carré de 512 × 512 px, sans coins arrondis imposés.
- `screenshots/` : captures réelles de la version Carnet sur émulateur Android, avec des données de démonstration. Elles remplacent les anciennes captures bleues de la fiche ; aucune interface n'a été retouchée dans les images.
- `../../app/src/main/res/drawable/ic_carnet_logo.xml` : logo vectoriel utilisé dans l'en-tête et l'accueil.
- `../../app/src/main/res/drawable/ic_carnet_foreground.xml` : premier plan de l'icône adaptative Android, centré dans la zone de sécurité.
- `../../app/src/main/res/drawable/ic_carnet_monochrome.xml` : variante monochrome à détails transparents pour les icônes thématiques Android. La feuille arrière est retirée pour conserver une silhouette lisible.

Les ressources Android et le PNG de la fiche sont régénérés avec `python3 scripts/generate-brand-assets.py`, depuis la racine du dépôt. Cet outil utilise Inkscape installé sur la machine ; il n'ajoute aucune dépendance à l'application.

Les captures sont issues des tests `CarnetInstrumentedTest` : `carnetStartsOnSubjectsAndTabsRemainReachable`, `swipeDeletesNotesAndSubjectsOnlyAfterConfirmation` et `targetAndDarkPaletteUseCarnetSurfaces`. Les originaux de contrôle restent dans `docs/qa/carnet-reference/`.

Ces fichiers sont préparés localement. Aucune modification de la fiche Google Play n'a été publiée.
