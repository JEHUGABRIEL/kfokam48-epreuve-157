import { useCallback, useEffect, useState } from 'react';
import { api, ErreurApi, type Etudiant, type PageDe, type Promotion } from '../api/client';

/**
 * F3 : le référentiel — promotions et étudiants — n'est chargé qu'ici.
 *
 * Les trois écrans le chargeaient chacun de leur côté : trois requêtes concurrentes, trois états de
 * chargement à gérer, et surtout trois listes déroulantes qui restaient vides sans rien dire. Une
 * erreur renvoyée par l'API n'est plus avalée : elle ressort dans l'état, pour être affichée sous le
 * champ concerné — une liste vide et une API injoignable ne se ressemblent pas.
 *
 * Le total est celui de l'API, pas celui de ce qui a été reçu : c'est lui qui permet à l'écran de
 * savoir combien de pages existent.
 */

export type EtatListe<T> = {
  donnees: T[];
  total: number;
  chargement: boolean;
  erreur: string | null;
  recharger: () => void;
};

export function messageErreur(erreur: unknown): string {
  if (erreur instanceof ErreurApi) {
    return erreur.message;
  }
  return "API injoignable : le backend a-t-il été démarré ?";
}

/**
 * `clef` identifie la requête : la changer relance le chargement, la passer à `null` vide la liste
 * sans appeler l'API. Elle porte donc la page demandée — c'est ce qui fait qu'un changement de page
 * relance la lecture. `charger` n'est volontairement pas une dépendance de l'effet : la clé suffit à
 * décider quand recharger, et la fonction est relue au rendu qui suit.
 */
function useListe<T>(clef: string | null, charger: () => Promise<PageDe<T>>): EtatListe<T> {
  const [etat, setEtat] = useState<Omit<EtatListe<T>, 'recharger'>>({
    donnees: [],
    total: 0,
    chargement: clef !== null,
    erreur: null,
  });
  const [compteur, setCompteur] = useState(0);

  useEffect(() => {
    if (clef === null) {
      setEtat({ donnees: [], total: 0, chargement: false, erreur: null });
      return;
    }

    let actif = true;
    setEtat({ donnees: [], total: 0, chargement: true, erreur: null });

    charger()
      .then((page) => {
        if (actif) {
          setEtat({ donnees: page.elements, total: page.total, chargement: false, erreur: null });
        }
      })
      .catch((erreur: unknown) => {
        if (actif) {
          setEtat({ donnees: [], total: 0, chargement: false, erreur: messageErreur(erreur) });
        }
      });

    return () => {
      actif = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clef, compteur]);

  return { ...etat, recharger: useCallback(() => setCompteur((precedent) => precedent + 1), []) };
}

export function usePromotions(page: number): EtatListe<Promotion> {
  return useListe<Promotion>(`promotions-${page}`, () => api.promotions(page));
}

export function useEtudiants(promotionId: number | null, page: number): EtatListe<Etudiant> {
  return useListe<Etudiant>(
    promotionId === null ? null : `etudiants-${promotionId}-${page}`,
    () => api.etudiants(promotionId ?? 0, page),
  );
}
