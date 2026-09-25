# Soumission — Épreuve finale fullstack KFOKAM48

> À téléverser sur la plateforme **avant 18h00**. Sans ce dépôt, rien n'est rendu.
> Les deux lignes marquées **à compléter** sont les seules que l'agent ne pouvait pas remplir :
> le matricule exact et le centre d'examen.

---

## Candidat

| | |
|---|---|
| Nom et prénom(s) | Binga Jehu Gabriel |
| Matricule | KF48-___-157 — **à compléter** (suffixe imposé au dépôt : `157`) |
| Centre | Yaoundé / Douala / Bafoussam — **à compléter** |
| Compte GitHub | JEHUGABRIEL |

## Projet

| | |
|---|---|
| Dépôt (public) | `https://github.com/JEHUGABRIEL/kfokam48-epreuve-157` |
| Commit final — hash complet, 40 caractères | `491913c6f9822e332b9f2330269d77c9e3364293` |
| Branche | `main` |

Le hash déclaré est celui du commit de merge qui précède immédiatement l'écriture de ces lignes. Il
contient l'intégralité du travail : les six modules backend, le frontend, l'analyse corrigée, le journal,
les deux tickets livrés après le premier relevé — le correctif des listes déroulantes et la barre
latérale — et la version de ce fichier qui décrit l'état exact du livrable. **Aucun changement n'est
postérieur à l'inscription de ce hash-ci** — un fichier ne peut pas contenir le hash du commit qui le
porte.

## Épreuve Git — étape 5

| | |
|---|---|
| Dépôt (public) | `https://github.com/JEHUGABRIEL/kfokam48-gitlab-157` |
| Commit final — hash complet | **non applicable** : `git-lab.bundle` ne m'a pas été remis |

Le dépôt `kfokam48-gitlab-157` n'a **pas été créé** et l'épreuve Git n'a pas été traitée : le bundle
`git-lab.bundle` fait partie des deux éléments « remis pendant l'épreuve » que je n'ai jamais reçus, avec
l'`enveloppe` de l'étape 3. C'est écrit dans `docs/JOURNAL.md` plutôt que passé sous silence.

## Technique

| | |
|---|---|
| Frontend utilisé | React 18 + Vite + TypeScript |
| Base de données | PostgreSQL 16, schéma versionné par Flyway (`V1`, `V2`), `ddl-auto: validate` |
| Commandes de démarrage | `docker compose up -d` · `cd backend && ./mvnw spring-boot:run` · `cd frontend && npm install && npm run dev` |

---

## Ce que j'ai livré

**Ce qui fonctionne** — les **cinq opérations imposées** du contrat, avec leurs codes de statut exacts :

- `POST /api/sessions` → `201` avec un code valable 15 minutes (EF2, RG1) ;
- `POST /api/presences` → `201`, `400 CODE_INCONNU`, `409 DEJA_PRESENT`, `410 CODE_EXPIRE`, plus `429`
  `TROP_DE_TENTATIVES` (EF1, RG7, RG8, extension documentée en §7) ;
- `POST /api/exercices` → `201`, `400 LIEN_INVALIDE`, `409 EXERCICE_DEJA_DEPOSE` (EF3, RG9) ;
- `POST /api/relectures/{id}` → `200`, `400 NOTE_INVALIDE`, `403 AUTO_RELECTURE`, `409
  RELECTURE_DEJA_RENDUE` (EF6, RG3, RG12) ;
- `GET /api/tableau?promotionId=` → `200`, `404 PROMOTION_INCONNUE` (EF9, RG14, ENF2).

Plus l'assignation automatique du relecteur par tirage au sort parmi les présents, jamais l'auteur
(EF5, RG2, RG5, RG4) ; la clôture de session qui verrouille toute écriture (EF8, RG13) ; la note reçue
sans l'identité du relecteur (EF7, RG6) ; le remplacement du lien (EF4, RG10) ; l'ajout manuel d'une
présence par le formateur, marqué `source = FORMATEUR` (RG11, Q14) ; et les listes d'identification
(Q1). Le tout est vérifié sur PostgreSQL réel, pas seulement en test : les quinze appels du parcours
complet rendent exactement les statuts imposés.

**Frontend** : une barre latérale qui porte le choix du rôle et commande tout l'écran ; trois écrans
(formateur, étudiant, relecteur) ; une couche d'appels API unique ; un seul composant de liste
déroulante, qui annonce toujours son état plutôt que de rester vide ; chargements et erreurs gérés au
même endroit ; aucun calcul métier dupliqué (F3).

**Ce qui ne fonctionne pas / n'est pas livré, et pourquoi :**

- Aucune authentification (Q1, ENF5) : identification par liste, sans vérification serveur.
- Aucun test automatisé sur le frontend : les trois écrans ont été vérifiés en les construisant
  (`npm run build`) et en exerçant l'API au `curl`, pas par des tests d'interface.
- L'étape 3 (enveloppe) n'a pas eu lieu : le script ne m'a pas été remis, donc le bug signalé et le
  changement de besoin ne sont pas traités, et l'analyse n'a pas eu à être corrigée en conséquence.
- L'étape 5 (épreuve Git) n'a pas eu lieu : bundle non remis.
- Pas d'export du tableau, pas de notification.

---

## Avant de téléverser, vérifie

- [x] Le dépôt est **public** et s'ouvre en navigation privée (vérifié : `visibility: PUBLIC`)
- [x] Le hash fait bien **40 caractères** et existe sur GitHub (`491913c6f9822e332b9f2330269d77c9e3364293`)
- [x] Tout le travail est **poussé** — `git status` propre, `main` = `origin/main`
- [x] Le `README` a été suivi à la lettre depuis le dépôt (parcours complet rejoué au `curl`)
- [x] `JOURNAL.md` et le cahier des charges sont dans `docs/`
- [x] Les **quatre** commits `[JALON]` sont poussés et dans l'ordre (`depart`, `analyse`, `v0.1`, `v1.0`)
- [ ] **Remplir le matricule et le centre ci-dessus**
- [ ] **Téléverser ce fichier sur la plateforme avant 18h00**

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans restriction et je
les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs réponses. Mes dépôts resteront
publics et inchangés jusqu'à la publication des résultats.

Signature : ______________________  Date : __________
