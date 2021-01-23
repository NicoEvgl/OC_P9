package com.dummy.myerp.testconsumer.consumer;

import com.dummy.myerp.consumer.dao.contrat.DaoProxy;

/**
 * Classe mère des classes de test d'intégration de la couche Consumer
 */
public abstract class ConsumerTestCase {

    /**
     * {@link DaoProxy }
     */
    private static final DaoProxy DAO_PROXY = com.dummy.myerp.testconsumer.consumer.SpringRegistry.getDaoProxy();

    static {
        SpringRegistry.init();
    }


    // ==================== Constructeurs ====================

    /**
     * Constructeur.
     */
    public ConsumerTestCase() {
    }


    // ==================== Getters/Setters ====================
    public static DaoProxy getDaoProxy() {
        return DAO_PROXY;
    }
    
}
