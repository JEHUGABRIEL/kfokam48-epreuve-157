import { useState } from 'react';
import { EcranFormateur } from './ecrans/EcranFormateur';
import { EcranEtudiant } from './ecrans/EcranEtudiant';
import { EcranRelecteur } from './ecrans/EcranRelecteur';
import { BarreLaterale, type Role } from './ui/BarreLaterale';

/**
 * F2 : trois écrans — formateur, étudiant, relecteur — et une barre latérale qui les commande.
 *
 * Le rôle vit ici, en mémoire du composant : le changer démonte l'écran précédent. Rien n'est écrit
 * dans le stockage du navigateur (ENF5, Q1) — un poste de salle n'est pas à nous.
 */
export function App() {
  const [role, setRole] = useState<Role>('FORMATEUR');

  return (
    <div className="application">
      <BarreLaterale role={role} onChoisirRole={setRole} />

      <main className="contenu">
        {role === 'FORMATEUR' && <EcranFormateur />}
        {role === 'ETUDIANT' && <EcranEtudiant />}
        {role === 'RELECTEUR' && <EcranRelecteur />}
      </main>
    </div>
  );
}
