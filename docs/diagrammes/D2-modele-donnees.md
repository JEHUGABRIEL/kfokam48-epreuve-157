# D2 — Modèle de données

Les entités, leurs attributs et leurs cardinalités. **Doit correspondre aux migrations** : c'est le
cas de `V1__schema_initial.sql` et de `V3__deux_relecteurs_par_exercice.sql` — table par table,
colonne par colonne, contrainte par contrainte.

```mermaid
classDiagram
    class Promotion {
        +Long id
        +String nom
    }

    class Etudiant {
        +Long id
        +String nom
        +Long promotionId
    }

    class Session {
        +Long id
        +String titre
        +Long promotionId
        +String code
        +Instant ouvertureAt
        +Instant expirationAt
        +Instant clotureAt
        +StatutSession statut
    }

    class Presence {
        +Long id
        +Long sessionId
        +Long etudiantId
        +Instant horodatage
        +SourcePresence source
    }

    class Exercice {
        +Long id
        +Long sessionId
        +Long etudiantId
        +String lien
        +StatutExercice statut
        +Instant deposeAt
    }

    class Relecture {
        +Long id
        +Long exerciceId
        +Long relecteurId
        +Integer note
        +String commentaire
        +StatutRelecture statut
        +Instant assigneeAt
        +Instant renduAt
    }

    class StatutSession {
        <<enumeration>>
        OUVERTE
        CLOTUREE
    }

    class SourcePresence {
        <<enumeration>>
        ETUDIANT
        FORMATEUR
    }

    class StatutExercice {
        <<enumeration>>
        DEPOSE
        EN_ATTENTE_RELECTURE
        RELU
    }

    class StatutRelecture {
        <<enumeration>>
        ASSIGNEE
        RENDUE
    }

    Promotion "1" --> "*" Etudiant : compte
    Promotion "1" --> "*" Session : concerne
    Session "1" --> "*" Presence : enregistre
    Etudiant "1" --> "*" Presence : marque
    Session "1" --> "*" Exercice : reçoit
    Etudiant "1" --> "0..1" Exercice : dépose (par session)
    Exercice "1" --> "1..2" Relecture : est relu par
    Etudiant "1" --> "*" Relecture : relit (comme relecteur)
```

**Notes de lecture**

- Il n'existe **pas d'entité `Relecteur`** : un relecteur est un `Etudiant` référencé par
  `Relecture.relecteurId` (§2, RG5).
- L'unicité `(sessionId, etudiantId)` de `Presence` (ENF4) et l'unicité
  `(exerciceId, relecteurId)` de `Relecture` (RG4, **révisée à l'étape 3**) sont portées par la base,
  pas seulement par le service.
- **Jusqu'à deux relectures par exercice** depuis le changement de besoin de l'étape 3 : deux pairs
  distincts, jamais l'auteur, distincts entre eux. La cardinalité s'écrit `1..2` et non `2` parce
  qu'une session où un seul étudiant est éligible doit quand même produire une relecture — sa note
  restera provisoire (§7). Le modèle ne fait qu'autoriser la seconde ligne : c'est `RG4` révisée qui
  dit d'en tirer deux.
- RG2 (pas d'auto-relecture) ne s'exprime pas par une contrainte entre deux tables : elle reste
  contrôlée dans `RelectureService`.
