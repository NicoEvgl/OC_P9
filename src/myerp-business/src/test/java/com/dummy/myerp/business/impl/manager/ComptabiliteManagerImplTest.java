package com.dummy.myerp.business.impl.manager;

import com.dummy.myerp.business.contrat.BusinessProxy;
import com.dummy.myerp.business.impl.TransactionManager;
import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComptabiliteManagerImplTest {

    private ComptabiliteManagerImpl manager = new ComptabiliteManagerImpl();

    @Mock
    private DaoProxy daoProxy;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private BusinessProxy businessProxy;

    @Mock
    private ComptabiliteDao comptabiliteDao;

    private EcritureComptable vEcritureComptable;

    private LigneEcritureComptable createLigne(Integer pCompteComptableNumero, String pDebit, String pCredit) {
        BigDecimal vDebit = pDebit == null ? null : new BigDecimal(pDebit);
        BigDecimal vCredit = pCredit == null ? null : new BigDecimal(pCredit);
        String vLibelle = ObjectUtils.defaultIfNull(vDebit, BigDecimal.ZERO)
                                  .subtract(ObjectUtils.defaultIfNull(vCredit, BigDecimal.ZERO)).toPlainString();
        return new LigneEcritureComptable(new CompteComptable(pCompteComptableNumero),
                vLibelle,
                vDebit, vCredit);
    }

    @Before
    public void setUpMockDao() {
        ComptabiliteManagerImpl.configure(businessProxy, daoProxy, transactionManager);
        when(daoProxy.getComptabiliteDao()).thenReturn(comptabiliteDao);
        vEcritureComptable = new EcritureComptable();
    }

    @Test
    public void checkEcritureComptableUnit() throws FunctionalException {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("AC-2020/00001");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(123)));
        manager.checkEcritureComptableUnit(vEcritureComptable);

    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptable() throws FunctionalException{
        vEcritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("BQ-2020/00001");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(123)));
        manager.checkEcritureComptable(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitViolation() throws Exception {
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitRG2() throws Exception {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(1234)));
        manager.regleGestion2(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitRG3() throws Exception {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        manager.regleGestion3(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void shouldReturnExceptionWhenEcritureComptableUnitHasOneLineRG3() throws Exception {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        manager.regleGestion3(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void shouldReturnExceptionWhenEcritureComptableUnitHasNoCreditRG3() throws Exception {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                "coucou", null,
                new BigDecimal(123)));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                "coucou", null,
                null));
        manager.regleGestion3(vEcritureComptable);
    }

    //test regex
    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitRG5WhenSequenceTooLong() throws Exception{
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("AC-2020/000001");

        manager.regleGestion5(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void shouldReturnExceptionWhenYearNotMatchRG5() throws Exception{
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("AC-2018/00001");

        manager.regleGestion5(vEcritureComptable);
    }

    @Test
    public void shouldReturnExceptionWhenJournalCodeNotMatchRG5(){
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("ABN-2020/00001");
        try{
            manager.regleGestion5(vEcritureComptable);
            fail("FunctionalException not thrown");
        } catch (FunctionalException e){

        }
    }

    @Test
    public void addReference() throws NotFoundException {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1, "test"),
                null, new BigDecimal(1234),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2, "test"),
                null, null,
                new BigDecimal(1234)));
        Date date = vEcritureComptable.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer ecritureYear = calendar.get(Calendar.YEAR);
        String journalCode = "AC";

        when(comptabiliteDao.getSequenceEcritureComptable(journalCode, ecritureYear)).thenReturn(new SequenceEcritureComptable(journalCode, ecritureYear, 8));

        manager.addReference(vEcritureComptable);

        assertEquals("AC-2020/00009", vEcritureComptable.getReference());
    }


    @Test
    public void addReferenceShouldInsertNewSequence() throws NotFoundException {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1, "test"),
                null, new BigDecimal(1234),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2, "test"),
                null, null,
                new BigDecimal(1234)));
        Date date = vEcritureComptable.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer ecritureYear = calendar.get(Calendar.YEAR);
        String journalCode = "AC";

        when(comptabiliteDao.getSequenceEcritureComptable(journalCode, ecritureYear)).thenReturn(null);

        manager.addReference(vEcritureComptable);

        assertEquals("AC-2020/00001", vEcritureComptable.getReference());
    }

    @Test
    public void shouldReturnFunctionalExceptionWhenEcritureComptableIdIsDifferentFromDbRG6() throws NotFoundException {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("AC-2020/00001");
        vEcritureComptable.setId(1);
        EcritureComptable returnedEcritureComptable = new EcritureComptable();
        returnedEcritureComptable.setId(vEcritureComptable.getId() + 1); // not the same id !!
        returnedEcritureComptable.setReference(vEcritureComptable.getReference());

        when(comptabiliteDao.getEcritureComptableByRef(vEcritureComptable.getReference()))
                .thenReturn(returnedEcritureComptable);
        try{
            manager.checkEcritureComptableContext(vEcritureComptable);
            fail("FunctionalException not thrown");
        } catch (FunctionalException exception){
            assertEquals("Une autre écriture comptable existe déjà avec la même référence.", exception.getMessage());
        }
    }

    @Test
    public void shouldNotReturnFunctionalExceptionWhenEcritureComptableIdIsSameFromDbRG6() throws NotFoundException, FunctionalException {
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("AC-2020/00001");
        vEcritureComptable.setId(1);

        when(comptabiliteDao.getEcritureComptableByRef(vEcritureComptable.getReference()))
                .thenReturn(vEcritureComptable);
        manager.checkEcritureComptableContext(vEcritureComptable);
    }

    @Test
    public void shouldNotReturnFunctionalExceptionIfEcritureComptableNotFoundInDb() throws NotFoundException, FunctionalException {
        lenient().when(comptabiliteDao.getEcritureComptableByRef(anyString())).thenThrow(NotFoundException.class);
        manager.checkEcritureComptableContext(vEcritureComptable);
    }

    @Test
    public void shouldNotReturnFunctionalExceptionWhenCheckEcritureComptable() throws NotFoundException, FunctionalException {
        vEcritureComptable.setId(1);
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.setReference("AC-2020/00001");
        vEcritureComptable.getListLigneEcriture().add(this.createLigne(1, "200.50", null));
        vEcritureComptable.getListLigneEcriture().add(this.createLigne(1, "100.50", "33"));
        vEcritureComptable.getListLigneEcriture().add(this.createLigne(2, null, "301"));
        vEcritureComptable.getListLigneEcriture().add(this.createLigne(2, "40", "7"));
        when(comptabiliteDao.getEcritureComptableByRef(anyString())).thenThrow(NotFoundException.class);
        manager.checkEcritureComptable(vEcritureComptable);
    }

    @After
    public void clearEcritureComptable() {
        vEcritureComptable = null;
    }


}
