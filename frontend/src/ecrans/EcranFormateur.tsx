import { useState } from 'react';
import { api, ErreurApi, type LigneTableau, type SessionOuverte } from '../api/client';
import { ListeDeroulante } from '../ui/ListeDeroulante';
import { Chargement, Erreur, Succes } from '../ui/Messages';
import { usePromotions } from '../ui/useListes';

/**
 * Écran formateur — EF2 (ouvrir une session et obtenir un code), EF8 (clôturer), EF9 (tableau).
 *
 * La moyenne affichée vient de l'API : elle n'est jamais recalculée ici (F3).
 */
export function EcranFormateur() {
  const promotions = usePromotions();
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [titre, setTitre] = useState('Séance du jour');
  const [session, setSession] = useState<SessionOuverte | null>(null);
  const [tableau, setTableau] = useState<LigneTableau[] | null>(null);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  async function ouvrir() {
    if (promotionId === null) {
      setErreur('Choisissez une promotion.');
      return;
    }
    setChargement(true);
    setErreur(null);
    setSucces(null);
    try {
      const ouverte = await api.ouvrirSession(titre, promotionId);
      setSession(ouverte);
      setSucces(`Session ouverte. Code à annoncer en salle : ${ouverte.code}`);
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  async function cloturer() {
    if (session === null) {
      return;
    }
    setChargement(true);
    setErreur(null);
    try {
      await api.cloturerSession(session.id);
      setSucces('Session clôturée : plus aucune présence, dépôt ni relecture ne peut y être écrit.');
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  async function chargerTableau() {
    if (promotionId === null) {
      setErreur('Choisissez une promotion.');
      return;
    }
    setChargement(true);
    setErreur(null);
    try {
      setTableau(await api.tableau(promotionId));
    } catch (e) {
      setErreur((e as ErreurApi).message);
    } finally {
      setChargement(false);
    }
  }

  return (
    <section>
      <h2>Formateur</h2>
      <Erreur message={erreur} />
      <Succes message={succes} />

      <div className="carte">
        <h3>Ouvrir une session</h3>
        <ListeDeroulante
          libelle="Promotion"
          etat={promotions}
          valeur={promotionId}
          onChange={setPromotionId}
          invitation="— Choisir une promotion —"
        />
        <label>
          Titre de la séance
          <input value={titre} onChange={(evenement) => setTitre(evenement.target.value)} />
        </label>
        <button type="button" onClick={ouvrir} disabled={chargement}>
          Ouvrir et obtenir le code
        </button>

        {session !== null && (
          <div className="resultat">
            <p>
              Code : <strong className="code">{session.code}</strong>
            </p>
            <p className="discret">
              Valable jusqu'à {new Date(session.expirationAt).toLocaleTimeString()} (RG1 : 15 minutes).
            </p>
            <button type="button" onClick={cloturer} disabled={chargement}>
              Clôturer la session
            </button>
          </div>
        )}
      </div>

      <div className="carte">
        <h3>Tableau récapitulatif</h3>
        <button type="button" onClick={chargerTableau} disabled={chargement}>
          Afficher le tableau de la promotion
        </button>
        {chargement && <Chargement texte="Chargement…" />}
        {tableau !== null && (
          <table>
            <thead>
              <tr>
                <th>Étudiant</th>
                <th>Présences</th>
                <th>Exercices déposés</th>
                <th>Moyenne</th>
                <th>Relectures en attente</th>
              </tr>
            </thead>
            <tbody>
              {tableau.map((ligne) => (
                <tr key={ligne.etudiantId}>
                  <td>{ligne.nom}</td>
                  <td>{ligne.presences}</td>
                  <td>{ligne.exercicesDeposes}</td>
                  {/* La moyenne vient de l'API ; « — » signifie « aucune note reçue », pas « 0 ». */}
                  <td>{ligne.moyenne === null ? '—' : ligne.moyenne}</td>
                  <td>{ligne.relecturesEnAttente}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
