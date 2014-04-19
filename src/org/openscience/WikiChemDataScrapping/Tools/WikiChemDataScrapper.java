/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.WikiChemDataScrapping.Tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author CS76
 */
public class WikiChemDataScrapper {

    public static void main(String[] args) throws Exception {
        URL yahoo = new URL("http://en.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:Alcohols&cmlimit=max&cmtype=page&format=xml");
        URLConnection yc = yahoo.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        
        System.out.println(sb.toString());
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(sb.toString().getBytes("utf-8"))));
        doc.getDocumentElement().normalize();
        
        NodeList nList = doc.getElementsByTagName("cm");
        System.out.println(nList.getLength());
        
    }
}
