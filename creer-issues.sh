#!/usr/bin/env bash
set -euo pipefail

# À lancer depuis la racine du dépôt cloné (kfokam48-epreuve-157),
# après `gh auth login`. Vérifie d'abord : gh repo view

REPO="JEHUGABRIEL/kfokam48-epreuve-157"

echo "Création des labels de priorité..."
gh label create must --color "d73a4a" --description "Priorité Must" --repo "$REPO" --force
gh label create should --color "fbca04" --description "Priorité Should" --repo "$REPO" --force
gh label create could --color "0e8a16" --description "Priorité Could" --repo "$REPO" --force

echo "Création des issues Must..."

gh issue create --repo "$REPO" --label must \
  --title "Formateur : ouvrir une session et obtenir un code" \
  --body "Critère : POST /api/sessions avec titre + promotionId → 201 avec id, code, ouvertureAt, expirationAt.
Réf : EF2, RG1"

gh issue create --repo "$REPO" --label must \
  --title "Étudiant : marquer sa présence" \
  --body "Critère : POST /api/presences avec code valide et non expiré → 201. Code inconnu → 400. Expiré → 410. Déjà présent → 409.
Réf : EF1, RG1, RG7, RG8"

gh issue create --repo "$REPO" --label must \
  --title "Étudiant : déposer le lien de son exercice" \
  --body "Critère : POST /api/exercices → 201 avec statut. Lien invalide → 400. Déjà déposé → 409.
Réf : EF3, RG9"

gh issue create --repo "$REPO" --label must \
  --title "Système : assigner automatiquement un relecteur au dépôt" \
  --body "Critère : à la création d'un exercice, un relecteur est tiré parmi les présents à la session, jamais l'auteur.
Réf : EF5, RG2, RG4, RG5"

gh issue create --repo "$REPO" --label must \
  --title "Relecteur : rendre une relecture" \
  --body "Critère : POST /api/relectures/{id} avec note (0–20) et commentaire → 200. Note invalide → 400. Auto-relecture → 403. Déjà rendue → 409.
Réf : EF6, RG2, RG3, RG12"

gh issue create --repo "$REPO" --label must \
  --title "Étudiant relu : consulter sa note sans l'identité du relecteur" \
  --body "Critère : la relecture rendue expose note + commentaire, jamais relecteurId ni nom.
Réf : EF7, RG6"

gh issue create --repo "$REPO" --label must \
  --title "Formateur : clôturer une session" \
  --body "Critère : POST /api/sessions/{id}/cloture → toute présence/dépôt/relecture ultérieure sur cette session est refusée.
Réf : EF8, RG13"

gh issue create --repo "$REPO" --label must \
  --title "Formateur : consulter le tableau récapitulatif" \
  --body "Critère : GET /api/tableau?promotionId= → 200 avec presences, exercicesDeposes, moyenne, relecturesEnAttente par étudiant. Promotion inconnue → 404.
Réf : EF9, RG14"

gh issue create --repo "$REPO" --label must \
  --title "Gestion centralisée des erreurs au format imposé" \
  --body "Critère : toute erreur (quel que soit l'endpoint) renvoie exactement { code, message }, jamais de stack trace ni page Spring par défaut.
Réf : B4, contrat Annexe B"

echo "Création des issues Should..."

gh issue create --repo "$REPO" --label should \
  --title "Étudiant : remplacer le lien de son exercice" \
  --body "Critère : tant qu'aucune relecture n'a démarré, un nouveau POST/PUT remplace le lien existant.
Réf : EF4, RG10"

gh issue create --repo "$REPO" --label should \
  --title "Formateur : ajouter une présence manuellement" \
  --body "Critère : la présence créée par le formateur porte source=FORMATEUR, visible distinctement dans le tableau.
Réf : RG11 (Q14)"

gh issue create --repo "$REPO" --label should \
  --title "Blocage temporaire après 5 échecs de code" \
  --body "Critère : après 5 tentatives échouées par le même étudiant, les tentatives suivantes sont refusées pendant 2 minutes.
Réf : RG8 (Q4)"

echo "Création des issues Could..."

gh issue create --repo "$REPO" --label could \
  --title "Diagramme bonus états-transitions du cycle de vie d'un exercice" \
  --body "Critère : diagramme Mermaid/PlantUML déposé/en attente/relu, versionné dans docs/diagrammes/.
Réf : bonus +3 pts"

gh issue create --repo "$REPO" --label could \
  --title "Données de démonstration au démarrage" \
  --body "Critère : au lancement, une promotion, des étudiants et une session de test sont chargés automatiquement.
Réf : contrainte « Démarrage » du sujet"

echo "Terminé : 14 issues créées (9 must, 3 should, 2 could)."
