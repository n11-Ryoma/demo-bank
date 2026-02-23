package com.example.ebank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class EBankApplication {
    private static final Logger log = LogManager.getLogger(EBankApplication.class);

	public static void main(String[] args) {
        log.info("EBankApplication startup begin");
        System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", "true");
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
        System.setProperty("log4j2.formatMsgNoLookups", "false");
        System.setProperty("log4j2.enableJndiLookup", "true");
        //System.setProperty("LOG4J_CONFIGURATION_FILE","C:\\Users\\Ryoma\\Desktop\\+Acts\\api\\e-shop-api\\eshop-api\\src\\main\\resources\\log4j2.xml");
		SpringApplication.run(EBankApplication.class, args);
        log.info("EBankApplication startup complete");
	}
}
