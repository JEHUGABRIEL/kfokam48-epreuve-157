# CHANGELOG — kfokam48-epreuve-157

Ce journal suit l'ordre réel de l'historique Git, jalons compris. Les numéros entre parenthèses sont
ceux des issues du dépôt.

## [JALON] v0.1 — première version

### Ajouté

- **Socle technique** : wrapper Maven 3.9.16 (`only-script`, `mvnw` commité), `pom.xml` Spring Boot
  4.1.1 sur Java 17, `compose.yaml` PostgreSQL 16 sur le port hôte 5433. (branche `chore/socle-maven`)
- **Base de données versionnée** : `V1__schema_initial.sql` (les 6 tables du modèle, index sur chaque
  clé étrangère, contraintes d'unicité et de cohérence en base) puis
  `V2__donnees_de_demonstration.sql` (1 promotion, 60 étudiants, une session ouverte `DEMO24`).
  Profils dev (PostgreSQL, `ddl-auto: validate`) et test (H2 en mémoire, Flyway désactivé).
  (branche `chore/base-de-donnees`)
- **Erreurs au format imposé** (#9) : `GlobalExceptionHandler` rend `{ code, message }` pour toute
  erreur — erreurs métier, erreurs de forme, pannes internes — sans jamais laisser fuir de trace
  technique (B4, ENF3).
- **Ouvrir une session** (#1) : `POST /api/sessions` remet un code à alphabet non ambigu et fixe la
  fenêtre de validité de 15 minutes (EF2, RG1).
- **Clôturer une session** (#7) : `POST /api/sessions/{id}/cloture` verrouille la session ; toute
  seconde clôture est refusée (EF8, RG13).
- **Convention de travail et outillage** (#17, #19, #18, #20) : `AGENTS.md` fixe la convention Git,
  `knowledge.md` porte le contexte technique, `docs/JOURNAL.md` est ouvert, et le statut `429
  TROP_DE_TENTATIVES` est ajouté au contrat pour rendre RG8 observable.
- **Modèle du domaine séparé de l'entité JPA** (#33) : `domain/model/` ne connaît plus la persistance,
  le dépôt du domaine devient un port, l'entité, le mapper et l'adaptateur vivent en infrastructure.

### Corrigé

- **`main` ne compilait pas** (#31) : `ApiException` et `ErreurDto`, dont dépend tout le format
  d'erreur, n'avaient jamais été commités. La règle de vérification manquante est désormais écrite
  dans `AGENTS.md` — vérifier la branche telle qu'elle arrivera sur `main`, pas l'arbre de travail.
- **Diagrammes invisibles sur GitHub** (#13) : les `.mmd` et `.puml` sont convertis en Markdown
  contenant un bloc Mermaid, et un quatrième diagramme d'états-transitions est ajouté (bonus).

## [JALON] analyse — analyse et conception

- Cahier des charges complet (10 sections, 9 `EF`, 5 `ENF`, 14 `RG` sourcées, section 7 des zones
  d'ombre et contradictions), trois diagrammes versionnés en texte, backlog de 14 issues priorisées,
  contrat d'API complété. Aucune ligne de code avant ce jalon.
