package com.dummy.myerp.model.bean.comptabilite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SequenceEcritureComptableTest {

    private SequenceEcritureComptable sequenceEcritureComptable;

    @Before
    public void initSequenceEcritureComptable(){
        sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setJournalCode("AC");
        sequenceEcritureComptable.setAnnee(2020);
        sequenceEcritureComptable.setDerniereValeur(00001);
    }

    @Test
    public void testSequenceEcritureComptableToString(){
        Assert.assertEquals("SequenceEcritureComptable{journal=AC, annee=2020, derniereValeur=1}", sequenceEcritureComptable.toString());
    }
}
