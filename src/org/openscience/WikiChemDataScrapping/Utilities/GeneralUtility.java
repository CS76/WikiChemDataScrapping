/* 
 * Copyright (C) 2013 Chandrasekkhar < mailcs76[at]gmail.com / www.cs76.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openscience.WikiChemDataScrapping.Utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.MACCSFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.similarity.Tanimoto;

/**
 *
 * @author chandu
 */
public class GeneralUtility {

    /**
     *
     * @param min
     * @param max
     * @return
     */
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     *
     * @param min
     * @param max
     * @param size
     * @return
     */
    public static int[] randIntSet(int min, int max, int size) {
        Random rand = new Random();
        int[] randomNum = new int[size];
        for (int i = 0; i < size; i++) {
            randomNum[i] = rand.nextInt((max - min) + 1) + min;
        }
        return randomNum;
    }

    /**
     *
     * @param toConCat
     * @return
     */
    public static String conCat(int[] toConCat) {
        return conCat(toConCat, ",");
    }

    public static String conCat(String s1, String s2, String separator) {
        return (s1 + separator + s2);
    }

    /**
     *
     * @param toConCat
     * @param delimitor
     * @return
     */
    public static String conCat(int[] toConCat, String delimitor) {
        StringBuilder sb = new StringBuilder();
        for (int s : toConCat) {
            sb.append(String.valueOf(s)).append(delimitor);
        }
        return (sb.substring(0, sb.length() - 1));
    }

    /**
     *
     * @param ar
     * @return
     */
    public static int[] shuffleArray(int[] ar) {
        int[] arrayToShuffle = ar;
        Random rnd = new Random();
        for (int i = arrayToShuffle.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = arrayToShuffle[index];
            arrayToShuffle[index] = arrayToShuffle[i];
            arrayToShuffle[i] = a;
        }
        return arrayToShuffle;
    }

    /**
     *
     * @param bytes
     * @return
     */
    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        bits = BitSet.valueOf(bytes);
//        for (int i = 0; i < bytes.length * 8; i++) {
//            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
//                bits.set(i);
//            }
//        }
        return bits;
    }

    /**
     *
     * @param bits
     * @return
     */
    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    /**
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(String filePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String everyThing = "";
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            everyThing = sb.toString();
        } finally {
            br.close();
        }
        return everyThing;
    }

    /**
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> readLines(String filePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        List<String> lines = new ArrayList<String>();
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return lines;
    }

    /**
     *
     * @param contentToWrite
     * @param filePath
     * @throws CDKException
     * @throws IOException
     */
    public static void writeToTxtFile(String contentToWrite, String filePath) throws CDKException, IOException {
        try {
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(contentToWrite);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendToFile(String contentToWrite, String filePath) throws CDKException, IOException {
        try {
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(contentToWrite);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param d
     * @return
     */
    public static String format_Double(double d) {
        String procWord = Double.toString(d);
        if (procWord.length() > 8) {
            DecimalFormat df = new DecimalFormat("#.####");
            procWord = (df.format(d));
        }
        String tempWord = "";
        if (procWord.charAt(0) != '-') {
            procWord = " " + procWord;
        }
        if (procWord.length() <= 8) {
            for (int i = 0; i < 8 - procWord.length(); i++) {
                tempWord += 0;
            }
            procWord = procWord + tempWord;
        }
        return procWord;
    }

    /*
     * 
     */
    public static void deleteFile(String filePath) {
        File nfile = new File(filePath);
        if (nfile.delete()) {
            System.out.println("File deleted: " + filePath);
        } else {
            System.out.println("Cant delete" + filePath + " file");
        }
    }

    /**
     *
     * @param sourceFile
     * @param destinationFile
     */
    public static void copyFile(File sourceFile, File destinationFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            FileOutputStream fileOutputStream = new FileOutputStream(
                    destinationFile);

            int bufferSize;
            byte[] bufffer = new byte[512];
            while ((bufferSize = fileInputStream.read(bufffer)) > 0) {
                fileOutputStream.write(bufffer, 0, bufferSize);
            }
            fileInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int booleanToInt(String value) {
        if (value.equalsIgnoreCase("true")) {
            return 1;
        } else if (value.equalsIgnoreCase("false")) {
            return 0;
        } else {
            return 0;
        }
    }

    public static String arrayToString(int[] array) {
        String arrayString = "";
        for (int t = 0; t < array.length; t++) {
            if (t < array.length - 1) {
                arrayString += array[t] + ",";
            } else {
                arrayString += array[t];
            }
        }
        return arrayString;
    }

    public static String arrayToString(char[] array) {
        String arrayString = "";
        for (int t = 0; t < array.length; t++) {
            arrayString += array[t] + ",";
        }
        return arrayString;
    }

    public static String arrayToString(String[] array) {
        String arrayString = "";
        for (int t = 0; t < array.length; t++) {
            if (t < array.length - 1) {
                arrayString += array[t] + ",";
            } else {
                arrayString += array[t];
            }
        }
        return arrayString;
    }

    public static String arrayToString(double[] array,String separator) {
        String arrayString = "";
        for (int t = 0; t < array.length; t++) {
            arrayString += array[t] + separator;
        }
        return arrayString;
    }

    public static String twoDimArrayToString(double[][] twoDimArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < twoDimArray.length; i++) {
            for (int j = 0; j < twoDimArray[i].length; j++) {
                sb.append(twoDimArray[i][j]);
                if (j < twoDimArray.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String twoDimArrayToString(int[][] twoDimArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < twoDimArray.length; i++) {
            for (int j = 0; j < twoDimArray[i].length; j++) {
                sb.append(twoDimArray[i][j]).append(" ");
            }
            if (i < twoDimArray.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String twoDimArrayToString(String[][] twoDimArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < twoDimArray.length; i++) {
            for (int j = 0; j < twoDimArray[i].length; j++) {
                sb.append(twoDimArray[i][j]).append(" ");
            }
            if (i < twoDimArray.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String twoDimArrayToStringWithHeader(double[][] twoDimArray, String[] header) {
        StringBuilder sb = new StringBuilder();
        System.out.println(GeneralUtility.arrayToString(header));

        sb.append(",").append(GeneralUtility.arrayToString(header)).append("\n");
        for (int i = 0; i < twoDimArray.length; i++) {
            sb.append(header[i]).append(",");
            for (int j = 0; j < twoDimArray[i].length; j++) {
                sb.append(twoDimArray[i][j]).append(",");
            }
            if (i < twoDimArray.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static boolean isAllElementsEqual(String[] arraytoCheck) {
        String initial = arraytoCheck[0];
        boolean isEqual = true;
        for (int i = 0; i < arraytoCheck.length; i++) {
            if (!arraytoCheck[i].equalsIgnoreCase(initial)) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    public static double getAverage(String[] propArray) {
        double total = 0.0;
        for (int i = 0; i < propArray.length; i++) {
            total += Double.valueOf(propArray[i]);
        }
        return total / propArray.length;
    }

    public static String getStringFromList(List<String> toWrite) {
        String stringToWrite = "";
        for (String s : toWrite) {
            stringToWrite = stringToWrite + s + "\n";
        }
        return stringToWrite;
    }

    public static boolean hasNext() {
        return false;
    }

    public static boolean isPolymer(String sdfFilePath) throws FileNotFoundException, IOException {
        List<String> lines = GeneralUtility.readLines(sdfFilePath);
        for (String s : lines) {
            if (s.contains("M  STY")) {
                return true;
            }
        }
        return false;
    }

    public static void extractPolymer(String sdfPath, String polymerSDFPath) throws FileNotFoundException, IOException, CDKException {
        BufferedReader br = new BufferedReader(new FileReader(sdfPath));
        String line = br.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            if (line.contains("$$$$")) {
                sb.append(line).append("\n");
                if (sb.toString().contains("M  STY")) {
                    GeneralUtility.appendToFile(sb.toString(), polymerSDFPath);
                }
                sb = new StringBuilder();
            } else {
                sb.append(line).append("\n");
            }
            line = br.readLine();
        }
    }

    public static int getRowCount(String filePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        int count = 0;
        try {
            String line = br.readLine();
            while (line != null) {
                count += 1;
                line = br.readLine();
            }
        } finally {

            br.close();
        }
        return count;
    }

    public static double getSimilarity(String mol1, String mol2) throws CDKException {
        double similarity = 0.0;
        MACCSFingerprinter mfp = new MACCSFingerprinter();
        DecimalFormat df = new DecimalFormat("#.####");
        IAtomContainer molecule1 = ChemUtility.getIAtomContainerFromSmilesWAP(mol1);
        IAtomContainer molecule2 = ChemUtility.getIAtomContainerFromSmilesWAP(mol2);
        similarity = Double.valueOf(df.format(Tanimoto.calculate(mfp.getBitFingerprint(molecule1), mfp.getBitFingerprint(molecule2))));
        return similarity;
    }

    public static List<Map<Integer, String>> splitMap(Map<Integer, String> mp) {
        int length = mp.size();
        int count = 0;
        Map<Integer, String> mp1 = new HashMap<Integer, String>();
        Map<Integer, String> mp2 = new HashMap<Integer, String>();

        for (Map.Entry e : mp.entrySet()) {
            if (count <= length / 2) {
                mp1.put((Integer) e.getKey(), (String) e.getValue());
            } else {
                mp2.put((Integer) e.getKey(), (String) e.getValue());
            }
        }
        return Arrays.asList(mp1, mp2);
    }

    public static List<String> getAllFileNamesInFolder(String folderPath) {
        List<String> fileNames = new ArrayList<String>();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileNames.add(listOfFiles[i].getName());
            }
        }
        return fileNames;
    }
    public static List<String> getAllFolderNamesInFolder(String folderPath) {
        List<String> folderNames = new ArrayList<String>();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (!listOfFiles[i].isFile()) {
                folderNames.add(listOfFiles[i].getName());
            }
        }
        return folderNames;
    }

    public static List<String> getNonEmptyArrayList(String[] parentList) {
        List<String> list = new ArrayList<String>(Arrays.asList(parentList));
        list.removeAll(Arrays.asList("", null));
        return list;
    }
}