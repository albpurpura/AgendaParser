import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 author = Alberto Purpura
 Copyright 2018 University of Padua, Italy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        HashMap<String, Integer> dups = new HashMap<>();
        File file = new File("test/Original");
        ArrayList<String> fileNames = new ArrayList<>();
        list(file, fileNames, dups, "csv");

        for (String f : fileNames) {
            //add missing rows to files

            matchRowsOriginalAndCheckedFile("test/Original/" + f + ".csv", "test/CHECKED_DESCRIPTION/" + f + ".csv", "prefix_" + f + ".csv");
            //process files
            processFile("prefix" + f + ".csv", "");
        }
        System.exit(0);

    }//main

    public static void countDifferences(String source, String target) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(source));
        List<String[]> linesOriginal = reader.readAll();
        reader = new CSVReader(new FileReader(target));
        List<String[]> linesNew = reader.readAll();
        int cnt = 0;
        for (String[] nLine : linesNew) {
            boolean found = false;
            for (String[] oLine : linesOriginal) {
                if (oLine[0].equalsIgnoreCase(nLine[0])
                        && oLine[1].equalsIgnoreCase(nLine[1])
                        && oLine[2].equalsIgnoreCase(nLine[2])
                        && oLine[3].equalsIgnoreCase(nLine[3])
                        && oLine[4].equalsIgnoreCase(nLine[4])
                        && oLine[5].equalsIgnoreCase(nLine[5])
                        && oLine[6].equalsIgnoreCase(nLine[6])
                        ) {
                    found = true;
                }
            }
            if (!found) {
                cnt++;
            }
        }
        System.out.println(cnt + " lines newly processed in file " + target + " respect to " + source);
    }

    public static void matchRowsOriginalAndCheckedFile(String original, String newFile, String outputFile) throws
            IOException {
        CSVReader reader = new CSVReader(new FileReader(original));
        List<String[]> linesOriginal = reader.readAll();
        reader = new CSVReader(new FileReader(newFile));
        List<String[]> linesNew = reader.readAll();
        int removed = 0;
        int initialSize = linesNew.size();
        //remove duplicates from list
        for (int i = 0; i < linesNew.size(); i++) {
            String[] source = linesNew.get(i);
            for (int j = i + 1; j < linesNew.size(); j++) {
                String[] candidateDup = linesNew.get(j);
                boolean duplicate = true;
                for (int k = 0; k < candidateDup.length; k++) {
                    if (candidateDup[k] == null || source.length <= k) {
                        duplicate = false;
                        break;
                    }
                    if (!candidateDup[k].equalsIgnoreCase(source[k])) {
                        duplicate = false;
                        break;
                    }
                }
                if (duplicate) {
                    removed++;
                    linesNew.remove(j);
                    j--;
                }
            }
        }
        System.out.println(removed + " removed of " + initialSize);


        CSVWriter w = new CSVWriter(new FileWriter(outputFile));
        ArrayList<String[]> newLines = new ArrayList<>();
        int cnt = 0;
        //select the lines from the ORIGINAL file that are not found in the new file and print them
        for (String[] oLine : linesOriginal) {
            boolean found = false;
            for (String[] nLine : linesNew) {
                if (oLine[0].equalsIgnoreCase(nLine[0])
                        && oLine[1].equalsIgnoreCase(nLine[1])
                        && oLine[2].equalsIgnoreCase(nLine[2])
                        && oLine[3].equalsIgnoreCase(nLine[3])
                        && getMinDistance(oLine[4], nLine[4]) <= 40) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                cnt++;
                String[] lineToPrint = new String[oLine.length + 2];
                lineToPrint[0] = oLine[0];
                lineToPrint[1] = oLine[1];
                lineToPrint[2] = oLine[2];
                lineToPrint[3] = oLine[3];
                lineToPrint[4] = oLine[4];//descrizione
                //System.out.println(oLine[4]);
                newLines.add(lineToPrint);
            }
        }
        //print the rest of the lines that were already processed by the students
        linesNew.remove(0);
        newLines.addAll(linesNew);
        w.writeAll(newLines);
        w.close();
        System.out.println(cnt + " lines added to: " + newFile.toString());
    }

    private static void checkIfAllRowsArePresent(String original, String newFile) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(original));
        List<String[]> linesOriginal = reader.readAll();
        reader = new CSVReader(new FileReader(original));
        List<String[]> linesNew = reader.readAll();

        for (String[] oLine : linesOriginal) {
            boolean found = false;
            for (String[] nLine : linesNew) {
                if (oLine[0].equalsIgnoreCase(nLine[0])
                        && oLine[1].equalsIgnoreCase(nLine[1])
                        && oLine[2].equalsIgnoreCase(nLine[2])
                        && oLine[3].equalsIgnoreCase(nLine[3])
                        && oLine[4].equalsIgnoreCase(nLine[4])) {
                    found = true;
                }
            }
            if (!found) {
                System.out.println("riga: " + oLine[0] + " " + oLine[1] + " " + oLine[2] + " " + oLine[3] + " non " +
                        "trovata!");
            }
        }
    }

    private static void processFile(String filename, String inputFolder) throws IOException {
        String inputFilePath = inputFolder.length() > 0 ? inputFolder + File.separator + filename : filename;
        String outputFilePath = "PROCESSED_" + filename;
        String notNamesList = "data/notNames.txt";
        String beginTitleFilePath = "data/possibleRoleStart.txt";
        ArrayList<String> notNames = loadList(notNamesList);
        ArrayList<String> possibleRoleStart = loadList(beginTitleFilePath);

        CSVReader reader = new CSVReader(new FileReader(inputFilePath));
        List<String[]> lines = reader.readAll();
        List<String[]> newLines = new ArrayList<>(lines.size());
        lines.remove(0);
        int nameFieldIndex = 5;
        int roleIndex = 6;
        int descriptionFieldIndex = 4;
        boolean foundSomething;
        int cnt = 0;
        for (String[] line : lines) {
            cnt++;
            //line: data diario, luogo città, luogo nazione, luogo specifico, descrizione
            //newLine: data diario, luogo città, luogo nazione, luogo specifico, descrizione, nome, ruolo
            // se trovo piu nomi nel campo descrizione allora raddoppio la riga.
            //extract fields from description field
            String description = line[descriptionFieldIndex];


            String[] tokens = description.split(" ");
            foundSomething = false;
            ArrayList<String> names = getNames(tokens, notNames);
            ArrayList<String> roles = getRoles(tokens, notNames, possibleRoleStart);

            for (int i = 0; i < names.size(); i++) {
                foundSomething = true;
                String name = names.get(i);
                if (roles.size() > 0 && i < roles.size()) { //match the role in the same order to the name
                    String role = roles.get(i);
                    String[] newLine = new String[line.length + 2];
                    System.arraycopy(line, 0, newLine, 0, descriptionFieldIndex + 1);
                    newLine[nameFieldIndex] = name;
                    newLine[roleIndex] = role;
                    newLines.add(newLine);

                } else {//if there are more names than roles or no roles: if (roles.size() > 0 && i >= roles.size())
                    String[] newLine = new String[line.length + 2];
                    System.arraycopy(line, 0, newLine, 0, descriptionFieldIndex + 1);
                    newLine[nameFieldIndex] = name;
                    newLine[roleIndex] = "";
                    newLines.add(newLine);
                }
            }
            //if no name was detected
            if (!foundSomething) {
                String[] newLine = new String[line.length + 2];
                System.arraycopy(line, 0, newLine, 0, descriptionFieldIndex + 1);
                newLine[nameFieldIndex] = "";
                newLines.add(newLine);
            }
        } //iterate through each line in the document

        System.out.println(cnt + " lines processed");
        //print new list
        PrintWriter writer = new PrintWriter(outputFilePath);
        CSVWriter csvWriter = new CSVWriter(writer);
        newLines.add(0, new String[]

                {
                        "Data Diario", "Luogo Città", "Luogo Nazione", "Luogo Specifico", "Descrizione",
                        "Attore", "Ruolo"
                });

        csvWriter.writeAll(newLines);
        csvWriter.close();

    }

    private static ArrayList<String> getRoles(String[] tokens, ArrayList<String> notNamesList, ArrayList<String>
            beginRolesTokens) {
        ArrayList<String> roles = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String role = "";

            //recognize a role from the first word
            if (beginRolesTokens.contains(token)) {
                role = "";
                role += token;
                for (int j = i + 1; j < tokens.length; j++) {
                    String next = tokens[j];
                    if (next.equals("e") || next.contains(",") || next.contains(":")) {
                        if (next.contains(",") && (isAllUpper(next.replace(",", "")) && !notNamesList.contains(next
                                .replace(",", "")))) {
                            break;
                        } else if (next.contains(",")) {
                            role = role + " " + next.replace(",", "");
                            break;
                        }
                        if (next.contains(":") && (isAllUpper(next.replace(":", "")) && !notNamesList.contains(next
                                .replace(":", "")))) {
                            break;
                        } else if (next.contains(":")) {
                            role = role + " " + next.replace(":", "");
                            break;
                        }
                        //if it's an "e" (conjunction)
                        break;
                    }
                    //if ends with a punctuation sign, remove it
                    if (next.length() > 0) {
                        if ((next.charAt(next.length() - 1) + "").matches("\\p{Punct}")) {
                            next = next.replace("\\p{Punct}", "");
                        }
                    }
                    role = role + " " + next;
                }
            }
            if (role.length() > 0 && !role.equalsIgnoreCase("vice") && !role.equalsIgnoreCase("ex")) {
                String[] splittedCandidate = role.split(" ");
                boolean canAdd = true;
                if (!splittedCandidate[0].equalsIgnoreCase("ex")) {
                    //compare with previously added roles in order to avoid the case: "ex Generale" (already added) and
                    // "Generale" (to add)
                    for (String addedRole : roles) {
                        String[] addedSplitted = addedRole.split(" ");
                        if (addedSplitted.length >= 2) {
                            if ((beginRolesTokens.contains(addedSplitted[0]))
                                    && addedSplitted[1].equalsIgnoreCase
                                    (splittedCandidate[0])) {
                                canAdd = false;
                                break;
                            }
                        }
                    }
                }
                if (!roles.contains(role) && canAdd) {
                    roles.add(role);
                }
            }

        }
        return roles;
    }

    /**
     * Recognizes and returns in an arraylist all the names found in the array of tokens that represents the
     * description field
     *
     * @param tokens       list of tokens (ordered)
     * @param notNamesList auxiliary list of tokens that are all uppercase but are not surnames
     * @return an arraylist (ordered) with the list of recognized names
     */
    private static ArrayList<String> getNames(String[] tokens, ArrayList<String> notNamesList) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            token = token.replace(",", "");
            String recognizedName = "";
            if (isAllUpper(token) && !(token.matches("([A-Z]\\.)+")) && !notNamesList.contains(token) &&
                    (i + 1 >= tokens.length || (!isAllUpper(tokens[i + 1]) && !(token.matches("([A-Z]\\.)+"))))) {
                //if ends with a punctuation sign, remove it
                if ((token.charAt(token.length() - 1) + "").matches("\\p{Punct}")) {
                    token = token.replace("\\p{Punct}", "");
                }
                recognizedName = token;
                for (int j = i - 1; j >= 0; j--) {
                    String currT = tokens[j];
                    if (currT.length() > 0) {
                        //if the previous token has a capital letter and does not contain a comma
                        if (Character.isUpperCase(currT.charAt(0)) && !currT.contains(",")) { // && !isAllUpper(currT)
                            //recognized as NAME
                            recognizedName = tokens[j] + " " + recognizedName;
                        } else {
                            break;
                        }
                    }
                }
            }//surname recognized
            if (recognizedName.length() > 0) {
                names.add(recognizedName);
            }
        }
        return names;
    }

    /**
     * Loads text file with one word per line into an arraylist of strings
     *
     * @param input input file path
     * @return An arraylist of strings with all the values in the text file
     * @throws IOException
     */
    private static ArrayList<String> loadList(String input) throws IOException {
        ArrayList<String> retval = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line = reader.readLine();
        while (line != null) {
            retval.add(line);
            line = reader.readLine();
        }
        return retval;
    }

    private static boolean isAllUpper(String s) {
        String whitespace_chars = ""       /* dummy empty string for homogeneity */
                + "\\u0009" // CHARACTER TABULATION
                + "\\u000A" // LINE FEED (LF)
                + "\\u000B" // LINE TABULATION
                + "\\u000C" // FORM FEED (FF)
                + "\\u000D" // CARRIAGE RETURN (CR)
                + "\\u0020" // SPACE
                + "\\u0085" // NEXT LINE (NEL)
                + "\\u00A0" // NO-BREAK SPACE
                + "\\u1680" // OGHAM SPACE MARK
                + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
                + "\\u2000" // EN QUAD
                + "\\u2001" // EM QUAD
                + "\\u2002" // EN SPACE
                + "\\u2003" // EM SPACE
                + "\\u2004" // THREE-PER-EM SPACE
                + "\\u2005" // FOUR-PER-EM SPACE
                + "\\u2006" // SIX-PER-EM SPACE
                + "\\u2007" // FIGURE SPACE
                + "\\u2008" // PUNCTUATION SPACE
                + "\\u2009" // THIN SPACE
                + "\\u200A" // HAIR SPACE
                + "\\u2028" // LINE SEPARATOR
                + "\\u2029" // PARAGRAPH SEPARATOR
                + "\\u202F" // NARROW NO-BREAK SPACE
                + "\\u205F" // MEDIUM MATHEMATICAL SPACE
                + "\\u3000" // IDEOGRAPHIC SPACE
                ;
        if (s.matches("[" + whitespace_chars + "]+") || s.equals("")) {
            return false;
        }
        if (s.length() == 1 && !Character.isLetter(s.toCharArray()[0])) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if ((Character.isLetter(c) && Character.isLowerCase(c)) || Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static void list(File file, ArrayList<String> retVal, HashMap<String, Integer> duplicates, String
            extension) {
        File[] children = file.listFiles();
        if (null != children) {
            for (File child : children) {
                list(child, retVal, duplicates, extension);
                if (child.getName().contains(extension) && !child.toString().contains(".DS_Store")) {
                    String fileNameNoExtension = child.getName().substring(0, child.getName().lastIndexOf('.'));
                    if (retVal.contains(fileNameNoExtension)) {
                        if (duplicates.containsKey(fileNameNoExtension)) {
                            duplicates.put(fileNameNoExtension, duplicates.get(fileNameNoExtension) + 1);
                        } else {
                            duplicates.put(fileNameNoExtension, 1);
                        }

                    } else {
                        retVal.add(fileNameNoExtension);
                    }
                }
            }
        }
    }

    private static int getMinDistance(String target, String source) {
        int n = target.length();
        int m = source.length();
        char[] targetChars = target.toCharArray();
        char[] sourceChars = source.toCharArray();
        int[][] distance = new int[n + 1][m + 1];
        int i;
        int j;
        distance[0][0] = 0;
        for (i = 1; i <= n; i++) {
            distance[i][0] = distance[i - 1][0] + 1;
        }

        for (j = 1; j <= m; j++) {
            distance[0][j] = distance[0][j - 1] + 1;
        }
        for (i = 1; i <= n; i++) {
            for (j = 1; j <= m; j++) {
                int v1 = distance[i - 1][j] + 1;
                int v2 = distance[i - 1][j - 1] + subCost(targetChars[i - 1], sourceChars[j - 1]);
                int v3 = distance[i][j - 1] + 1;
                distance[i][j] = Math.min(v1, Math.min(v2, v3));
            }
        }
        return distance[n][m];
    }

    private static int subCost(char t, char s) {
        if (s == t) {
            return 0;
        }
        return 2;
    }
}
