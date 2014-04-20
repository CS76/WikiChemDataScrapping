/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.WikiChemDataScrapping.Tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openscience.WikiChemDataScrapping.Utilities.GeneralUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Java class to retrieve the list of chemical or drug data deposited in WIKIPEDIA
 * It recursively checks for the chemical/ drug data and if found saves it the repo.
 * data stored contains the Name, PageID, Chemical/Drug data (ChemBox/ Drug Box Details)
 * @author CS76
 */
public class WikiChemDataScrapper {

    public static void main(String[] args) throws Exception {
        String rootCategory = "Organic compounds";
        WikiChemDataScrapper wcds = new WikiChemDataScrapper();
        wcds.recursiveDataFetcher(rootCategory);
    }

    /**
     * Recursively Checks for the Chemical or Drug data and if found stores to a repo
     * @param query
     * @throws Exception
     */
    private void recursiveDataFetcher(String query) throws Exception {
        // getList of SubCategories in the give query category
        // ----> handles even if the list of subcategories are greater than 500.
        List<String> subCatList = this.querySubCat(query, null, "");
        if (subCatList.size() > 0) {
            System.out.println("SubCategories :: " + subCatList.size() + " ==== " + query);
            for (String subCategory : subCatList) {
                recursiveDataFetcher(subCategory);
            }
        }
        // getList of Pages in the give query category
        // ----> handles even if the list of pages are greater than 500.
        List<String> pages = this.queryPage(query, null, "");
        if (pages.size() > 0) {
            System.out.println("Pages :: " + pages.size() + " ==== " + query);
            // loop through each page and find out if it belong to a chemical or drug category
            // --> if TRUE -> append to the repo
            for (String p : pages) {
                List<String> chemData = this.fetchChemCompDetails(p);
                if (chemData.size() > 0) {
                    StringBuilder sbn = new StringBuilder();
                    for (String data : chemData) {
                        sbn.append(data).append("\t");
                    }
                    sbn.append("\n");
                    // append data to a text file (tab delimited)
                    GeneralUtility.appendToFile(sbn.toString(), "C:\\Users\\CS76\\Desktop\\dataWiki.txt");
                }
            }
        }
    }

    /**
     * Gets a list of pages in the give Query
     * @param catgeoryQuery
     * @param pagesList
     * @param cmCont
     * @return
     */
    private List<String> queryPage(String catgeoryQuery, List<String> pagesList, String cmCont) throws Exception {
        String action = "query";
        String list = "categorymembers";
        String searchTerm = catgeoryQuery.replace("Category:", "").replace(" ", "%20");
        String cmtitle = "Category:" + searchTerm;
        String cmlimit = "max";
        String cmtype = "page";
        String format = "xml";
        String cmprop = "type|title";
        String pageCmCont = "";

        Boolean cmContinueStatus = false;

        List<String> pageResults = new ArrayList<String>();
        String URLString = "";


        // checks if the query if for cmcontinue or a direct query and assigns the purticular url to URLString
        // intializes the corresponding variables accordingly
        if (pagesList == null && cmCont == "") {
            URLString = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format;
        } else if ((pagesList != null && cmCont != "")) {
            pageResults = pagesList;
            URLString = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format + "&cmcontinue=" + cmCont;
        }

        StringBuilder sb = new StringBuilder();
        try {
            URL wikiURL = new URL(URLString);
            URLConnection wikic = wikiURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(wikic.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Error in querying wiki:" + e.getMessage());
            return null;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        NodeList nList = null, cmContM = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = loadXMLFromString(sb.toString());
            doc.getDocumentElement().normalize();

            nList = doc.getElementsByTagName("cm");

            cmContM = doc.getElementsByTagName("query-continue");
            if (cmContM.getLength() > 0) {
                cmContinueStatus = true;
                pageCmCont = (cmContM.item(0).getFirstChild().getAttributes().getNamedItem("cmcontinue").getTextContent());
            }
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                pageResults.add(eElement.getAttribute("title"));
            }
        }
        if (cmContinueStatus) {
            pageResults = queryPage(catgeoryQuery, pageResults, pageCmCont);
        }
        return pageResults;
    }
    
    /**
     * Gets a list of subCategories in the give Query
     * @param queryString1
     * @param catList
     * @param cmCatCont
     * @return
     * @throws Exception 
     */
    private List<String> querySubCat(String catgeoryQuery, List<String> catList, String cmCatCont) throws Exception {
        String action = "query";
        String list = "categorymembers";
        String searchTerm = catgeoryQuery.replace("Category:", "").replace(" ", "%20");
        String cmtitle = "Category:" + searchTerm;
        String cmlimit = "max";
        String cmtype = "subcat";
        String format = "xml";
        String cmprop = "type|title";
        String URLQuery = "";
        String catCmCont = "";

        Boolean cmContinue = false;
        List<String> subCatResults = new ArrayList<String>();

        if (catList == null && cmCatCont == "") {
            URLQuery = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format;
        } else if ((catList != null && cmCatCont != "")) {
            subCatResults = catList;
            URLQuery = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format + "&cmcontinue=" + cmCatCont;
        }

        StringBuilder sbc = new StringBuilder();
        try {
            URL wiki = new URL(URLQuery);
            URLConnection wikic = wiki.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(wikic.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sbc.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Error in querying wiki:" + e.getMessage());
            return null;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        NodeList nList = null, cmContM = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = loadXMLFromString(sbc.toString());
            doc.getDocumentElement().normalize();
            nList = doc.getElementsByTagName("cm");

            cmContM = doc.getElementsByTagName("query-continue");
            if (cmContM.getLength() > 0) {
                cmContinue = true;
                catCmCont = (cmContM.item(0).getFirstChild().getAttributes().getNamedItem("cmcontinue").getTextContent());
            }
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                subCatResults.add(eElement.getAttribute("title"));
            }
        }

        if (cmContinue) {
            subCatResults = queryPage(catgeoryQuery, subCatResults, catCmCont);
        }
        return subCatResults;
    }
   
    /**
     * Gets a data from a query Page if it belongs to chemical or drug category else returns a null List
     * @param queryString1
     * @return
     * @throws Exception 
     */
    private List<String> fetchChemCompDetails(String pageQuery) throws Exception {
        System.out.println(pageQuery);
        String action = "query";
        String searchTerm = pageQuery.replace(" ", "%20");
        String titles = "API|" + searchTerm;
        String prop = "revisions";
        String rvprop = "timestamp|user|comment|content";
        String format = "xml";
        String URLQuery = "http://en.wikipedia.org/w/api.php?action=" + action + "&prop=" + prop + "&titles=" + titles + "&rvprop=" + rvprop + "&format=" + format;
        StringBuilder sb = new StringBuilder();
        try {
            URL wiki = new URL(URLQuery);
            URLConnection wikic = wiki.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(wikic.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Error in querying wiki:" + e.getMessage());
            return null;
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        NodeList nList = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = loadXMLFromString(sb.toString());
            doc.getDocumentElement().normalize();
            nList = doc.getElementsByTagName("page");
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> ChemCompoundData = new ArrayList<String>();
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("title").equalsIgnoreCase(pageQuery)) {
                    String content = eElement.getElementsByTagName("rev").item(0).getTextContent();
                    String mBoxData = splitString(content, "'''" + pageQuery + "'''").get(0);
                    if (mBoxData.contains("drugbox") || mBoxData.contains("chembox") || mBoxData.contains("Drugbox") || mBoxData.contains("Chembox")) {
                        ChemCompoundData.add(pageQuery);
                        ChemCompoundData.add(eElement.getAttribute("pageid"));
                        ChemCompoundData.add(mBoxData);
                    }
                }
            }
        }
        return ChemCompoundData;
    }
    
    /**
     * Splits the word using the given split String and return back the list of strings
     * @param word
     * @param splitWord
     * @return 
     */
    public List<String> splitString(String word, String splitWord) {
        String[] wordArray = word.split(splitWord);
        List<String> wordList = Arrays.asList(wordArray);
        return wordList;
    }
    
    /**
     * returns document from the xml string
     * @param xml
     * @return
     * @throws Exception 
     */
    public Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}