package com.kfokam48.epreuve.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * {@code @RestControllerAdvice} centralisé (B4). Quelle que soit l'erreur, la sortie est
 * exactement {@link ErreurDto}, soit {@code { code, message }} : jamais de corps vide, jamais de
 * page d'erreur Spring, jamais de stack trace (ENF3).
 *
 * <p>Trois familles, traitées dans cet ordre de spécificité :
 * <ol>
 *   <li>les erreurs métier ({@link ApiException}) portent elles-mêmes leur code stable et leur
 *       statut — c'est la seule voie pour les codes imposés par le contrat ;</li>
 *   <li>les erreurs de forme (champ manquant, JSON illisible, paramètre absent ou mal typé) sont
 *       regroupées sous un code unique {@code REQUETE_INVALIDE}, car le contrat n'en impose aucun
 *       pour elles ;</li>
 *   <li>tout le reste est un 500 générique : le détail est journalisé côté serveur, jamais renvoyé.</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Erreur de forme : le contrat impose le format, pas le code (cf. cahier des charges §7). */
    static final String CODE_REQUETE_INVALIDE = "REQUETE_INVALIDE";

    /** Filet de sécurité : le client ne doit jamais recevoir autre chose que le format imposé. */
    static final String CODE_ERREUR_INTERNE = "ERREUR_INTERNE";

    // 1. Erreur métier — le code et le statut viennent de l'exception, pas d'une table de correspondance.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErreurDto> gererErreurMetier(ApiException e) {
        return ResponseEntity.status(e.getStatut()).body(new ErreurDto(e.getCode(), e.getMessage()));
    }

    // 2a. Erreurs de forme levées par Spring MVC avant même d'entrer dans un contrôleur.
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErreurDto> gererRequeteInvalide(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ErreurDto(CODE_REQUETE_INVALIDE, messageLisible(e)));
    }

    // 2b. Erreurs que Spring MVC rend d'ordinaire lui-même (route inconnue, verbe non supporté,
    //     type de contenu refusé). Sans ce handler, elles sortiraient en page Spring par défaut,
    //     donc hors du format imposé. On conserve le statut de Spring, le code devient neutre.
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErreurDto> gererErreurSpring(ErrorResponseException e) {
        HttpStatus statut = HttpStatus.valueOf(e.getStatusCode().value());
        return ResponseEntity.status(statut)
                .body(new ErreurDto(CODE_REQUETE_INVALIDE, "La requête n'a pas pu être traitée."));
    }

    // 3. Filet de sécurité. On journalise l'exception réelle ici (ENF3 : ni stack trace ni
    //    requête SQL ne sortent d'ici) et on répond au client au format imposé.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurDto> gererErreurInattendue(Exception e) {
        log.error("Erreur inattendue non mappée par le conseil d'erreurs", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErreurDto(CODE_ERREUR_INTERNE,
                        "Une erreur interne est survenue. Réessayez plus tard."));
    }

    /**
     * Traduit une erreur de forme en phrase française. Le message de Spring serait en anglais et
     * souvent technique (« JSON parse error ») : il n'a rien à faire devant un utilisateur.
     */
    private String messageLisible(Exception e) {
        if (e instanceof MethodArgumentNotValidException validation) {
            String champ = validation.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getField)
                    .findFirst()
                    .orElse("inconnu");
            return "Le champ « " + champ + " » est obligatoire ou invalide.";
        }
        if (e instanceof MissingServletRequestParameterException manquant) {
            return "Le paramètre « " + manquant.getParameterName() + " » est obligatoire.";
        }
        if (e instanceof MethodArgumentTypeMismatchException malType) {
            return "Le paramètre « " + malType.getName() + " » est invalide.";
        }
        return "Le corps de la requête est illisible ou mal formé.";
    }
}
