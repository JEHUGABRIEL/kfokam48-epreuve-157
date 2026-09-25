# CHANGELOG — kfokam48-epreuve-157

Ce fichier décrit ce qui a été livré, dans l'ordre où l'historique Git le montre. Il n'est pas réécrit
après coup : chaque entrée correspond à des commits et des pull requests qui existent sur `main`.

Le projet suit six étapes imposées par le sujet : analyse, `v0.1`, enveloppe, `v1.0`, épreuve Git,
soumission.

---

## [Étape 1] Analyse et conception — commit `[JALON] analyse`

- `docs/CAHIER_DES_CHARGES.md` : les dix sections imposées, 9 exigences fonctionnelles (`EF1`–`EF9`),
  5 exigences non fonctionnelles (`ENF1`–`ENF5`), 14 règles de gestion (`RG1`–`RG14`), chacune sourcée
  sur une question `Qx` ou sur une hypothèse explicite de la section 7.
- Section 7 : le trou du sujet est nommé (la présence est-elle requise pour déposer un exercice ?), la
  contradiction `Q10` / `Q15` est tranchée en faveur de `Q15` et justifiée par la citation du client,
  et les hypothèses non couvertes par les 16 `Qx` sont écrites plutôt que subies.
- `docs/diagrammes/` : `D1` cas d'utilisation, `D2` modèle de données (aligné sur les migrations),
  `D3` séquence « marquer sa présence » avec ses deux chemins d'erreur, `D4` états-transitions d'un
  exercice (bonus).
- `api/contrat.yaml` complété : les 5 opérations imposées, plus les opérations nécessaires à la
  lecture (`GET /api/relectures/recues`, `.../assignees`, `/api/promotions`, `/api/etudiants`) et à la
  clôture de session, sans laquelle aucune règle dépendant de l'état « session clôturée » n'est
  implémentable.
- Backlog de 14 issues, priorisées Must / Should / Could, chacune avec un critère d'acceptation
  vérifiable et le renvoi à ses `EFx` / `RGx`.

## [Étape 2] Première version — commit `[JALON] v0.1`

- Socle : wrapper Maven commité (B1), Spring Boot 4.1.1 / Java 17, `compose.yaml` PostgreSQL 16 sur le
  port hôte 5433, profils dev (`validate`) et test (H2, Flyway désactivé) (B5).
- Schéma versionné : `V1__schema_initial.sql` (6 tables, index sur les clés étrangères, unicité
  `(session_id, etudiant_id)` portée par la base — ENF4) et `V2__donnees_de_demonstration.sql`
  (1 promotion, 60 étudiants, une session ouverte).
- `common/error/` : format d'erreur imposé `{ code, message }` centralisé dans un `@RestControllerAdvice`
  (B4), sans fuite de stack trace (ENF3).
- Architecture : chaque module en `domain/` (modèle pur + port) → `application/` (cas d'usage + DTO) →
  `infrastructure/` (contrôleur + persistance). Le modèle du domaine ne connaît ni JPA ni JSON.
- Opérations livrées : ouvrir une session (EF2, RG1), clôturer (EF8, RG13), marquer sa présence avec
  ses quatre refus dont le blocage RG8 en `429` (EF1, RG7, RG8), déposer un exercice (EF3, RG9),
  assigner le relecteur par tirage au sort (EF5, RG2, RG5, RG4), rendre une relecture verrouillée
  (EF6, RG3, RG12), lire sa note sans l'identité du relecteur (EF7, RG6), tableau récapitulatif
  (EF9, RG14, ENF2), remplacer le lien (EF4, RG10), et les listes d'identification (Q1).
- Frontend React + Vite + TypeScript : trois écrans (F2), une couche d'appels API unique (F3), états de
  chargement et d'erreur mutualisés, aucun calcul métier dupliqué (la moyenne vient de l'API).
- Tests : 37 tests, dont 20 unitaires qui tournent **sans contexte Spring** et un test d'intégration du
  contrôleur jusqu'à la base (B6). `./mvnw test` ne demande ni Docker ni PostgreSQL.

### Incidents de l'étape 2, et ce qu'ils ont changé

- **`main` a été cassée deux fois** par des classes référencées mais non commitées, puis par un import
  manquant. Chaque fois, la vérification locale passait parce que l'arbre de travail contenait le
  fichier absent du dépôt. Correctifs dédiés, et règle écrite dans `AGENTS.md` : vérifier la branche
  **telle qu'elle arrivera sur `main`**, pas telle qu'elle est sur le disque.
- **Un run de `./mvnw` enchaîné dans un pipe a masqué un échec de compilation** (le code de sortie
  retenu était celui de `grep`), ce qui a laissé passer le second incident. Les vérifications sont
  désormais lancées sans pipe et arrêtent la chaîne de commandes en cas d'échec.

## [Étape 3] Enveloppe

- **Non ouverte** : le script `enveloppe` n'a pas été remis au candidat pendant l'épreuve
  (cf. `docs/JOURNAL.md`). Le bug signalé et le changement de besoin ne sont donc pas traités, et
  l'analyse n'a pas eu à être corrigée en conséquence.

## [Étape 4] Version finale — commit `[JALON] v1.0`

- `README.md` réécrit pour décrire l'application **réellement livrée** : trois commandes vérifiées,
  données de démonstration, parcours de bout en bout pour les trois rôles, et une section qui annonce
  ce qui n'est pas livré plutôt que de le laisser découvrir.
- `CHANGELOG.md` (ce fichier) aligné sur l'historique Git.
- `docs/CAHIER_DES_CHARGES.md` version 1.5 : §7 complétée (codes de référence en 404, `409`
  `SESSION_DEJA_CLOTUREE`, note non entière) et §9 corrigé — la liste des livrables citait encore des
  fichiers `.puml` / `.mmd` antérieurs au renommage en `.md`.
- `SOUMISSION.md` complété : dépôt, hash du commit final sur `main`, commandes de démarrage.
- **Parcours complet vérifié sur PostgreSQL réel** (et non seulement en tests H2) : `docker compose up -d`
  puis `./mvnw spring-boot:run`, Flyway applique `V1` et `V2`, Hibernate valide le schéma, puis les
  quinze appels du contrat rendent exactement les statuts imposés — dont `409 DEJA_PRESENT`,
  `400 CODE_INCONNU`, `400 LIEN_INVALIDE`, `400 NOTE_INVALIDE`, `409 RELECTURE_DEJA_RENDUE`,
  `409 SESSION_DEJA_CLOTUREE` et `404 PROMOTION_INCONNUE`. Le relecteur tiré au sort est bien un autre
  étudiant que l'auteur, et la moyenne vaut `15.0` pour l'auteur relu contre `null` pour un étudiant
  sans note.
- Piège d'environnement documenté dans le README : si le port **8080** est occupé (Keycloak, autre
  serveur), l'API échoue avec `BindException`. `SERVER_PORT` et `VITE_API_TARGET` permettent de
  démarrer sans modifier le code.

## Reste à faire (assumé)

- Épreuve Git (étape 5) : `git-lab.bundle` n'a pas été remis au candidat ; le second dépôt
  `kfokam48-gitlab-157` n'a donc pas de contenu à recevoir.
- Étape 3 (enveloppe) : le script `enveloppe` n'a pas été remis non plus ; le bug signalé et le
  changement de besoin ne sont donc pas traités.

## Livré en fin de parcours

- **Ajout manuel d'une présence par le formateur** (`RG11`, `Q14`, ticket `#11`) :
  `POST /api/presences/formateur`, ajouté au contrat **après son gel** et documenté en §7 du cahier
  des charges. C'est la seule entorse assumée au principe « contrat figé avant la première ligne de
  code » : le champ `source` (`ETUDIANT` / `FORMATEUR`) existait depuis `V1` sans qu'aucune opération
  ne le renseigne, et RG11 était la dernière règle de gestion sans code pour l'exprimer.
