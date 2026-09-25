# Project knowledge

## What this is

Épreuve finale fullstack **KFOKAM48** (auteur : Binga Jehu Gabriel · 157). Une application de suivi
de présence en session de cours, avec dépôt d'exercice, assignation automatique d'un relecteur et
restitution d'un tableau de bord formateur.

Le cœur du sujet n'est pas l'UI mais la solidité des règles métier (code qui expire, pas
d'auto-relecture, note verrouillée une fois rendue, clôture de session irréversible).

**Langue du projet : français** — docs, messages d'erreur, titres d'issues et libellés.

> Dernière vérification de ce fichier : **2026-09-25**, sur `main`.
> `knowledge.md` **et** `AGENTS.md` font autorité — les relire avant toute tâche.

## Démarrage rapide

```bash
docker compose up -d                      # racine : PostgreSQL 16 (port hôte 5433)
cd backend && ./mvnw spring-boot:run      # API sur :8080, migrations Flyway au démarrage
cd backend && ./mvnw test                 # 58 tests, sans Docker ni PostgreSQL (H2 en mémoire)
```

```bash
cd frontend && npm install && npm run dev   # frontend React sur :5173, proxy /api vers :8080
cd frontend && npm run build                # vérification de types + build de production
```

## État actuel du dépôt

Les jalons Git sont : `[JALON] depart` → `[JALON] analyse` → `[JALON] v0.1` → `[JALON] v1.0`.
**Les quatre sont poussés et dans l'ordre**, `[JALON] analyse` avant le premier commit de code.

**Situation Git au 2026-09-25.** `main` est à jour et saine, les quatre jalons sont poussés, et
**les 36 issues du backlog sont fermées**. Le travail passe par des branches de ticket : une issue, une
branche, une PR vers `main` fermée par `Closes #<n°>`, un merge commit, puis la branche supprimée. Les
derniers tickets de la journée donnent la mesure du rythme :

| Ticket | Branche | PR | Contenu |
|---|---|---|---|
| `#64` | `fix/64-listes-deroulantes` | `#65` | une liste déroulante annonce son état au lieu de rester vide |
| `#66` | `feat/66-barre-laterale-roles` | `#67` | barre latérale qui porte le choix du rôle |
| `#70` | `feat/70-pagination` | `#71` | `page` / `taille` optionnels sur les quatre lectures |
| `#72` | `feat/72-choix-role-accueil` | `#73` | accueil de choix du rôle, puis barre latérale des fonctionnalités |
| `#74` | `chore/74-relecture-documents` | *(cette PR)* | relecture des documents contre `main` |

Deux dettes d'historique subsistent, **antérieures à la régularisation des conventions** et non réparables sans
réécrire `main` (interdit) : la PR `#16` (base de données) a été fermée sans merge alors que ses
commits sont bien dans `main`, et l'issue `#14` est fermée sans PR qui la référence. Pour ces deux
tickets, le découpage n'est donc pas lisible dans l'historique. Note utile : le numéro `#15` est
celui de la PR du socle Maven, pas une issue — issues et PR partagent la numérotation GitHub.

| Chemin | Contenu |
|---|---|
| `AGENTS.md` | **Instructions de travail pour l'agent** : ce qui est noté, convention Git obligatoire, rappels coûteux, pièges d'environnement. À lire avant d'agir |
| `docs/CAHIER_DES_CHARGES.md` | **Document de référence** : acteurs, périmètre, EF/ENF, règles de gestion RG1–RG14, hypothèses et contradictions |
| `docs/JOURNAL.md` | Journal de bord, **une entrée par étape**. Étapes 1, 2 et 4 rédigées ; 3 et 5 écrites comme **non déroulées** (scripts non remis) ; 6 reste à écrire au téléversement. Format : Fait / Bloqué / IA + « comment j'ai vérifié » |
| `docs/diagrammes/D1-cas-utilisation.md` | Cas d'utilisation (Mermaid) |
| `docs/diagrammes/D2-modele-donnees.md` | Modèle de données (Mermaid) — source de vérité des entités |
| `docs/diagrammes/D3-sequence-presence.md` | Séquence de marquage de présence, chemins d'erreur inclus |
| `docs/diagrammes/D4-etats-transitions-exercice.md` | Cycle de vie d'un exercice (bonus) |
| `SOUMISSION.md` | Dossier de soumission : candidat, dépôts, hash, checklist de téléversement |
| `api/contrat.yaml` | Contrat OpenAPI 3.0.3 — **imposé** pour 5 opérations, extensible pour le reste |
| `creer-issues.sh` | Script `gh` de création des labels et des **14 issues initiales** du backlog (9 Must, 3 Should, 2 Could) ; la suite du backlog a été ouverte ticket par ticket |
| `compose.yaml` | PostgreSQL 16 de développement (base `epreuve`, port hôte 5433) |
| `backend/` | Maven Spring Boot 4.1.1. Packages sous `src/main/java/com/kfokam48/epreuve/` : `common/`, `session/`, `presence/`, `exercice/`, `relecture/`, `tableau/` |
| `frontend/` | React 18 + Vite + TypeScript. `src/api/client.ts` est le **seul** module qui appelle l'API (F3) ; `src/ecrans/` porte l'écran d'accueil de choix du rôle et les trois écrans ; `src/ui/` porte la barre latérale, les listes déroulantes, la pagination et le catalogue des rôles |

**Tous les modules sont implémentés** : `common/referentiel/`, `session/`, `presence/`, `exercice/`,
`relecture/` et `tableau/`, plus le transverse `common/error/` et `common/pagination/`. Chacun en
`domain/` (modèle pur + port) → `application/` (cas d'usage + DTO) → `infrastructure/` (contrôleur +
persistance). `./mvnw test` passe en entier, contexte Spring compris : **58 tests, 0 échec**, dans
14 classes. Les cinq opérations imposées du contrat sont livrées, plus six ajoutées (clôture de
session, remplacement du lien, listes d'identification, lectures de relecture, ajout manuel d'une
présence).

**Tout est commité et poussé** : `git status` est propre sur `main`, et `main` = `origin/main`.

## Stack et commandes

**Backend** — Java 17 (cible), **Spring Boot 4.1.1**, Maven, wrapper `mvnw` commité (type
`only-script`, Maven 3.9.16, pas de `maven-wrapper.jar`), PostgreSQL, Flyway.

```bash
# À la racine du dépôt
docker compose up -d              # démarre PostgreSQL 16 (port hôte 5433)
docker compose down               # arrête (les données restent)
docker compose down -v            # arrête et efface les données (utile si une migration rate)

cd backend
./mvnw spring-boot:run            # dev — applique les migrations Flyway au démarrage
./mvnw test                       # tests unitaires + intégration (H2, sans Docker)
./mvnw test -Dtest=SessionTest    # un seul test
./mvnw clean package              # build
./mvnw dependency:tree            # vérifier une dépendance transitive
```

Rien à signaler : `./mvnw test` passe en entier (58 tests), sur H2 et **sans Docker** — un correcteur
peut donc valider le projet sur un poste vierge.

Il n'y a **pas encore de linter ni de formateur** configuré côté backend (pas de checkstyle, pas de
spotless). Ne pas en inventer un sans le commiter.

Dépendances déclarées : `spring-boot-starter-webmvc`, `-data-jpa`, `-validation`, `-flyway`,
`flyway-database-postgresql`, `postgresql` (runtime), `lombok` (optionnel), `h2` (test),
`-test`, `-webmvc-test` (test).

**Frontend** — React 18 + Vite 5 + TypeScript. Un écran d'accueil qui fait choisir le rôle, une barre
latérale qui liste les fonctionnalités du rôle choisi, et trois écrans (formateur, étudiant, relecteur).
Chaque liste de plus d'une page est découpée par un contrôle unique (`src/ui/Pagination.tsx`). Pas de
routeur, pas de gestionnaire d'état : le rôle et la fonctionnalité affichée sont deux `useState` dans
`App.tsx`, et un rechargement ramène à l'accueil (ENF5 : rien dans le stockage du navigateur). Les
commandes sont :

```bash
cd frontend
npm install
npm run dev
npm run build
```

## Architecture cible

- **Backend en clean architecture par module**, code transverse dans `common/`. Un modèle de domaine
  pur, un port de persistance, puis en infrastructure l'adaptateur, l'entité JPA et le mapper. Pas de
  sur-découpage pour autant : pas d'interface par cas d'usage, pas de port/in-port/out.
- **Front** : couche d'appel API dédiée (`src/api/client.ts`, seul module qui appelle l'API), écran
d'accueil de choix du rôle, barre latérale de fonctionnalités, écrans formateur / étudiant / relecteur.
- **Modèle de données** (cf. D2) : `Promotion`, `Etudiant`, `Session`, `Presence`, `Exercice`,
  `Relecture`. Il n'existe **pas** d'entité `Relecteur` : c'est un `Etudiant` référencé par
  `Relecture.relecteurId`.

### Module de référence : `session/`

`session/` est le **gabarit** : les six autres modules suivent la même découpe, au point qu'un nouveau
module se rédige en copiant ses fichiers.

- **`domain/model/`** : le modèle métier, **sans aucune annotation ni dépendance de persistance**
  (`Session`, `StatutSession`). Lombok pour l'accès aux champs, règles métier en méthodes
  (`estExpiree()`, `estCloturee()`). Conséquence directe : ces règles se testent sans base, sans
  contexte Spring et sans mapping.
- **`domain/`** : le **port** de persistance (`SessionRepository`), exprimé dans le langage du domaine
  (`enregistrer`, `trouverParId`, `trouverParCode`) et n'étendant **pas** `JpaRepository`. Les
  exceptions métier y vivent aussi, héritant d'`ApiException`.
- **`application/`** : `@Service`, injection par constructeur explicite (pas de `@Autowired` sur
  champ), `@Transactional` sur les cas d'usage, constantes métier en `private static final`.
- **`application/dto/`** : `record` Java 17 ; les requêtes portent les contraintes
  `jakarta.validation` (`@NotBlank`, `@NotNull`).
- **`infrastructure/`** : `@RestController` + `@RequestMapping`, retourne
  `ResponseEntity<Dto>` — jamais une entité (B3).
- **`infrastructure/persistence/`** : l'entité JPA (`SessionEntity`, colonnes `snake_case` explicites,
  `@Enumerated(EnumType.STRING)`), le dépôt Spring Data (`SessionJpaRepository`), le mapper
  (`SessionMapper`, écrit champ par champ — un oubli se voit) et l'adaptateur
  (`SessionRepositoryAdapter`) qui implémente le port. Seul l'adaptateur connaît Spring Data.
- **Exceptions** : `extends ApiException` et appel `super(code, HttpStatus.X, "message français")`.
  `ApiException` est dans `common/error/` et porte `code` + `statut`.

### Répartition des responsabilités entre modules (tranché)

- **`RelectureService` est le seul à écrire une `Relecture`** : assignation au dépôt (RG2/RG4/RG5)
  et rendu de note (RG12). Signature retenue :
  `assigner(Long exerciceId, Long sessionId, Long auteurId)`.
- `ExerciceService` se contente de l'appeler juste après le dépôt, **dans la même transaction**
  (propagation REQUIRED) : un tirage qui échoue doit annuler le dépôt, pas laisser un exercice
  sans relecteur.
- Paramètres en `Long` et non l'entité `Exercice` : évite une dépendance circulaire entre les deux
  modules (`relecture` n'importe rien de `exercice`).
- `tableau/` n'a pas de `domain/` : il lit les repositories des autres modules, en lecture seule.
  La moyenne des notes se calcule **uniquement** là (F3 : aucun calcul métier dupliqué côté front).

### Spécificités Spring Boot 4 (pièges de migration)

Le projet est en Boot 4, pas en Boot 3 : plusieurs chemins de packages et noms de starters ont
changé. Les erreurs « cannot find symbol » viennent presque toujours de là.

- `spring-boot-starter-web` est **déprécié** au profit de `spring-boot-starter-webmvc`.
- JSON passe par **Jackson 3** : `tools.jackson.databind.ObjectMapper`
  (et non `com.fasterxml.jackson.databind.ObjectMapper`). Les annotations restent en
  `com.fasterxml.jackson.annotation`.
- Tests Web MVC : `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
  (et non `...boot.test.autoconfigure.web.servlet...`), fourni par
  `spring-boot-starter-webmvc-test`.
- `spring-boot-starter-flyway` existe et apporte `flyway-core` ; il faut toujours ajouter
  `flyway-database-postgresql` en plus.

### Base de données

**`compose.yaml` (racine)** — PostgreSQL 16, base `epreuve`, `postgres`/`postgres`, volume nommé
`epreuve-pgdata`. Le port hôte est **5433**, pas 5432 : un PostgreSQL local occupe déjà 5432 sur la
machine de développement. Surchargeable avec `DB_PORT=5432 docker compose up -d`.

**`backend/src/main/resources/application.yml`** — lit `DB_URL`, `DB_USER`, `DB_PASSWORD` (défauts
`jdbc:postgresql://localhost:5433/epreuve`, `postgres`, `postgres`). `ddl-auto: validate` (B5 : le
schéma appartient à Flyway), `open-in-view: false`, `server.error.include-*: never` (ENF3).

**`backend/src/test/resources/application-test.yml`** — profil `test` : H2 en mémoire en
`MODE=PostgreSQL`, **Flyway désactivé**, `ddl-auto: create-drop`. `./mvnw test` ne dépend donc ni de
Docker ni de PostgreSQL. Corollaire : le jeu de démonstration V2 n'existe pas dans les tests, chaque
test crée ses données.

**Migrations** (`src/main/resources/db/migration/`) :

- `V1__schema_initial.sql` — les 6 tables de D2, en `snake_case` conforme au mapping JPA, plus
  l'index sur chaque clé étrangère.
- `V2__donnees_de_demonstration.sql` — 1 promotion (donc **id 1**), 60 étudiants (ENF2) et une
  session ouverte de code `DEMO24` valable 15 min. `POST /api/sessions` avec `promotionId=1`
  fonctionne donc dès le premier démarrage.

Règles poussées dans la base plutôt que seulement dans les services : `uq_presence_session_etudiant`
(ENF4), `uq_relecture_exercice_relecteur` (RG4 révisée : deux pairs distincts par exercice),
`ck_relecture_note` (RG3), `ck_relecture_rendue` (RG12). RG2 (pas d'auto-relecture) ne s'exprime pas en
SQL : elle reste dans le service, comme la lecture-puis-écriture de la présence.

Les trois migrations (`V1`, `V2`, `V3`) ont été exécutées contre un PostgreSQL 16.13 réel, la dernière
sur une base **déjà remplie**, et Hibernate a validé son mapping dessus. En cas de modification d'une
migration déjà appliquée, Flyway refusera le checksum : `docker compose down -v` puis relance.

### Ce qui manque encore, ou n'a jamais eu lieu

- **Étape 5 (`git-lab.bundle`)** : l'épreuve Git n'a pas pu être traitée, le bundle ne m'ayant pas été
  remis. Le dépôt `kfokam48-gitlab-157` n'a jamais été créé. C'est écrit dans `docs/JOURNAL.md`
  (entrée de l'étape 5) plutôt que passé sous silence.
- **Étape 3 (`enveloppe`)** : remise **tardivement**, après l'heure limite. Elle a été traitée — bug
  (`#78`), changement de besoin (`#81`–`#83`), analyse corrigée en conséquence — mais dans l'ordre
  inverse de l'épreuve, le code existant déjà. Le ticket `#79` est sacrifié, et c'est écrit.
- **Aucun test automatisé sur le frontend** : les écrans ont été vérifiés en les construisant
  (`npm run build`), en pilotant Chrome sans interface sur le parcours réel, et au `curl` sur l'API.
  C'est une faiblesse reconnue, pas une omission cachée.
- **Aucune authentification** : voir « Pièges et points non évidents ». Ni jeton, ni route réservée,
  ni contrôle côté serveur — l'usurpation reste possible et assumée (ENF5).
- `AGENTS.md` et ce fichier font autorité et se relisent avant toute tâche. `.agents/` ne contient que
  des types TypeScript de l'outil et est ignoré par Git.

## Contraintes imposées par le sujet (B1–B6, F1–F3)

- **B2 — Contrat strict** : `api/contrat.yaml` doit être respecté à la lettre (chemins, verbes, codes
  de statut, format d'erreur) pour les 5 opérations imposées. Toute opération ajoutée va dans le
  même fichier, sous `/api`, **avant** le premier commit de code.
- **B3** : séparation contrôleur / service / repository, **aucune entité JPA exposée en JSON** —
  DTO systématiques.
- **B4** : validation des entrées + `@RestControllerAdvice` centralisé ; jamais de stack trace ni de
  page d'erreur Spring par défaut au client.
- **B5** : schéma versionné par **Flyway**, migrations commitées ; `ddl-auto=update` interdit hors
  tests.
- **B6** : au moins un test unitaire sur une règle métier réelle (ex. RG12) et un test d'intégration
  sur un endpoint.
- **F1–F3** : framework front justifié dans le README, build qui passe, trois écrans, aucun calcul
  métier dupliqué côté front (la moyenne vient de l'API).

## Erreurs — format imposé, sans exception

Toute erreur renvoie exactement `{ code, message }` : `code` en MAJUSCULES (identifiant stable),
`message` en français, lisible par un humain. Aucun corps vide, aucune stack trace.

Codes attendus : `CODE_INCONNU` (400), `CODE_EXPIRE` (410), `DEJA_PRESENT` (409),
`SESSION_INCONNUE` (404), `SESSION_DEJA_CLOTUREE` (409), `LIEN_INVALIDE` (400),
`EXERCICE_DEJA_DEPOSE` (409), `NOTE_INVALIDE` (400), `AUTO_RELECTURE` (403),
`RELECTURE_DEJA_RENDUE` (409), `PROMOTION_INCONNUE` (404), `TROP_DE_TENTATIVES` (429).

Deux codes ne viennent d'aucune `Qx` et sont documentés en section 7 du cahier des charges :
`REQUETE_INVALIDE` (400, toute erreur de forme : champ manquant, JSON illisible, paramètre absent ou
mal typé) et `ERREUR_INTERNE` (500, filet de sécurité — le détail est journalisé, jamais renvoyé).
Les deux vivent dans `common/error/GlobalExceptionHandler.java`.

`TROP_DE_TENTATIVES` (429) est le **seul statut ajouté à une opération imposée** : RG8 (Q4,
5 échecs de code ⇒ 2 min de blocage) n'était observable autrement ni par le client ni par un
test. Décision tranchée et consignée dans `docs/CAHIER_DES_CHARGES.md` §7, avec le compteur
associé (en mémoire applicative, indexé par `etudiantId` seul). Les trois statuts imposés de
`POST /api/presences` (400/409/410) sont inchangés.

## Pièges et points non évidents

- **`POST /api/sessions/{id}/cloture` n'est pas dans le contrat imposé** mais est indispensable :
  RG13 et plusieurs autres règles en dépendent (voir cahier des charges §7).
- **La présence n'est pas une condition du dépôt d'exercice** : un étudiant absent peut déposer.
  Compromis assumé.
- **Le relecteur est tiré au sort au moment du dépôt**, parmi les présences déjà enregistrées à cet
  instant. Une présence ajoutée après coup par le formateur n'entre pas dans le pool — limite connue.
- **Aucun étudiant présent éligible** ⇒ l'exercice reste « en attente d'assignation » ; aucune
  exception n'est prévue.
- **Une relecture est définitive dès qu'elle est rendue** (RG12) : Q15 a été retenue contre Q10, qui
  autorisait une correction avant clôture. Même le formateur ne peut pas la modifier.
- **Unicité `(sessionId, etudiantId)` sur `Presence`** doit être garantie au niveau base (ENF4), pas
  seulement dans le service : deux étudiants marquent présence au même instant.
- **Pas d'authentification réelle** (ENF5, hors périmètre) : l'`etudiantId` est transmis par le client
  dans les requêtes qui le demandent, sans vérification serveur, et le rôle se choisit sur l'écran
  d'accueil sans rien prouver. Il n'y a ni jeton, ni route réservée côté front, et **rien n'est écrit
  dans le stockage du navigateur** : une promotion ou un nom ne survit pas à un rechargement.
  L'usurpation est un risque assumé — ne pas le présenter comme corrigé.
- **Pagination optionnelle, jamais par défaut** (ticket `#70`) : `page` et `taille` sur les quatre
  lectures, mais un appel qui ne les demande pas rend la **collection entière** — c'est la contrainte
  qui tient tout le reste. `GET /api/tableau` est une opération imposée dont la réponse est un tableau
  JSON : donc pas d'enveloppe `{elements, total}`, le total part dans l'en-tête `X-Total-Count`.
  `PageDemandee.depuis(null, null)` rend `Optional.empty()` ; ne jamais compléter un défaut pour un
  appel qui n'a rien demandé. Hors bornes (`page < 1`, `taille` hors de 1 à 100) : `400
  REQUETE_INVALIDE`, aucun code de statut nouveau.
- **L'identité du relecteur ne doit jamais sortir de l'API** (RG6) : ni `relecteurId`, ni nom, dans
  la réponse à l'étudiant relu.
- **Blocage anti-brute-force** (RG8) : 5 échecs de code par étudiant ⇒ 2 minutes de blocage.
- Seuils et bornes : code valable **15 min** (RG1), note entière **0–20** (RG3), **deux relecteurs
  distincts par exercice** et note retenue = moyenne des deux, provisoire tant qu'un pair n'a pas rendu
  (RG4, révisée à l'étape 3).
- **Configuration locale** : un seul `.env` à la racine (modèle `.env.example`, jamais commité le vrai),
  lu par Spring Boot (`spring.config.import: optional:file:../.env[.properties]`, depuis `backend/`),
  Vite (`loadEnv`) et Docker Compose. Aucune variable exportée nécessaire.
- **`SessionControllerTest` déclare `package ...session.application`** alors qu'il teste
  `SessionController` (infrastructure) : le fichier vit donc sous
  `src/test/java/.../session/application/`. À déplacer si tu préfères la symétrie des packages.
- Front mobile-first pour le marquage de présence : viewport 375 px sans défilement horizontal
  (ENF1) ; `GET /api/tableau` doit répondre en < 2 s pour 60 étudiants (ENF2).

## Livrables et conventions de travail

- Livrables attendus : README d'installation testé depuis un clone vierge, `CHANGELOG.md` cohérent
  avec l'historique Git, `JOURNAL.md` mis à jour à chaque étape.
- **Quatre commits `[JALON]`**, tous poussés et dans l'ordre : `depart`, `analyse`, `v0.1`, `v1.0`.
- **Une branche par ticket, jamais de branche fourre-tout** : `feat/<n°issue>-<slug>` (ou
  `fix/<n°issue>-<slug>`), créée depuis `main` à jour. Une PR par branche, vers `main`, liée à
  l'issue par `Closes #<n°>`. Branche supprimée après merge, puis `main` remis à jour. Un ticket =
  une branche, y compris pour une tâche d'outillage ou de documentation. Les noms `chore/<sujet>`
  sans numéro d'issue sont proscrits : ils regroupent plusieurs tickets. Seuls les commits
  `[JALON]` font exception et vont directement sur `main`. Pousser après chaque ticket, jamais en
  fin de journée. Règle complète dans `AGENTS.md` et `docs/CAHIER_DES_CHARGES.md` §10.
- Commits séparés entre correctif et évolution. Mettre à jour le cahier des charges et les diagrammes
  au fil de l'eau, pas à la fin.

### Definition of Done d'un ticket

1. Code conforme à B3 (couches séparées, DTO, pas d'entité exposée).
2. Critères d'acceptation de l'issue vérifiés manuellement ou par test.
3. Contrat d'API respecté à la lettre pour l'endpoint concerné.
4. PR liée à l'issue et celle-ci fermée par le merge.
5. `JOURNAL.md` mis à jour.

## Ordre de priorité en cas de retard

Sacrifier d'abord les tickets Should/Could et le bonus diagramme, **jamais** la conformité au contrat
d'API ni la clôture de session — ce sont les points qui pèsent le plus au barème.
