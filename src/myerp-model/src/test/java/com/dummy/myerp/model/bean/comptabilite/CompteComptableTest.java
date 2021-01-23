package com.dummy.myerp.model.bean.comptabilite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CompteComptableTest {

    private CompteComptable compteComptable;
    Integer numero = 401;
    String libelle = "TestLibelle";
    static List<CompteComptable> compteComptableList;

    @BeforeClass
    public static void initListCompteComptable(){
        compteComptableList = new ArrayList<>();
        CompteComptable c;
        for (int i = 0; i < 10; i++) {
            c = new CompteComptable();
            c.setNumero(i);
            c.setLibelle("Compte Test" + (i));
            compteComptableList.add(c);
        }
    }


    @Before
    public void initCompteComptable(){
        compteComptable = new CompteComptable(numero, libelle);

    }

    @Test
    public void testConstructorCreation(){
        Assert.assertEquals(compteComptable.getLibelle(),libelle);
        Assert.assertEquals(compteComptable.getNumero(),numero);
    }

    @Test
    public void testGetCompteComptableByNumeroIfCompteInListReturnCompte(){
        CompteComptable compteInList = CompteComptable.getByNumero(compteComptableList, 5);
        Assert.assertEquals(compteInList, compteComptableList.get(5));
    }

    @Test
    public void testGetCompteComptableByNumeroIfCompteNotExistReturnNull(){
        CompteComptable compteInList = CompteComptable.getByNumero(compteComptableList, 13);
        Assert.assertEquals(null, compteInList);
    }
}
