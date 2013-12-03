package com.bdoc.specifique;

import org.apache.log4j.Logger;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;

import com.bdoc.specifique.BdocWebServiceInterfaceException;
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
import com.bdoc.java.ediweb.api.exception.BDocWebException;


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
			//test BdocWeb Network Errors. AVELA.
			this.testSocket();
			ewSession = new JBApplication(ewConnect);
		} 		
		catch (BDocWebException e ){			
			logger.error("BDocWebException : "+e.getMessage());
			throw e;
		}
		catch (BDocException e) {
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

	public void testSocket() throws BdocWebServiceInterfaceException{
		try {
			
			   Socket s = null;
	           s = new Socket(serveur, port);
	        } catch (UnknownHostException e) {
	        	// check spelling of hostname
	        	String errorMessage = e.getMessage() + " - ";	        	
	        	throw new BdocWebServiceInterfaceException(4,BdocWebServiceInterfaceException.getErrorLibelle(4) + " " + errorMessage + " - check spelling of hostname") ;
	           
	        } catch (ConnectException e) {
	        	
	        	// connection refused - is server down? Try another port.
	        	String errorMessage = e.getMessage() + " - ";
	        	throw new BdocWebServiceInterfaceException(4,BdocWebServiceInterfaceException.getErrorLibelle(4) + " " + errorMessage + " - connection refused - is server down? Try another port.") ;
	        	
	           
	        } catch (NoRouteToHostException e) {
	        	// The connect attempt timed out.  Try connecting through a proxy
	        	String errorMessage = e.getMessage() + " - ";
	        	throw new BdocWebServiceInterfaceException(4,BdocWebServiceInterfaceException.getErrorLibelle(4) + " " + errorMessage + " - The connect attempt timed out.  Try connecting through a proxy") ;
	        	
	           
	        } 
			catch (SocketException e) {
	        	// socket error occurred
	        	String errorMessage = e.getMessage() + " - ";
	        	throw new BdocWebServiceInterfaceException(4,BdocWebServiceInterfaceException.getErrorLibelle(4) + " " + errorMessage + " - Socket error occurred") ;
	        	           
	        }	
			catch (IOException e) {
	        	// another error occurred
	        	String errorMessage = e.getMessage() + " - ";
	        	throw new BdocWebServiceInterfaceException(4,BdocWebServiceInterfaceException.getErrorLibelle(4) + " " + errorMessage + " - nother error occurred") ;
	        	           
	        }	
		
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
