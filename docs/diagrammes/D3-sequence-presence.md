# D3 — Séquence : marquer sa présence

Cas nominal **et** les trois chemins d'erreur, alignés sur les codes HTTP du contrat (`api/contrat.yaml`).

```mermaid
sequenceDiagram
    participant E as Étudiant
    participant F as Front
    participant API as PresenceController
    participant S as PresenceService
    participant DB as Base de données

    E->>F: saisit le code
    F->>API: POST /api/presences { code, etudiantId }
    API->>S: enregistrer(code, etudiantId)
    S->>DB: rechercher session par code

    alt code inconnu
        DB-->>S: aucune session trouvée
        S-->>API: CodeInconnuException
        API-->>F: 400 { code: "CODE_INCONNU" }
        F-->>E: affiche l'erreur, invite à ressaisir
    else code expiré (RG1)
        DB-->>S: session trouvée, expirationAt dépassé
        S-->>API: CodeExpireException
        API-->>F: 410 { code: "CODE_EXPIRE" }
        F-->>E: affiche l'erreur, code périmé
    else déjà présent (RG7)
        DB-->>S: présence existante (sessionId, etudiantId)
        S-->>API: DejaPresentException
        API-->>F: 409 { code: "DEJA_PRESENT" }
        F-->>E: affiche l'erreur, présence déjà enregistrée
    else cas nominal
        DB-->>S: session valide, aucune présence existante
        S->>DB: créer Presence(sessionId, etudiantId, source=ETUDIANT)
        DB-->>S: Presence créée
        S-->>API: Presence
        API-->>F: 201 { id, sessionId, etudiantId, source }
        F-->>E: confirmation de présence
    end
```

**Notes de lecture**

- Le code expiré est refusé en **410** et le déjà-présent en **409** : ce sont les codes du contrat,
  pas un choix d'implémentation.
- Un quatrième refus existe, hors chemin nominal : après 5 échecs de code, le même étudiant est
  bloqué 2 minutes et reçoit **429 `TROP_DE_TENTATIVES`** (RG8, extension documentée en §7).
- Une session clôturée refuse toute nouvelle présence (RG13) : **409 `SESSION_DEJA_CLOTUREE`**.
