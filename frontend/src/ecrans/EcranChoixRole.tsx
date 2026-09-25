import { ROLES, type Role } from '../ui/roles';

/**
 * Écran d'accueil — première chose affichée, avant toute interface.
 *
 * Auparavant l'application s'ouvrait directement sur l'écran formateur : on voyait le tableau d'une
 * promotion avant d'avoir dit qui on était. Ici, chaque rôle annonce d'abord **ce qu'il permet de
 * faire** et la liste de ses fonctionnalités, pour qu'on sache où l'on met les pieds avant de cliquer.
 *
 * Le rôle choisi ne vit qu'en mémoire de la page (Q1, ENF5) : rien n'est écrit dans le stockage du
 * navigateur, un poste de salle n'est pas à nous.
 */

type Props = {
  onChoisirRole: (role: Role) => void;
};

export function EcranChoixRole({ onChoisirRole }: Props) {
  return (
    <main className="accueil">
      <header className="marque">
        <h1>Suivi de présence</h1>
        <p className="sous-titre">KFOKAM48 — Promotion 2026</p>
      </header>

      <h2>Qui êtes-vous ?</h2>
      <p className="discret">
        Choisissez votre rôle pour ouvrir l'interface correspondante. Il n'y a pas de mot de passe
        (Q1, ENF5) : ce choix remplace la connexion, et rien n'en est conservé après la fermeture de
        la page.
      </p>

      <ul className="choix-roles">
        {ROLES.map((entree) => (
          <li key={entree.role}>
            <button
              type="button"
              className="choix-role"
              onClick={() => onChoisirRole(entree.role)}
            >
              <span className="choix-role-nom">{entree.libelle}</span>
              <span className="choix-role-resume">{entree.resume}</span>
              <span className="choix-role-fonctions">
                {entree.sections.map((section) => section.libelle).join(' · ')}
              </span>
            </button>
          </li>
        ))}
      </ul>
    </main>
  );
}
