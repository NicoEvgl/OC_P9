package com.dummy.myerp.business.impl.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.dummy.myerp.model.bean.comptabilite.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionStatus;
import com.dummy.myerp.business.contrat.manager.ComptabiliteManager;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;


/**
 * Comptabilite manager implementation.
 */
public class ComptabiliteManagerImpl extends AbstractBusinessManager implements ComptabiliteManager {

    // ==================== Attributs ====================


    // ==================== Constructeurs ====================
    /**
     * Instantiates a new Comptabilite manager.
     */
    public ComptabiliteManagerImpl() {
    }


    // ==================== Getters/Setters ====================
    @Override
    public List<CompteComptable> getListCompteComptable() {
        return getDaoProxy().getComptabiliteDao().getListCompteComptable();
    }


    @Override
    public List<JournalComptable> getListJournalComptable() {
        return getDaoProxy().getComptabiliteDao().getListJournalComptable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcritureComptable> getListEcritureComptable() {
        return getDaoProxy().getComptabiliteDao().getListEcritureComptable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addReference(EcritureComptable pEcritureComptable) {

        // Bien se référer à la JavaDoc de cette méthode !
        /* Le principe :
                1.  Remonter depuis la persistance la dernière valeur de la séquence du journal pour l'année de l'écriture
                    (table sequence_ecriture_comptable)
                2.  * S'il n'y a aucun enregistrement pour le journal pour l'année concernée :
                        1. Utiliser le numéro 1.
                    * Sinon :
                        1. Utiliser la dernière valeur + 1
                3.  Mettre à jour la référence de l'écriture avec la référence calculée (RG_Compta_5)
                4.  Enregistrer (insert/update) la valeur de la séquence en persistance
                    (table sequence_ecriture_comptable)
         */
        /*1.  Remonter depuis la persistance la dernière valeur de la séquence du journal pour l'année de l'écriture
                            (table sequence_ecriture_comptable)
         Remonter depuis la persistance la dernière valeur de la séquence du journal pour l'année de l'écriture
         implémenter un moyen de récupérer le dernier numéro de séquence. la séquence représente l'année + le numéro chrono
         pour la regex on obtient : codeJournal-annéeSequence/valeurSequence
         */
        // nouvelle séquence
        SequenceEcritureComptable newSequenceEcritureComptable = new SequenceEcritureComptable();

        //récupérer l'année de l'écriture
        Date date = pEcritureComptable.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer ecritureYear = calendar.get(Calendar.YEAR);
        // journal visé
        String journalCode = pEcritureComptable.getJournal().getCode();

        SequenceEcritureComptable sequenceEcritureComptableInBdd;
        try {
            sequenceEcritureComptableInBdd = getDaoProxy()
                                                     .getComptabiliteDao()
                                                     .getSequenceEcritureComptable(journalCode, ecritureYear);
        } catch (NotFoundException e){
            sequenceEcritureComptableInBdd = null;
        }

        /*2.  * S'il n'y a aucun enregistrement pour le journal pour l'année concernée :
                Utiliser le numéro 1.
                                      * Sinon :
                Utiliser la dernière valeur + 1
         */

        // sequence présente
        if (sequenceEcritureComptableInBdd != null) {
            // On set les attributs de la nouvelle séquence
            newSequenceEcritureComptable.setJournalCode(sequenceEcritureComptableInBdd.getJournalCode());
            newSequenceEcritureComptable.setAnnee(sequenceEcritureComptableInBdd.getAnnee());

            // On ajoute 1
            newSequenceEcritureComptable.setDerniereValeur(sequenceEcritureComptableInBdd.getDerniereValeur() + 1);
        } else {
            newSequenceEcritureComptable.setJournalCode(journalCode);
            newSequenceEcritureComptable.setAnnee(ecritureYear);
            newSequenceEcritureComptable.setDerniereValeur(1);
        }

        /*  3.  Mettre à jour la référence de l'écriture avec la référence calculée (RG_Compta_5)
         */
        // On formate la référence en respectant la RG_Compta_5
        String updateReference = journalCode + "-" + ecritureYear + "/" + String.format("%05d", newSequenceEcritureComptable.getDerniereValeur());

        //On update la référence de pEcriture
        pEcritureComptable.setReference(updateReference);

        // Lancement Transaction
        if (newSequenceEcritureComptable.getDerniereValeur() == 1) {
            insertSequenceEcritureComptable(newSequenceEcritureComptable);
            getDaoProxy().getComptabiliteDao().insertSequenceEcritureComptable(newSequenceEcritureComptable);
        } else {
            updateSequenceEcritureComptable(newSequenceEcritureComptable);
            getDaoProxy().getComptabiliteDao().updateSequenceEcritureComptable(newSequenceEcritureComptable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptableUnit(pEcritureComptable);
        this.checkEcritureComptableContext(pEcritureComptable);
    }


    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion unitaires,
     * c'est à dire indépendemment du contexte (unicité de la référence, exercice comptable non clôturé...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */
    protected void checkEcritureComptableUnit(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== Vérification des contraintes unitaires sur les attributs de l'écriture
        Set<ConstraintViolation<EcritureComptable>> vViolations = getConstraintValidator().validate(pEcritureComptable);
        if (!vViolations.isEmpty()) {
            throw new FunctionalException("L'écriture comptable ne respecte pas les règles de gestion.",
                    new ConstraintViolationException(
                            "L'écriture comptable ne respecte pas les contraintes de validation",
                            vViolations));
        }

        // ===== RG_Compta_2 : Pour qu'une écriture comptable soit valide, elle doit être équilibrée
        regleGestion2(pEcritureComptable);

        // ===== RG_Compta_3 : une écriture comptable doit avoir au moins 2 lignes d'écriture (1 au débit, 1 au crédit)
        regleGestion3(pEcritureComptable);

        // ===== RG_Compta_5 : La référence d'une écriture comptable est composée du code du journal dans lequel
        regleGestion5(pEcritureComptable);
    }


    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion liées au contexte
     * (unicité de la référence, année comptable non cloturé...)
     *  RG_Compta_6 : La référence d'une écriture comptable doit être unique
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */
    protected void checkEcritureComptableContext(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== RG_Compta_6 : La référence d'une écriture comptable doit être unique
        if (StringUtils.isNoneEmpty(pEcritureComptable.getReference())) {
            try {
                // Recherche d'une écriture ayant la même référence
                EcritureComptable vECRef = getDaoProxy().getComptabiliteDao().getEcritureComptableByRef(
                        pEcritureComptable.getReference());

                // Si l'écriture à vérifier est une nouvelle écriture (id == null),
                // ou si elle ne correspond pas à l'écriture trouvée (id != idECRef),
                // c'est qu'il y a déjà une autre écriture avec la même référence
                if (pEcritureComptable.getId() == null
                            || !pEcritureComptable.getId().equals(vECRef.getId())) {
                    throw new FunctionalException("Une autre écriture comptable existe déjà avec la même référence.");
                }
            } catch (NotFoundException vEx) {
                vEx.getMessage();
                // Dans ce cas, c'est bon, ça veut dire qu'on n'a aucune autre écriture avec la même référence.
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptable(pEcritureComptable);
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().insertEcritureComptable(pEcritureComptable);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptable(pEcritureComptable);
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().updateEcritureComptable(pEcritureComptable);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEcritureComptable(Integer pId) {
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().deleteEcritureComptable(pId);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertSequenceEcritureComptable(SequenceEcritureComptable pSequenceEcritureComptable) {
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().insertSequenceEcritureComptable(pSequenceEcritureComptable);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSequenceEcritureComptable(SequenceEcritureComptable pSequenceEcritureComptable) {
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().updateSequenceEcritureComptable(pSequenceEcritureComptable);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }


    /**
     * RG_Compta_2 : Pour qu'une écriture comptable soit valide, elle doit être équilibrée
     * @param pEcritureComptable
     * @throws FunctionalException
     */
    public void regleGestion2(EcritureComptable pEcritureComptable) throws FunctionalException{
        if (!pEcritureComptable.isEquilibree()) {
            throw new FunctionalException("L'écriture comptable n'est pas équilibrée.");
        }
    }

    /**
     * RG_Compta_3 : une écriture comptable doit avoir au moins 2 lignes d'écriture (1 au débit, 1 au crédit)
     * @param pEcritureComptable
     * @throws FunctionalException
     */
    public void regleGestion3(EcritureComptable pEcritureComptable) throws FunctionalException{
        int vNbrCredit = 0;
        int vNbrDebit = 0;
        for (LigneEcritureComptable vLigneEcritureComptable : pEcritureComptable.getListLigneEcriture()) {
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getCredit(),
                    BigDecimal.ZERO)) != 0) {
                vNbrCredit++;
            }
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getDebit(),
                    BigDecimal.ZERO)) != 0) {
                vNbrDebit++;
            }
        }
        // On test le nombre de lignes car si l'écriture à une seule ligne
        //      avec un montant au débit et un montant au crédit ce n'est pas valable
        if (pEcritureComptable.getListLigneEcriture().size() < 2
                    || vNbrCredit < 1
                    || vNbrDebit < 1) {
            throw new FunctionalException(
                    "L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }
    }

    /**
     * RG_Compta_5 : La référence d'une écriture comptable est composée du code du journal dans lequel
     * figure l'écriture suivi de l'année et d'un numéro de séquence (propre à chaque journal) sur 5 chiffres
     * incrémenté automatiquement à chaque écriture. Le formatage de la référence est : XX-AAAA/#####.
     * @param pEcritureComptable
     * @throws FunctionalException
     */
    public void regleGestion5(EcritureComptable pEcritureComptable) throws FunctionalException{
        String regex = "\\D{1,5}-\\d{4}/\\d{5}";
        Pattern pattern = Pattern.compile(regex);
        // On récupère la référence
        String referenceEcriture = pEcritureComptable.getReference();
        Matcher matcher = pattern.matcher(referenceEcriture);
        //on teste la regex
        if (!matcher.matches()){
            throw new FunctionalException("La référence de l'écriture ne respecte pas le format.");
        }
        //
        // On split la référence
        String [] splitReferenceEcriture = referenceEcriture.split("[\\-/]");
        //récupérer le code du journal
        String journalCode = pEcritureComptable.getJournal().getCode();

        // On vérifie la correspondance du code journal
        if (!journalCode.equals(splitReferenceEcriture[0])){
            throw new FunctionalException("Le code journal de la référence est différent du code journal de l'écriture.");
        }

        //récupérer l'année de l'écriture
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pEcritureComptable.getDate());
        String ecritureYear = String.valueOf(calendar.get(Calendar.YEAR));

        // On vérifie la correspondance du code journal
        if (!ecritureYear.equals(splitReferenceEcriture[1])){
            throw new FunctionalException("La date de la référence est différente de la date de l'écriture.");
        }
    }
}
