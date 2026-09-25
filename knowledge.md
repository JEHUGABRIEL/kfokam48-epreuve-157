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
cd backend && ./mvnw test                 # SessionTest passe, SessionControllerTest échoue (cf. plus bas)
```

`frontend/` est vide : aucune commande npm n'existe encore.

## État actuel du dépôt

Les jalons Git sont : `[JALON] depart` → `[JALON] analyse` (fait) → `v0.1` → `v1.0`.

**Situation Git au 2026-09-25.** `main` est à jour et saine. Le travail d'outillage et de
documentation est passé par des branches de ticket : une issue, une branche, une PR, un merge
commit, puis la branche supprimée.

| Ticket | Branche | PR | Contenu |
|---|---|---|---|
| `#17` | `chore/17-convention-git` | `#21` | `AGENTS.md`, convention Git (cahier des charges §10), `creer-issues.sh`, `.gitignore` (`.agents/`) |
| `#19` | `chore/19-journal-etapes-1-2` | `#22` | `docs/JOURNAL.md` (étapes 1 et 2) |
| `#20` | `chore/20-corrections-analyse-contrat` | `#23` | statut `429` au contrat, section 7 complétée, révisions `1.2` |
| `#18` | `chore/18-contexte-projet` | *(cette PR)* | ce fichier |

Deux dettes d'historique subsistent, **antérieures à cette régularisation** et non réparables sans
réécrire `main` (interdit) : la PR `#16` (base de données) a été fermée sans merge alors que ses
commits sont bien dans `main`, et l'issue `#14` est fermée sans PR qui la référence. Pour ces deux
tickets, le découpage n'est donc pas lisible dans l'historique. Note utile : le numéro `#15` est
celui de la PR du socle Maven, pas une issue — issues et PR partagent la numérotation GitHub.

**Toujours non commité : tout le code Java** (`backend/src/main/java/`, `backend/src/test/java/`),
`session/` compris, ainsi que `frontend/` (vide). Voir « Ce qui manque encore » : le prochain
travail Git consiste à ouvrir une branche par ticket fonctionnel et à y déposer ce code.

| Chemin | Contenu |
|---|---|
| `AGENTS.md` | **Instructions de travail pour l'agent** : ce qui est noté, convention Git obligatoire, rappels coûteux, pièges d'environnement. À lire avant d'agir |
| `docs/CAHIER_DES_CHARGES.md` | **Document de référence** : acteurs, périmètre, EF/ENF, règles de gestion RG1–RG14, hypothèses et contradictions |
| `docs/JOURNAL.md` | Journal de bord, **une entrée par étape** (étapes 1 et 2 rédigées, 3 à 6 vides). Format imposé : Fait / Bloqué / IA + « comment j'ai vérifié » |
| `docs/diagrammes/D1-cas-utilisation.puml` | Cas d'utilisation (PlantUML) |
| `docs/diagrammes/D2-modele-donnees.mmd` | Modèle de données (Mermaid) — source de vérité des entités |
| `docs/diagrammes/D3-sequence-presence.mmd` | Séquence de marquage de présence, chemins d'erreur inclus |
| `api/contrat.yaml` | Contrat OpenAPI 3.0.3 — **imposé** pour 5 opérations, extensible pour le reste |
| `creer-issues.sh` | Script `gh` de création des labels et des 14 issues du backlog (9 Must, 3 Should, 2 Could) |
| `compose.yaml` | PostgreSQL 16 de développement (base `epreuve`, port hôte 5433) |
| `backend/` | Maven Spring Boot 4.1.1. Packages sous `src/main/java/com/kfokam48/epreuve/` : `common/`, `session/`, `presence/`, `exercice/`, `relecture/`, `tableau/` |
| `frontend/` | **Vide** — aucun `package.json` pour l'instant |

**Seul `session/` est implémenté.** Les autres modules existent sous forme de fichiers-squelettes
documentés (classes vides + Javadoc décrivant le comportement attendu) : ils ne compilent pas de
règle métier.

**Tout le code Java est encore non commité** : `git status` montre `?? backend/src/main/java/` et
`?? backend/src/test/java/` en bloc — `session/` compris. Ne pas conclure que `session/` est
déjà dans Git ; `git diff` ne montre rien pour ces fichiers, c'est normal.

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

`./mvnw test` **échoue aujourd'hui** (2 erreurs sur 4, reproduit le 2026-09-25) :
`SessionControllerTest` ne charge pas le contexte Spring — cf. « Ce qui manque encore ».
`SessionTest` (règle métier pure) passe.

Il n'y a **pas encore de linter ni de formateur** configuré côté backend (pas de checkstyle, pas de
spotless). Ne pas en inventer un sans le commiter.

Dépendances déclarées : `spring-boot-starter-webmvc`, `-data-jpa`, `-validation`, `-flyway`,
`flyway-database-postgresql`, `postgresql` (runtime), `lombok` (optionnel), `h2` (test),
`-test`, `-webmvc-test` (test).

**Frontend** — React + Vite + TypeScript, trois écrans (formateur, étudiant, relecteur). Rien n'est
encore installé ; les commandes prévues seront :

```bash
cd frontend
npm install
npm run dev
npm run build
```

## Architecture cible

- **Backend en clean architecture par module** (pas de sur-découpage port/in-port/out), code
  transverse dans `common/`. Découpage contrôleur / service / repository obligatoire.
- **Front** : couche d'appel API dédiée, écrans formateur / étudiant / relecteur.
- **Modèle de données** (cf. D2) : `Promotion`, `Etudiant`, `Session`, `Presence`, `Exercice`,
  `Relecture`. Il n'existe **pas** d'entité `Relecteur` : c'est un `Etudiant` référencé par
  `Relecture.relecteurId`.

### Module de référence : `session/`

`session/` est le seul module implémenté et sert de gabarit à répliquer.

- **`domain/`** : entité JPA annotée Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`), colonnes
  `snake_case` explicites, `@Enumerated(EnumType.STRING)`, règles métier en méthodes (`estExpiree()`).
  `*Repository extends JpaRepository<Entité, Long>`, requêtes dérivées (`findByCode`).
- **`application/`** : `@Service`, injection par constructeur explicite (pas de `@Autowired` sur
  champ), `@Transactional` sur les cas d'usage, constantes métier en `private static final`.
- **`application/dto/`** : `record` Java 17 ; les requêtes portent les contraintes
  `jakarta.validation` (`@NotBlank`, `@NotNull`).
- **`infrastructure/`** : `@RestController` + `@RequestMapping`, retourne
  `ResponseEntity<Dto>` — jamais une entité (B3).
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
(ENF4), `uq_relecture_exercice` (RG4), `ck_relecture_note` (RG3), `ck_relecture_rendue` (RG12).
RG2 (pas d'auto-relecture) ne s'exprime pas en SQL : elle reste dans le service.

Les deux migrations ont été exécutées contre un PostgreSQL 16.13 réel et Hibernate a validé son
mapping dessus. En cas de modification d'une migration déjà appliquée, Flyway refusera le checksum :
`docker compose down -v` puis relance.

### Ce qui manque encore

- **Le contexte Spring ne démarre pas.** `PresenceRepository`, `ExerciceRepository` et
  `RelectureRepository` étendent `JpaRepository` sur des classes encore vides et non annotées
  `@Entity`. Le premier bean en échec est `relectureRepository` :
  `Not a managed type: class ...relecture.domain.Relecture`. Conséquence actuelle : `./mvnw compile`
  et `SessionTest` passent, `SessionControllerTest` échoue au chargement du contexte (2 erreurs sur
  4). Cela se résout dès que les modules presence/exercice/relecture sont implémentés (ou en rendant
  leurs repositories inertes en attendant).
- `frontend/` est vide alors que F1 à F3 exigent trois écrans.
- Livrables non écrits : `README.md` (installation testée depuis un clone vierge), `CHANGELOG.md`
  et `SOUMISSION.md` — aucun des trois n'existe à la racine. `docs/JOURNAL.md` existe et est à
  compléter au fil des étapes.
- `AGENTS.md` est désormais écrit (il a remplacé le gabarit vide) et fait autorité avec ce fichier.
  `.agents/` ne contient que des types TypeScript de l'outil et est ignoré par Git.

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
- **Pas d'authentification réelle** (ENF5, hors périmètre) : `etudiantId` transite côté client
  (localStorage) sans vérification serveur ; la route `/formateur` n'est protégée que par un code
  statique côté front. L'usurpation est un risque assumé — ne pas le présenter comme corrigé.
- **L'identité du relecteur ne doit jamais sortir de l'API** (RG6) : ni `relecteurId`, ni nom, dans
  la réponse à l'étudiant relu.
- **Blocage anti-brute-force** (RG8) : 5 échecs de code par étudiant ⇒ 2 minutes de blocage.
- Seuils et bornes : code valable **15 min** (RG1), note entière **0–20** (RG3), un seul relecteur
  par exercice (RG4).
- **`SessionControllerTest` déclare `package ...session.application`** alors qu'il teste
  `SessionController` (infrastructure) : le fichier vit donc sous
  `src/test/java/.../session/application/`. À déplacer si tu préfères la symétrie des packages.
- Front mobile-first pour le marquage de présence : viewport 375 px sans défilement horizontal
  (ENF1) ; `GET /api/tableau` doit répondre en < 2 s pour 60 étudiants (ENF2).

## Livrables et conventions de travail

- Livrables attendus : README d'installation testé depuis un clone vierge, `CHANGELOG.md` cohérent
  avec l'historique Git, `JOURNAL.md` mis à jour à chaque étape.
- **Trois commits `[JALON]`** dans l'ordre : `analyse` (fait), `v0.1`, `v1.0`.
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
