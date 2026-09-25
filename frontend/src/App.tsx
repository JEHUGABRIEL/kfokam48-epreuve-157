import { useState } from 'react';
import { EcranFormateur } from './ecrans/EcranFormateur';
import { EcranEtudiant } from './ecrans/EcranEtudiant';
import { EcranRelecteur } from './ecrans/EcranRelecteur';

/**
 * F2 : trois écrans — formateur, étudiant, relecteur.
 *
 * Il n'y a pas d'authentification (Q1, ENF5) : le choix d'un rôle ici remplace la connexion, et
 * chaque écran demande ensuite à l'appelant de s'identifier en se choisissant dans une liste.
 */
type Role = 'FORMATEUR' | 'ETUDIANT' | 'RELECTEUR';

const LIBELLES: Record<Role, string> = {
  FORMATEUR: 'Formateur',
  ETUDIANT: 'Étudiant',
  RELECTEUR: 'Relecteur',
};

export function App() {
  const [role, setRole] = useState<Role>('FORMATEUR');

  return (
    <div className="application">
      <header>
        <h1>Suivi de présence, exercices et relecture</h1>
        <p className="sous-titre">KFOKAM48 — Promotion 2026</p>
        <nav className="onglets">
          {(Object.keys(LIBELLES) as Role[]).map((candidat) => (
            <button
              key={candidat}
              type="button"
              className={candidat === role ? 'onglet actif' : 'onglet'}
              onClick={() => setRole(candidat)}
            >
              {LIBELLES[candidat]}
            </button>
          ))}
        </nav>
      </header>

      <main>
        {role === 'FORMATEUR' && <EcranFormateur />}
        {role === 'ETUDIANT' && <EcranEtudiant />}
        {role === 'RELECTEUR' && <EcranRelecteur />}
      </main>

      <footer>
        <p>
          Les règles métier — expiration du code, tirage du relecteur, verrouillage de la note,
          moyenne — sont appliquées par l'API. Cet écran ne les recalcule jamais (F3).
        </p>
      </footer>
    </div>
  );
}
