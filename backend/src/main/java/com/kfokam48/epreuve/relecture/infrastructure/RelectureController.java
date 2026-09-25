package com.kfokam48.epreuve.relecture.infrastructure;

/**
 * Adapte le module relecture au monde extérieur (contrat {@code api/contrat.yaml}).
 * <ul>
 *   <li>{@code POST /api/relectures/{id}} → 200, 400 (NOTE_INVALIDE), 403 (AUTO_RELECTURE),
 *       409 (RELECTURE_DEJA_RENDUE)</li>
 * </ul>
 */
public class RelectureController {
}
