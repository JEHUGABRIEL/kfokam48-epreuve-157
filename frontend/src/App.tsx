import { useState } from 'react';
import { EcranChoixRole } from './ecrans/EcranChoixRole';
import { EcranFormateur } from './ecrans/EcranFormateur';
import { EcranEtudiant } from './ecrans/EcranEtudiant';
import { EcranRelecteur } from './ecrans/EcranRelecteur';
import { BarreLaterale } from './ui/BarreLaterale';
import { premiereSection, type Role, type Section } from './ui/roles';

/**
 * F2 : trois écrans — formateur, étudiant, relecteur — précédés d'un écran d'accueil qui fait choisir
 * son rôle, puis une barre latérale qui liste les fonctionnalités de ce rôle.
 *
 * Le rôle et la fonctionnalité affichée vivent ici, en mémoire du composant : rien n'est écrit dans le
 * stockage du navigateur (ENF5, Q1) — un poste de salle n'est pas à nous. Changer de rôle démonte
 * l'écran précédent, donc les identités (promotion, nom) choisies dans l'ancien rôle ne suivent pas.
 */
export function App() {
  const [role, setRole] = useState<Role | null>(null);
  const [section, setSection] = useState<Section | null>(null);

  /** On entre par la première fonctionnalité du rôle : l'utilisateur n'a jamais un écran vide à droite. */
  function choisirRole(choisi: Role) {
    setRole(choisi);
    setSection(premiereSection(choisi));
  }

  function changerDeRole() {
    setRole(null);
    setSection(null);
  }

  if (role === null || section === null) {
    return <EcranChoixRole onChoisirRole={choisirRole} />;
  }

  return (
    <div className="application">
      <BarreLaterale
        role={role}
        section={section}
        onChoisirSection={setSection}
        onChangerRole={changerDeRole}
      />

      <main className="contenu">
        {role === 'FORMATEUR' && <EcranFormateur section={section} />}
        {role === 'ETUDIANT' && <EcranEtudiant section={section} />}
        {role === 'RELECTEUR' && <EcranRelecteur section={section} />}
      </main>
    </div>
  );
}
