-- V3 : deux relecteurs par exercice.
--
-- Changement de besoin de l'étape 3 (enveloppe) : « Finalement, un seul relecteur ça ne marche pas ».
-- Q6 tombe, un exercice est relu par deux pairs distincts, et la note retenue est la moyenne des deux
-- (cahier des charges §7).
--
-- Ce que cette migration fait, et surtout ce qu'elle ne fait pas :
--   - elle est AJOUTÉE, jamais substituée : V1 et V2 gardent leurs octets et leurs sommes de
--     contrôle. Réécrire une migration déjà appliquée ferait échouer Flyway sur toute base migrée ;
--   - elle ne supprime aucune ligne, n'en modifie aucune, ne retire aucune colonne. Une relecture
--     déjà assignée reste assignée ; elle sera simplement seule sur son exercice, qui restera sans
--     second relecteur — limite assumée et écrite en §7 ;
--   - elle remplace une contrainte par une autre, en une seule transaction : entre les deux
--     instructions, la table n'est jamais sans garde-fou.
--
-- L'ancienne contrainte interdisait plus d'une relecture par exercice. La nouvelle interdit plus
-- d'une relecture du même relecteur pour un même exercice : c'est exactement la règle qui reste vraie
-- quand on passe de un à deux pairs — deux personnes différentes, jamais deux fois la même.
ALTER TABLE relecture DROP CONSTRAINT uq_relecture_exercice;

ALTER TABLE relecture
    ADD CONSTRAINT uq_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);
