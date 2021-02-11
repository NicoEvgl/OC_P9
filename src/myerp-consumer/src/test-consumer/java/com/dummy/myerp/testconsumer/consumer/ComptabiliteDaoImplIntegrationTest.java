package com.dummy.myerp.testconsumer.consumer;

import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class ComptabiliteDaoImplIntegrationTest extends ConsumerTestCase {

    @Test
    public void getListCompteComptableShouldReturnList() {

        List<CompteComptable> compteComptableList = getDaoProxy().getComptabiliteDao().getListCompteComptable();
        assertThat(compteComptableList).isNotEmpty();

    }

    @Test
    public void getListJournalComptableShouldReturnList() {
        List<JournalComptable> journalComptableList = getDaoProxy().getComptabiliteDao().getListJournalComptable();
        assertThat(journalComptableList).isNotEmpty();

    }

    @Test
    public void getListEcritureComptableShouldReturnList() {
        List<EcritureComptable> ecritureComptableList = getDaoProxy().getComptabiliteDao().getListEcritureComptable();
        assertThat(ecritureComptableList).isNotEmpty();

    }

    @Test
    public void getEcritureComptableShouldReturnEcritureComptable() throws NotFoundException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateStringTest = "2016-12-30";

        EcritureComptable ecritureComptable;
        try {
            ecritureComptable = getDaoProxy().getComptabiliteDao().getEcritureComptable(-2);
            assertThat(ecritureComptable).isNotNull();
            assertThat(ecritureComptable.getReference()).isEqualTo("VE-2016/00002");
            assertThat(ecritureComptable.getJournal().getCode()).isEqualTo("VE");
            Date dateTest = formatter.parse(dateStringTest);
            assertThat(ecritureComptable.getDate()).isEqualTo(dateTest);
            assertThat(ecritureComptable.getLibelle()).isEqualTo("Imprimante");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = NotFoundException.class)
    public void getEcritureComptableByIdShouldReturnException() throws NotFoundException {
        getDaoProxy().getComptabiliteDao().getEcritureComptable(-10);
    }


    @Test
    public void getEcritureComptableByRefShouldReturnEcritureComptable() throws NotFoundException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateStringTest = "2016-12-30";

        EcritureComptable ecritureComptable;
        try {
            ecritureComptable = getDaoProxy().getComptabiliteDao().getEcritureComptableByRef("VE-2016/00002");
            assertThat(ecritureComptable).isNotNull();
            assertThat(ecritureComptable.getId()).isEqualTo(-2);
            assertThat(ecritureComptable.getJournal().getCode()).isEqualTo("VE");
            Date dateTest = formatter.parse(dateStringTest);
            assertThat(ecritureComptable.getDate()).isEqualTo(dateTest);
            assertThat(ecritureComptable.getLibelle()).isEqualTo("Imprimante");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = NotFoundException.class)
    public void getEcritureComptableByReferenceShouldReturnException() throws NotFoundException {
        getDaoProxy().getComptabiliteDao().getEcritureComptableByRef("VE-2016/00010");
    }

    @Test
    public void loadListLigneEcriture() throws NotFoundException {
        EcritureComptable ecritureComptable = getDaoProxy().getComptabiliteDao().getEcritureComptable(-5);
        getDaoProxy().getComptabiliteDao().loadListLigneEcriture(ecritureComptable);
        assertThat(ecritureComptable.getListLigneEcriture().get(0).getCompteComptable().getNumero()).isEqualTo(512);
        assertThat(ecritureComptable.getListLigneEcriture().get(1).getCompteComptable().getNumero()).isEqualTo(411);
    }



    @Test
    public void getListSequenceEcritureComptableShouldReturnList(){
        List<SequenceEcritureComptable> sequenceEcritureComptableList = getDaoProxy().getComptabiliteDao().getListSequenceEcritureComptable();
        assertThat(sequenceEcritureComptableList).isNotEmpty();
    }

    @Test
    public void getLastSequenceEcritureComptableShouldReturnLastSequence() throws NotFoundException {
        SequenceEcritureComptable lastSequence = getDaoProxy().getComptabiliteDao().getSequenceEcritureComptable("BQ", 2016);
        assertThat(lastSequence).isNotNull();
        assertThat(lastSequence.getDerniereValeur()).isEqualTo(51);
    }


}
