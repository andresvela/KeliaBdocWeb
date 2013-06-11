package com.bdoc.specifique;


import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.bdoc.java.ediweb.api.BDocDocument;
import com.bdoc.java.ediweb.api.GenerationOptions;
import com.bdoc.java.ediweb.api.JBApplication;
import com.bdoc.java.ediweb.api.JBGeneration;
import com.bdoc.java.ediweb.api.JBIResource;
import com.bdoc.java.ediweb.api.JBTemplate;
import com.bdoc.java.ediweb.api.exception.BDocException;

import org.apache.commons.codec.binary.Base64;


/**
 * Servlet implementation class for Servlet: GTBdocWeb
 * 
 */

public class GTBdocWeb extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
	static final long serialVersionUID = 1L;

	private java.text.NumberFormat nf = java.text.NumberFormat
			.getInstance(Locale.FRENCH);

	private HashMap<Integer, String> Errolib = new HashMap<Integer, String>();

	// Libell� des nom des variables pass� par le flux
	static final String MODELE = "modele";
	static final String DOMAINE = "domaine";
	static final String FLUX = "flux";
	static final String ENVIRONNEMENT = "nom_base";
	static final String BINDENVIRONNEMENT = "EnvironnementAutorises";
	static final String FORMAT = "format";
	static final String CHEMINFDP = "CheminFDP";
	static final String BASE64_CHUNKED = "base64Chunked";
	static final String BDOCWEB_SERVEUR = "BdocWeb_Serveur";
	static final String BDOCWEB_LOGIN = "BdocWeb_Login";
	static final String BDOCWEB_PORT = "BdocWeb_Port";
	static final String BDOCWEB_PASS = "BdocWeb_Password";
	final static String SESSION_ID = "session-id_123456789";

	static final String DEBUGGMODE = "DebugMode";
	static Logger logger = Logger.getLogger(GTBdocWeb.class.getName());

	private String domaine = null;
	private String template = null;
	private byte[] flux = null; 
	
	private JBGeneration ewGeneration;

	private String CheminFDP= null;
	
	// Propi�t�es param�tr�es

	String BWServer = "localhost";
	String BWlogin = null;
	String BWpassword = null;
	int BWport = 800;
//	boolean base64Chunked = false;
//	boolean checkEnvironnement = false;
	StringTokenizer Environnements = null;

	// Propri�t� globales
	JBApplication ewapp = null;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */

	public GTBdocWeb() {
		super();
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */

	@Override
	public void init() throws ServletException {
		Errolib.put(0, "Param�tre data non renseign�");
		Errolib.put(1, "Le nom du mod�le n'est pas renseign�");
		Errolib.put(2, "Le nom du domaine n'est pas renseign�");
		Errolib.put(3, "Le flux est vide ou non renseign�");
		Errolib.put(4, "Le nom de l'environnement n'est pas renseign�");
		Errolib.put(5, "Le format n'est pas renseign�");
		Errolib.put(6, "Le format n'est doit �tre RTF ou PDF");
		Errolib.put(7, "Erreur lors de la v�rification de l'environnement");
		Errolib.put(8, "Erreur lors de l'envoi du flux vers Bdoc-Web");
		Errolib.put(10, "Erreur de conversion Base64");
		Errolib.put(11, "Erreur de conversion du Flux en Base64");
		Errolib.put(403, "Mauvais Environnement");

		// Initialisation de la connexion au serveur
		if (getInitParameter(BDOCWEB_SERVEUR) != null
				&& !getInitParameter(BDOCWEB_SERVEUR).equals(""))
			BWServer = getInitParameter(BDOCWEB_SERVEUR);
		if (getInitParameter(BDOCWEB_LOGIN) != null
				&& !getInitParameter(BDOCWEB_LOGIN).equals(""))
			BWlogin = getInitParameter(BDOCWEB_LOGIN);
		if (getInitParameter(BDOCWEB_PASS) != null
				&& !getInitParameter(BDOCWEB_PASS).equals(""))
			BWpassword = getInitParameter(BDOCWEB_PASS);
		if (getInitParameter(BDOCWEB_PORT) != null
				&& !getInitParameter(BDOCWEB_PORT).equals(""))
			BWport = Integer.valueOf(getInitParameter(BDOCWEB_PORT));

		if (getInitParameter(CHEMINFDP) != null
				&& !getInitParameter(CHEMINFDP).equals(""))
			CheminFDP = getInitParameter(CHEMINFDP);

		
		// Initialisation Environnements
//		if (getInitParameter(BASE64_CHUNKED) != null
//				&& !getInitParameter(BASE64_CHUNKED).equals(""))
//			base64Chunked = Boolean.valueOf(getInitParameter(BASE64_CHUNKED));
//		if (getInitParameter(ENVIRONNEMENT) != null
//				&& !getInitParameter(ENVIRONNEMENT).equals("")) {
//			checkEnvironnement = true;
//			Environnements = new StringTokenizer (getInitParameter(ENVIRONNEMENT),",");
//			
//		}
		
		try {
			BWConnect();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			logger.error ("Erreur lors de la connexion : "+e.getMessage() );
		}
	
		
		super.init();

	}


	/**
	 * V�rification des param�tres
	 * 
	 * @param param
	 * @return
	 */

	private static int checkParams(HashMap<String, String> param) {
		String temp = null;
		// Check mod�le
		temp = param.get(MODELE);
		if (temp == null || "".equals(temp))
			return 1;

		// Check du domaine
		temp = param.get(DOMAINE);
		if (temp == null || "".equals(temp))
			return 2;

		// Check du flux
		temp = param.get(FLUX);
		if (temp == null || "".equals(temp))
			return 3;

		if (!temp.equals("RTF") && !temp.equals("PDF"))
			return 6;

		return 0;
	}

	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {

		logger.info("Requ�te GET : Client = " + request.getRemoteAddr()	+ " - Requ�te : " + request.getQueryString());
		response.getOutputStream().write("<html><head>		<meta http-equiv='Content-Type' content='text/html; charset=ISO-8859-1'><title>Formulaire de g�n�ration de document</title>	</head><body>	<form method='POST' action='GTBdocWeb'>			<input type='text' name='D' value='THELEM_HORSPRODUCTION'>domaine</input><br/>		<input type='text' name='M' value='I_MODELE_DEMONSTRATION'>mod�le</input><br/>		<input type='text' name='F'>flux</input><br/>		<input type='submit' name='submit'>assembler</input><br/></form></body></html>".getBytes());

	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		
		logger.debug("Requ�te POST : Client = " + request.getRemoteAddr() + " - Requ�te : " + request.getQueryString());

		// R�cup�ration des variable du POST
		// [domain="",modele="",flux="" ...]

		domaine = request.getParameter("D");
		template = request.getParameter("M");
		flux = request.getParameter("F").getBytes();
		

		try{
			int result = 0;

			// R�cup�ration du flux data
//			if (data==null || data.length()==0 ) {throw new Exception("Erreur 0 : "+Errolib.get(0));}
//			HashMap<String, String> param = null;
						
			byte[] document = BdocGenerate();

			response.setContentLength(document.length);
			response.setContentType("application/pdf");
			response.getOutputStream().write(document);
			
		} catch (Throwable t) {
			logger.error("Erreur : "+t.getMessage());
			response.sendError(500 , "Erreur : "+t.getMessage());
		}
	}


	/**
	 * G�n�ration mod�les de document + merge des fonds de page
	 * 
	 * @param logger
	 * @param xml
	 * @param document
	 * @param pdf
	 * @throws Throwable
	 */
	private byte[] BdocGenerate()
			throws Throwable {

		// Properties HPreprint = new Properties();

		try {
			// Si On n'est pas d�j� connect� � BdocWeb alors on se connecte
			BWConnect();
			
			// S�lection du mod�le
			JBTemplate ewtemplate = (JBTemplate) ewapp.getDomain(domaine).getTemplateByLogicalName(template);
			this.ewGeneration = ewapp.setTemplateToGenerate(SESSION_ID,  ewtemplate);

			// Pr�paration de l'envoi du flux de donn�es par http + moteur PDF
			
						
			GenerationOptions options = getGenenerationOptions();
			ewGeneration.setOptions(options);

			// Composition du document
			logger.info("d�marrage de la composition: template="+ ewtemplate.getName());
			
			// V�rification de la validit� du mod�le
			if (ewtemplate.isInteractive())
				throw new Exception("Le mod�le " + ewtemplate.getName()	+ " est interactif et ne peut �tre trait�");
			if (!ewtemplate.isValid())
				throw new Exception("Le mod�le " + ewtemplate.getName()	+ " n'est pas valide");
			
			// Composition effective 
			Long datestart = System.currentTimeMillis();
			BDocDocument bd = ewGeneration.generateToBDocDocument();
			byte[] ewResult = bd.getDocument();
		
			
			if (ewResult == null) throw new Exception("Document "+domaine+"."+template+" : retour null de BdocWeb");
			if (ewResult.length == 0) throw new Exception("Document "+domaine+"."+template+" de taille nul");

			logger.debug("fin composition  en : "
					+ nf.format(System.currentTimeMillis() - datestart)
					+ " ms, Taille (o): " + ewResult.length);
			
			
			// Retour du document
			if (Boolean.valueOf(getInitParameter("ResultBase64"))){
				// Encodage Base64 du document 
				logger.debug("Encodage Base 64 mode Chunk : " + String.valueOf(Boolean.valueOf(getInitParameter("base64Chunked"))));
				return Base64.encodeBase64(ewResult, Boolean.valueOf(getInitParameter("base64Chunked")));
			}
			else{
				logger.debug("Retour du document sans encodage Base 64 : ");
				return ewResult;
			}
							
		} catch (Throwable t) {
					throw new Exception(
					"Erreur lors de la composition du document : template="
							+ domaine+"."+template + ", Error = "
							+ t.getLocalizedMessage());
		}
	}

	/**
	 * 
	 * Retourne un objet BDocGenerationOption contenant les options d'assemblage
	 * 
	 * @param Filename
	 * @param document
	 * @return BDocGenerationOption
	 * @throws Throwable
	 */
	private GenerationOptions getGenenerationOptions() throws Throwable {

				
		GenerationOptions genOption = new GenerationOptions();

		genOption.setProductionFormat(GenerationOptions.FORMAT_PDF);
		
		genOption.setAuthor("Sample author");
		genOption.setSubject("Sample subject ");
		genOption.setTitle("Sample title");
		
		
		// S�lection du mode flow
		//genOption.s.setFlowMode(true);

		// Transformation Base64 --> Flux
		byte[] dataflow = null;
		
		if (Boolean.valueOf(getInitParameter("FluxBase64"))){
		try {
			logger.debug("D�codage du flux base 64");
			dataflow = Base64.decodeBase64(flux);
		} catch (Exception e) {
			throw new Exception("Erreur 10 : " + Errolib.get(10));
		}
		}else{
			logger.debug("r�cup�ration du flux sans d�codage base64");
			dataflow = flux;			
		}

		// Envoi du flux vers BdocWeb
		try {
			logger.debug("Envoi du flux");
			this.ewGeneration.setDataStream(dataflow);
			logger.debug("Fin d'envoi du flux");
		} catch (Exception e) {
			throw new Exception("Erreur 8 : " + Errolib.get(8) + " : "+ e.getLocalizedMessage());
		}

		java.util.Properties properties = new Properties();
		properties.setProperty("CheminFDP", CheminFDP);
		
		//genOption.addTask("1", "com.bdoc.specifique.AjoutFDPDynamiquePageAPage", properties);
		
		
		return genOption;
	}

	private void BWConnect() throws Throwable {
		ewapp = BdocWebConnection.getInstance().getJBApplication(BWServer,BWlogin, BWpassword, BWport);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		try {
			BdocWebConnection.getInstance().dispose();
		} catch (BDocException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.destroy();
	}
	
}