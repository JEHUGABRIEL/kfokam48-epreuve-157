import { useEffect, useState } from 'react';
import {
  api,
  ErreurApi,
  type Etudiant,
  type Promotion,
  type RelectureAssignee,
} from '../api/client';
import { Chargement, Erreur, Succes } from '../ui/Messages';

/**
 * Écran relecteur — EF6 (rendre une note et un commentaire).
 *
 * L'identifiant de la relecture se lit ici, et nulle part ailleurs : c'est le seul moyen d'atteindre
 * `POST /api/relectures/{id}`. L'auteur de l'exercice n'est jamais affiché (RG6).
 */
export function EcranRelecteur() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const [relecteurId, setRelecteurId] = useState<number | null>(null);
  const [assignees, setAssignees] = useState<RelectureAssignee[] | null>(null);
  const [notes, setNotes] = useState<Record<number, string>>({});
  const [commentaires, setCommentaires] = useState<Record<number, string>>({});
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  useEffect(() => {
    api
      .promotions()
      .then((liste) => {
        setPromotions(liste);
        if (liste.length > 0) {
          setPromotionId(liste[0].id);
        }
      })
      .catch((e: ErreurApi) => setErreur(e.message));
  }, []);

  useEffect(() => {
    if (promotionId === null) {
      return;
    }
    api
      .etudiants(promotionId)
      .then((liste) => {
        setEtudiants(liste);
        setRelecteurId(liste.length > 0 ? liste[0].id : null);
      })
      .catch((e: ErreurApi) => setErreur(e.message));
  }, [promotionId]);

  async function chargerAssignees() {
    if (relecteurId === null) {
      setErreur('Choisissez votre nom.');
      return;
    }
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      setAssignees(await api.relecturesAssignees(relecteurId));
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  async function rendre(relectureId: number) {
    const noteSaisie = Number(notes[relectureId]);
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      await api.rendreRelecture(relectureId, noteSaisie, commentaires[relectureId] ?? '');
      setSucces('Relecture rendue : elle est définitive (RG12).');
      if (relecteurId !== null) {
        setAssignees(await api.relecturesAssignees(relecteurId));
      }
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  return (
    <section>
      <h2>Relecteur</h2>
      <Erreur message={erreur} />
      <Succes message={succes} />

      <div className="carte">
        <h3>Mes relectures à rendre</h3>
        <label>
          Promotion
          <select
            value={promotionId ?? ''}
            onChange={(evenement) => setPromotionId(Number(evenement.target.value))}
          >
            {promotions.map((promotion) => (
              <option key={promotion.id} value={promotion.id}>
                {promotion.nom}
              </option>
            ))}
          </select>
        </label>
        <label>
          Mon nom
          <select
            value={relecteurId ?? ''}
            onChange={(evenement) => setRelecteurId(Number(evenement.target.value))}
          >
            {etudiants.map((etudiant) => (
              <option key={etudiant.id} value={etudiant.id}>
                {etudiant.nom}
              </option>
            ))}
          </select>
        </label>
        <button type="button" onClick={chargerAssignees} disabled={chargement}>
          Afficher mes relectures
        </button>
        {chargement && <Chargement texte="Chargement…" />}

        {assignees !== null && assignees.length === 0 && (
          <p className="discret">Aucune relecture en attente pour vous.</p>
        )}

        {assignees !== null &&
          assignees.map((relecture) => (
            <div className="carte interne" key={relecture.id}>
              <p>
                Relecture n° <strong>{relecture.id}</strong> — exercice {relecture.exerciceId}, session{' '}
                {relecture.sessionId}
              </p>
              <p>
                <a href={relecture.lien} target="_blank" rel="noreferrer">
                  Ouvrir le travail à relire
                </a>
              </p>
              <div className="ligne-champs">
                <label>
                  Note (0 à 20)
                  <input
                    type="number"
                    min={0}
                    max={20}
                    value={notes[relecture.id] ?? ''}
                    onChange={(evenement) =>
                      setNotes({ ...notes, [relecture.id]: evenement.target.value })
                    }
                  />
                </label>
                <label>
                  Commentaire
                  <input
                    value={commentaires[relecture.id] ?? ''}
                    onChange={(evenement) =>
                      setCommentaires({ ...commentaires, [relecture.id]: evenement.target.value })
                    }
                  />
                </label>
              </div>
              <button type="button" onClick={() => rendre(relecture.id)} disabled={chargement}>
                Rendre la relecture
              </button>
            </div>
          ))}
      </div>
    </section>
  );
}
