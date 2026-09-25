-- V2 : jeu de données de démonstration.
--
-- Objectif : le correcteur ne part jamais d'une base vide.
--   - une promotion
--   - 60 étudiants (ENF2 : le tableau doit répondre en moins de 2 s pour 60 étudiants)
--   - une session ouverte, utilisable immédiatement avec son code
--
-- Les identifiants sont laissés aux colonnes d'identité : la promotion prend l'id 1,
-- donc POST /api/sessions avec promotionId=1 fonctionne dès le premier démarrage.

INSERT INTO promotion (nom) VALUES ('KFOKAM48 — Promotion 2026');

INSERT INTO etudiant (nom, promotion_id)
SELECT 'Étudiant ' || lpad(i::text, 2, '0'), 1
FROM generate_series(1, 60) AS i;

-- Session de test ouverte maintenant, avec le même délai de validité que RG1 (15 min).
-- Code volontairement lisible et hors alphabet aléatoire, pour être annoncé en salle.
INSERT INTO session (titre, promotion_id, code, ouverture_at, expiration_at, statut)
VALUES (
    'Séance de démonstration',
    1,
    'DEMO24',
    now(),
    now() + interval '15 minutes',
    'OUVERTE'
);
