package com.dummy.myerp.model.bean.comptabilite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class LigneEcritureComptableTest {

    private LigneEcritureComptable ligneEcritureComptable;
    private static CompteComptable compteComptable = new CompteComptable(411, "Clients");

    @Before
    public void initLigneEcritureComptable(){
        ligneEcritureComptable = new LigneEcritureComptable();
        ligneEcritureComptable.setCompteComptable(compteComptable);
        ligneEcritureComptable.setLibelle("Cartouches d'imprimante");
        ligneEcritureComptable.setDebit(new BigDecimal("100.00"));
        ligneEcritureComptable.setCredit(new BigDecimal("0"));
    }

    @Test
    public void testLigneEcritureComptableToString(){
        Assert.assertEquals("LigneEcritureComptable{compteComptable=CompteComptable{numero=411, libelle='Clients'}, libelle='Cartouches d'imprimante', " +
                                    "debit=100.00, credit=0}", ligneEcritureComptable.toString());
    }
}
