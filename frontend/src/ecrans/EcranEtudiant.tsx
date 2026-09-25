import { useEffect, useState } from 'react';
import { api, ErreurApi, type Etudiant, type Promotion, type RelectureRecue } from '../api/client';
import { Chargement, Erreur, Succes } from '../ui/Messages';

/**
 * Écran étudiant — EF1 (marquer sa présence avec le code), EF3 (déposer un exercice), EF7 (consulter
 * sa note sans l'identité du relecteur).
 *
 * Q1 : l'étudiant s'identifie en se choisissant dans une liste ; il n'y a pas de mot de passe. Le
 * choix est conservé en mémoire du composant, pas dans le stockage du navigateur : rien ne justifie de
 * laisser un identifiant traîner sur un téléphone partagé.
 */
export function EcranEtudiant() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const [etudiantId, setEtudiantId] = useState<number | null>(null);

  const [code, setCode] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [lien, setLien] = useState('');
  const [note, setNote] = useState<RelectureRecue | null>(null);

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
        setEtudiantId(liste.length > 0 ? liste[0].id : null);
      })
      .catch((e: ErreurApi) => setErreur(e.message));
  }, [promotionId]);

  async function executer(action: () => Promise<string>) {
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      setSucces(await action());
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  function marquerPresence() {
    if (etudiantId === null) {
      setErreur('Choisissez votre nom.');
      return;
    }
    void executer(async () => {
      const presence = await api.marquerPresence(code, etudiantId);
      return `Présence enregistrée (source ${presence.source}) pour la session ${presence.sessionId}.`;
    });
  }

  function deposer() {
    if (etudiantId === null || sessionId.trim() === '') {
      setErreur('Choisissez votre nom et indiquez la session.');
      return;
    }
    void executer(async () => {
      const exercice = await api.deposerExercice(Number(sessionId), etudiantId, lien);
      return `Exercice déposé (statut ${exercice.statut}).`;
    });
  }

  function consulterNote() {
    if (etudiantId === null || sessionId.trim() === '') {
      setErreur('Choisissez votre nom et indiquez la session.');
      return;
    }
    void executer(async () => {
      setNote(await api.relectureRecue(etudiantId, Number(sessionId)));
      return 'Relecture consultée.';
    });
  }

  return (
    <section>
      <h2>Étudiant</h2>
      <Erreur message={erreur} />
      <Succes message={succes} />

      <div className="carte">
        <h3>Qui êtes-vous ?</h3>
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
            value={etudiantId ?? ''}
            onChange={(evenement) => setEtudiantId(Number(evenement.target.value))}
          >
            {etudiants.map((etudiant) => (
              <option key={etudiant.id} value={etudiant.id}>
                {etudiant.nom}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="carte">
        <h3>Marquer ma présence</h3>
        <label>
          Code annoncé en salle
          <input
            value={code}
            autoComplete="off"
            onChange={(evenement) => setCode(evenement.target.value.toUpperCase())}
          />
        </label>
        <button type="button" onClick={marquerPresence} disabled={chargement}>
          Marquer ma présence
        </button>
        <p className="discret">
          Le code expire 15 minutes après l'ouverture (RG1), et 5 erreurs bloquent 2 minutes (RG8).
        </p>
      </div>

      <div className="carte">
        <h3>Mon exercice</h3>
        <label>
          Numéro de session
          <input
            value={sessionId}
            inputMode="numeric"
            onChange={(evenement) => setSessionId(evenement.target.value)}
          />
        </label>
        <label>
          Lien de mon exercice
          <input
            value={lien}
            placeholder="https://…"
            onChange={(evenement) => setLien(evenement.target.value)}
          />
        </label>
        <div className="boutons">
          <button type="button" onClick={deposer} disabled={chargement}>
            Déposer le lien
          </button>
          <button type="button" onClick={consulterNote} disabled={chargement}>
            Voir ma note
          </button>
        </div>

        {chargement && <Chargement texte="Envoi en cours…" />}
        {note !== null && (
          <div className="resultat">
            {note.statut === 'RENDUE' ? (
              <p>
                <strong className="note">{note.note}/20</strong> — {note.commentaire}
              </p>
            ) : (
              <p>Relecture assignée, pas encore rendue. L'identité du relecteur reste confidentielle.</p>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
