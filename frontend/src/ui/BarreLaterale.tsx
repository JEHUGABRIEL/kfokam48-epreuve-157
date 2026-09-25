/**
 * F2 : la barre latérale remplace les onglets horizontaux — le rôle est la navigation principale, il
 * commande tout l'écran situé à sa droite.
 *
 * Il n'y a pas d'authentification (Q1, ENF5) : « choisir son rôle » remplace la connexion. Chaque
 * entrée annonce ce que le rôle permet de faire, pour qu'on sache où l'on met les pieds avant de
 * cliquer, et l'identité (promotion, nom) n'est jamais conservée hors de la page.
 */

export type Role = 'FORMATEUR' | 'ETUDIANT' | 'RELECTEUR';

type Entree = {
  role: Role;
  libelle: string;
  resume: string;
};

export const ROLES: Entree[] = [
  {
    role: 'FORMATEUR',
    libelle: 'Formateur',
    resume: 'Ouvrir une séance et annoncer son code, la clôturer, afficher le tableau.',
  },
  {
    role: 'ETUDIANT',
    libelle: 'Étudiant',
    resume: 'Marquer sa présence, déposer un exercice, consulter sa note.',
  },
  {
    role: 'RELECTEUR',
    libelle: 'Relecteur',
    resume: 'Rendre une note et un commentaire sur les travaux qui lui sont assignés.',
  },
];

type Props = {
  role: Role;
  onChoisirRole: (role: Role) => void;
};

export function BarreLaterale({ role, onChoisirRole }: Props) {
  return (
    <aside className="barre-laterale">
      <div className="marque">
        <h1>Suivi de présence</h1>
        <p className="sous-titre">KFOKAM48 — Promotion 2026</p>
      </div>

      <nav className="navigation" aria-label="Choix du rôle">
        <p className="etiquette">Je suis…</p>
        {ROLES.map((entree) => (
          <button
            key={entree.role}
            type="button"
            className={entree.role === role ? 'lien-role actif' : 'lien-role'}
            aria-current={entree.role === role ? 'page' : undefined}
            onClick={() => onChoisirRole(entree.role)}
          >
            <span className="lien-role-nom">{entree.libelle}</span>
            <span className="lien-role-resume">{entree.resume}</span>
          </button>
        ))}
      </nav>

      <footer className="note-bas">
        <p>
          Il n'y a pas de mot de passe (Q1, ENF5) : le rôle remplace la connexion, et l'identité se
          choisit dans l'écran. Les règles métier — expiration du code, tirage du relecteur,
          verrouillage de la note, moyenne — sont appliquées par l'API. Cet écran ne les recalcule
          jamais (F3).
        </p>
      </footer>
    </aside>
  );
}
