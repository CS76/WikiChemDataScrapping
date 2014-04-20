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
 *
 * @author CS76
 */
public class WikiChemDataScrapper {

    public static void main(String[] args) throws Exception {
        WikiChemDataScrapper wcds = new WikiChemDataScrapper();
        wcds.recDataFetcher("Alcohols");
    }

    private void recDataFetcher(String query) throws Exception {
//        System.out.println("CAtegory:::" + query);
//        List<String> subCat = this.querySubCat(query);
//        if (subCat.size() > 0) {
//            for (String s : subCat) {
//                recDataFetcher(s);
//            }
//        }
        List<String> pages = this.queryPage(query, null, "");
        System.out.println(pages);
        if (pages.size() > 0) {
            System.out.println(pages.size() + " ==== " + query);
            for (String p : pages) {
                List<String> chemData = this.fetchChemCompDetails(p);
                if (chemData.size() > 0) {
                    StringBuilder sbn = new StringBuilder();
                    for (String data : chemData) {
                        // System.out.println("Page:: " + data);
                        sbn.append(data).append("\t");
                    }
                    sbn.append("\n");
                    GeneralUtility.appendToFile(sbn.toString(), "C:\\Users\\CS76\\Desktop\\dataWiki.txt");
                }
            }
        }
    }

    private List<String> queryPage(String queryString1, List<String> pagesList, String cmCont) {
        String action = "query";
        String list = "categorymembers";
        String searchTerm = queryString1.replace("Category:", "").replace(" ", "%20");
        String cmtitle = "Category:" + searchTerm;
        String cmlimit = "max";
        String cmtype = "page";
        String format = "xml";
        String cmprop = "type|title";
        String qc = "";
        Boolean cmContinue = false;
        List<String> pageResults = new ArrayList<String>();
        String URLQuery = "";
        if (pagesList == null && cmCont == "") {
            URLQuery = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format;
        } else if ((pagesList != null && cmCont != "")) {
            pageResults = pagesList;
            URLQuery = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format + "&cmcontinue=" + cmCont;
        }
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
        NodeList nList = null, cmContM = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(sb.toString().getBytes("utf-8"))));
            doc.getDocumentElement().normalize();
            nList = doc.getElementsByTagName("cm");

            cmContM = doc.getElementsByTagName("query-continue");
            if (cmContM.getLength() > 0) {
                cmContinue = true;
                qc = (cmContM.item(0).getFirstChild().getAttributes().getNamedItem("cmcontinue").getTextContent());
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
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
        if (cmContinue) {
           pageResults = queryPage(queryString1, pageResults,qc);
        }
        return pageResults;
    }

    private List<String> querySubCat(String queryString1) throws Exception {
        String action = "query";
        String list = "categorymembers";
        String searchTerm = queryString1.replace("Category:", "").replace(" ", "%20");
        String cmtitle = "Category:" + searchTerm;
        String cmlimit = "max";
        String cmtype = "subcat";
        String format = "xml";
        String cmprop = "type|title";
        String URLQuery = "http://en.wikipedia.org/w/api.php?action=" + action + "&list=" + list + "&cmtitle=" + cmtitle + "&cmlimit=" + cmlimit + "&cmtype=" + cmtype + "&cmprop=" + cmprop + "&format=" + format;

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
        NodeList nList = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = loadXMLFromString(sbc.toString());
            doc.getDocumentElement().normalize();
            nList = doc.getElementsByTagName("cm");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> subCatResults = new ArrayList<String>();

        for (int i = 0; i < nList.getLength(); i++) {

            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                subCatResults.add(eElement.getAttribute("title"));
            }
        }
        return subCatResults;
    }

    private List<String> fetchChemCompDetails(String queryString1) throws Exception {
        System.out.println(queryString1);
        String action = "query";
        String searchTerm = queryString1.replace(" ", "%20");
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
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiChemDataScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> ChemCompoundData = new ArrayList<String>();
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("title").equalsIgnoreCase(queryString1)) {
                    String content = eElement.getElementsByTagName("rev").item(0).getTextContent();
                    String mBoxData = splitString(content, "'''" + queryString1 + "'''").get(0);
                    if (mBoxData.contains("drugbox") || mBoxData.contains("chembox") || mBoxData.contains("Drugbox") || mBoxData.contains("Chembox")) {
                        ChemCompoundData.add(queryString1);
                        ChemCompoundData.add(eElement.getAttribute("pageid"));
                        ChemCompoundData.add(mBoxData);
                    }
                }
            }
        }
        return ChemCompoundData;
    }

    public List<String> splitString(String word, String splitWord) {
        String[] wordArray = word.split(splitWord);
        List<String> wordList = Arrays.asList(wordArray);
        return wordList;
    }

    public Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}