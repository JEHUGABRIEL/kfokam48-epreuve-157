# kfokam48-epreuve-157 — Suivi de présence, dépôt d'exercices et relecture entre pairs

Application de suivi de présence en session de cours. Le formateur ouvre une session et obtient un
code de présence ; l'étudiant marque sa présence, dépose le lien de son exercice ; un relecteur est
assigné automatiquement parmi les présents ; le formateur consulte un tableau récapitulatif.

**Frontend choisi : React (Vite, TypeScript)** — un seul build statique, trois écrans qui partagent
la même couche d'appels API, et un outillage déjà maîtrisé, donc du temps investi dans le modèle
métier plutôt que dans la configuration.

Le cœur du sujet n'est pas l'interface mais les règles métier : code qui expire, pas d'auto-relecture,
note verrouillée une fois rendue, clôture de session irréversible (cf. `docs/CAHIER_DES_CHARGES.md`).

---

## Démarrage

**Prérequis** : Docker, JDK 17 ou plus (testé sur 21), et le wrapper Maven fourni — rien d'autre à
installer.

```bash
# 1. La base de données de développement (PostgreSQL 16, port hôte 5433)
docker compose up -d

# 2. L'API — les migrations Flyway s'appliquent au démarrage
cd backend && ./mvnw spring-boot:run

# 3. Le frontend (Vite)
cd frontend && npm install && npm run dev
```

L'API écoute sur `http://localhost:8080`, le frontend sur `http://localhost:5173`.

> Le port hôte de PostgreSQL est **5433**, pas 5432 : un PostgreSQL local occupe souvent 5432.
> Pour en changer : `DB_PORT=5432 docker compose up -d`, en surchargeant alors `DB_URL`.

### Données de démonstration

Elles sont chargées automatiquement par la migration `V2__donnees_de_demonstration.sql` : une
promotion (identifiant **1**), 60 étudiants, et une session ouverte dont le code est `DEMO24`
(valable 15 minutes à partir du dernier démarrage des migrations).

Pour vérifier sans rien installer d'autre :

```bash
curl -X POST http://localhost:8080/api/sessions \
     -H 'Content-Type: application/json' \
     -d '{"titre":"Session démo","promotionId":1}'
# → 201 {"id":2,"code":"K7QM3P","ouvertureAt":"...","expirationAt":"..."}
```

### Vérifier l'installation

```bash
cd backend && ./mvnw test
```

Les tests tournent sur H2 en mémoire, avec Flyway désactivé : **`./mvnw test` ne demande ni Docker
ni PostgreSQL**. C'est ce qui permet à un correcteur de valider le projet sur un poste vierge.

---

## Ce qui fonctionne, ce qui ne fonctionne pas

**Fonctionne** — l'API, avec ses règles métier et ses codes d'erreur :

| Opération | Rôle |
|---|---|
| `POST /api/sessions` | ouvrir une session et obtenir un code (EF2, RG1) |
| `POST /api/sessions/{id}/cloture` | clôturer une session — hors contrat imposé, cf. §7 (EF8, RG13) |
| `POST /api/presences` | marquer sa présence (EF1, RG7, RG8) |
| `POST /api/exercices` | déposer le lien de son exercice (EF3, RG9) |
| `PUT /api/exercices/{id}` | remplacer ce lien (EF4, RG10) |
| `POST /api/relectures/{id}` | rendre note et commentaire (EF6, RG12) |
| `GET /api/relectures/assignees` · `GET /api/relectures/recues` | ce qu'un relecteur doit rendre, ce qu'un relu a reçu (EF7, RG6) |
| `GET /api/tableau?promotionId=` | tableau récapitulatif du formateur (EF9, RG14) |
| `GET /api/promotions` · `GET /api/etudiants?promotionId=` | listes d'identification (Q1) |

Le contrat complet, y compris les opérations ajoutées et leurs codes d'erreur, est dans
`api/contrat.yaml` — c'est la référence, pas ce README.

**Ne fonctionne pas / n'est pas livré** — annoncé plutôt que découvert par le correcteur :

- **Le frontend n'est pas livré** : `frontend/` est vide. Les trois écrans (formateur, étudiant,
  relecteur) restent à faire (F2, F3). L'API est utilisable au `curl` et documentée dans
  `api/contrat.yaml`, mais aucune interface ne l'accompagne.
- `README` et `CHANGELOG` ont été écrits à la fin : ils sont exacts, mais l'historique Git ne les
  montre pas au fil de l'eau.
- Les 14 relectures de la campagne de démonstration ne sont pas pré-remplies : la base de
  démonstration fournit promotion, étudiants et session, pas d'exercices déjà déposés.

---

## Tests

| Test | Ce qu'il prouve |
|---|---|
| `SessionTest` | RG1 et RG13 : la fenêtre de validité et l'état de clôture, **sans base ni contexte Spring** |
| `SessionMapperTest` | l'aller-retour modèle ↔ entité ne perd aucun champ |
| `SessionRepositoryAdapterTest` | la persistance tient à travers le **port**, pas à travers l'implémentation |
| `SessionControllerTest` | le endpoint imposé, du HTTP jusqu'à la base, y compris le format d'erreur |
| `GlobalExceptionHandlerTest` | B4 : `{ code, message }` pour toute erreur, sans fuite technique (ENF3) |

Le format unitaire / intégration demandé par B6 est respecté : les règles métier se testent seules,
et un test part du contrôleur pour aller jusqu'à la base.

---

## Architecture

```
backend/src/main/java/com/kfokam48/epreuve/
├── common/error/          erreurs centralisées, format { code, message } imposé (B4)
├── common/referentiel/    Promotion, Étudiant — données partagées
├── session/               ouvrir / clôturer une session
├── presence/              marquer sa présence
├── exercice/              déposer, remplacer le lien
├── relecture/             assigner (RG5), rendre une note (RG12)
└── tableau/               lecture agrégée pour le formateur
```

Chaque module suit la même découpe, et c'est le gabarit à répliquer :

- `domain/model/` — le modèle métier, **sans aucune annotation de persistance** ; les règles y vivent
  en méthodes (`estExpiree()`, `estCloturee()`) et se testent sans base ;
- `domain/` — le **port** de persistance, qui n'étend pas `JpaRepository` ;
- `application/` — les cas d'usage, avec leurs DTO ;
- `infrastructure/` — le contrôleur, et `infrastructure/persistence/` avec l'entité JPA, le dépôt
  Spring Data, le mapper et l'adaptateur.

Conséquence directe de ce découpage : **le port ne rend que des modèles**, donc renvoyer une entité
JPA en JSON (interdit par B3) est impossible par construction, pas seulement déconseillé.

Le schéma est versionné par Flyway (`V1__schema_initial.sql`, `V2__donnees_de_demonstration.sql`) ;
`ddl-auto` vaut `validate` hors tests, et `create-drop` dans le profil `test`.

---

## Documentation

| Fichier | Contenu |
|---|---|
| `docs/CAHIER_DES_CHARGES.md` | le besoin, les exigences `EF`/`ENF`, les règles `RG`, et la section 7 des zones d'ombre |
| `docs/diagrammes/D1` à `D4` | cas d'utilisation, modèle de données, séquence de présence, états-transitions d'un exercice |
| `docs/JOURNAL.md` | le journal de bord, une entrée par étape |
| `api/contrat.yaml` | le contrat d'API, les 5 opérations imposées plus les ajouts |
| `knowledge.md` | le contexte technique du dépôt : conventions, pièges, état réel |
