# Instructions de travail — kfokam48-epreuve-157

Épreuve finale fullstack KFOKAM48. Application de suivi de présence, dépôt d'exercices et relecture
entre pairs. **Le contexte complet du projet est dans `knowledge.md`** — le lire avant toute tâche.

**Langue du projet : le français.** Documentation, messages d'erreur, titres d'issues, libellés de
branches : tout est en français.

## Ce qui est noté, et donc non négociable

Le sujet note la **démarche** avant le produit : 38 pts d'analyse, 32 pts de Git, 10 pts de conduite
du changement, 15 pts de produit, 5 pts de journal. Le rendu visuel n'est pas noté.

1. **Historique Git = copie d'examen.** Commits atomiques, messages qui disent le *pourquoi*.
2. **Aucune conformité sacrifiée** : `api/contrat.yaml` et la clôture de session passent avant tout
   le reste. En cas de retard, on sacrifie les tickets Should/Could et le bonus diagramme.
3. **Une hypothèse silencieuse est une faute.** Toute décision qui tranche un point non couvert par
   les 16 `Qx` de `CLIENT.md` doit être écrite dans `docs/CAHIER_DES_CHARGES.md` §7.
4. **Aucun secret, aucun fichier généré** (`target/`, `node_modules/`, `dist/`) dans l'historique.
   Malus immédiat.

## Convention Git — à appliquer sans exception

**Une branche par ticket. Jamais de branche fourre-tout.**

| Étape | Règle |
|---|---|
| Nommage | `feat/<n°issue>-<slug-court>` pour une fonctionnalité, `fix/<n°issue>-<slug-court>` pour un correctif. Exemple : `feat/2-marquer-presence` |
| Création | Toujours depuis `main` à jour : `git switch main && git pull && git switch -c feat/<n°>-<slug>` |
| Contenu | **Un seul ticket par branche.** Une tâche technique isolée (outillage, migration, documentation) est un ticket comme un autre : sa propre branche, jamais empilée sur celle d'un autre ticket |
| Publication | Pousser la branche dès le premier commit, puis à chaque commit — jamais de travail qui ne vit que sur le disque |
| Intégration | Une PR par branche, vers `main`, liée à l'issue (`Closes #<n°>`) ; la PR ferme l'issue au merge |
| Après merge | Supprimer la branche, puis `git switch main && git pull` avant d'ouvrir la suivante |
| `main` | Reste toujours à jour et saine : jamais plus d'un ticket d'avance sur les branches ouvertes. Pas de `push --force` destructeur dessus |

**Cas particuliers :**

- Les trois commits **`[JALON]`** (`analyse`, `v0.1`, `v1.0`) se posent **directement sur `main`**,
  vides de code, et sont poussés immédiatement. Un jalon non poussé n'existe pas.
- **Correctif et évolution ne partagent jamais une branche ni un commit.** À l'étape 3, le bug
  signalé et le changement de besoin sont deux tickets distincts, avec leurs branches distinctes.
- Le nommage `chore/…` est réservé aux tickets d'outillage du projet, **avec un numéro d'issue** —
  `chore/socle-maven` ou `chore/base-de-donnees` ne sont pas des noms valides : ils regroupent
  plusieurs tickets.

## Rappels qui coûtent cher si on les oublie

- **Un test vert en local ne prouve rien sur la branche.** L'arbre de travail peut contenir des
  fichiers encore non commités : le code paraît alors terminé alors qu'il ne compile pas une fois
  mergé — c'est arrivé sur les tickets `#9` et `#7`, corrigé par `#31`. Avant tout merge, vérifier la
  branche telle qu'elle arrivera sur `main` : `git stash push -u`, puis compiler et tester ; ou
  compiler un clone propre de la branche.
- `.gitignore` (Java **et** JS) doit être déjà commité avant toute ligne de code.
- Au moins une issue doit être ouverte : le malus « aucune issue » est de −10.
- Le hash soumis dans `SOUMISSION.md` doit être sur **`main`**.
- `docs/CAHIER_DES_CHARGES.md` et `docs/diagrammes/` doivent être corrigés **après** l'étape 3 :
  l'enveloppe rend une partie de l'analyse fausse.
- `docs/JOURNAL.md` : une entrée par étape, écrite au moment où l'étape se termine, jamais à la fin.

## Conventions de code

Le module `session/` est le gabarit de référence : `domain/` → `application/` → `infrastructure/`,
dépendances dans ce sens uniquement, DTO systématiques, aucune entité JPA en JSON, exceptions héritant
d'`ApiException`. Détail complet dans `knowledge.md`.

## Pièges de l'environnement

- **Spring Boot 4.1.1, pas Boot 3.** `spring-boot-starter-webmvc` (et non `-web`), Jackson 3
  (`tools.jackson.databind.ObjectMapper`), tests Web MVC en
  `org.springframework.boot.webmvc.test.autoconfigure`. Ces trois chemins cassent une compilation
  si on les écrit « à la Boot 3 ».
- **PostgreSQL sur le port hôte 5433**, pas 5432.
- `./mvnw test` échoue tant que `presence/`, `exercice/` et `relecture/` ne sont pas implémentés :
  leurs repositories Spring Data ciblent des classes encore non annotées `@Entity`.
