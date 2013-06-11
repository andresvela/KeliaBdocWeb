package com.bdoc.specifique;

import org.apache.log4j.Logger;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;

import com.bdoc.java.ediweb.api.ApplicationManager;
import com.bdoc.java.ediweb.api.BDocDocument;
import com.bdoc.java.ediweb.api.GenerationOptions;
import com.bdoc.java.ediweb.api.JBApplication;
import com.bdoc.java.ediweb.api.JBConnection;
import com.bdoc.java.ediweb.api.JBDomain;
import com.bdoc.java.ediweb.api.JBGeneration;
import com.bdoc.java.ediweb.api.JBITemplate;
import com.bdoc.java.ediweb.api.JBItem;
import com.bdoc.java.ediweb.api.JBKeyword;
import com.bdoc.java.ediweb.api.JBKeywordManager;
import com.bdoc.java.ediweb.api.JBTemplate;
import com.bdoc.java.ediweb.api.exception.BDocException;


/**
 * Singleton garantissant l'unicité (singleton) de connexion à bdocweb
 */



public class BdocWebConnection {

	/*
	 *
	 */

	private Logger logger = Logger.getLogger(BdocWebConnection.class.getName());
	
	private String serveur;

	private int port;

	private String login;

	private String password;

	private JBApplication ewSession;

	private JBConnection ewConnect;

	private static BdocWebConnection instance;

	public void init() throws Throwable {
		instance = this;
	}
	
	public static BdocWebConnection getInstance() {
		if (instance==null) instance = new BdocWebConnection();
		return instance;
	}

	public String getServeur() {
		return serveur;
	}

	public void setServeur(String serv) {
		serveur = serv;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String log) {
		login = log;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String pass) {
		password = pass;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int portnumber) {
		port = portnumber;
	}

	public JBConnection doConnect() throws Throwable {
		
		JBConnection ewConnect = null;		
		boolean ewConnected = false;
		try {
			ewConnect = new JBConnection(serveur, port);
			ewConnected = true;
			logger.info("Connexion BDOCWeb : serveur="+serveur+":"+port+" - login = "+login);			
			ewSession = new JBApplication(ewConnect);
		} catch (BDocException e) {
			logger.error("BDocWebException : "+e.getMessage());
			throw e;
		} 
		catch (Throwable e) {
			logger.error("Throwable : "+e.getMessage());
			throw e;
		}
	
		if (ewConnected) {
			logger.info("connexion BdocWeb OK");
		} else {
			logger.error("UnknownConnexionException : Erreur de connexion cause inconnue. login="+login+", password="+password+", serveur= "+ serveur +", port="+port);
			throw new Exception("Erreur de connexion cause inconnue. login="+login+", password="+password+", serveur= "+ serveur +", port="+port);
		}

		return ewConnect;
	}

	public synchronized JBApplication getJBApplication(String BWServer,String BWlogin,String BWpassword, int BWport) throws Throwable {
		
		if (ewSession == null || !ewSession.getConnection().isConnectedToBdocweb(true)) {
			logger.debug("Connexion BDOC-WEB");
			setLogin(BWlogin);
			setPassword(BWpassword);
			setServeur(BWServer);
			setPort(BWport);
			doConnect();
		}
		return ewSession;
	}

	public void destroy() {
	}

	public void dispose() throws BDocException {
		logger.info("Déconnexion BdocWeb");
		if (ewConnect != null) {
			ewConnect.closeConnection();
		}
	}
}
