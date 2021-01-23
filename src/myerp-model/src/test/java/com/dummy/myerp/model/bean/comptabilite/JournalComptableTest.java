package com.dummy.myerp.model.bean.comptabilite;

import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JournalComptableTest {

    private static List<JournalComptable> vList;

    @BeforeClass
    public static void setUpBeforeClass() {
        vList = new ArrayList<>(0);
    }

    @Before
    public void initJournalComptableList(){
        vList.add(new JournalComptable("AC", "Achat"));
        vList.add(new JournalComptable("BQ", "Banque"));
    }

    @Test
    public void getJournalByCodeShouldReturnJournal(){
        assertEquals(JournalComptable.getByCode(vList, "AC"), vList.get(0));
    }

    @Test
    public void testJournalComptableToString(){
        assertEquals("JournalComptable{code='AC', libelle='Achat'}", vList.get(0).toString());
    }

    @After
    public void clearList() {
        vList.clear();
    }
}
