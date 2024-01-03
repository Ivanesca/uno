package vindur;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private final static String EMPTY_STRING = "\"\"";
    private final static String EMPTY_STRING_REGEXP = "(\"\";)*(\"\"){1}";
    private final static String LINE_REGEXP = "(\"\\w*\";)*(\"\\w*\"){1}";
    private final static String LINE_SPLIT_REGEXP = ";";
    private final static Pattern EMPTY_LINE_PATTERN = Pattern.compile(EMPTY_STRING_REGEXP);
    private final static Pattern LINE_PATTERN = Pattern.compile(LINE_REGEXP);

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        String sourceFileName = args.length > 0 ? args[0] : "data.txt";
        String destFileName = args.length > 1 ? args[1] : "result.txt";

        ArrayList<HashMap<String, Integer>> columns = computeColumnMap(sourceFileName);

        Set<String> uniqueLines = new HashSet<>();
        Set<String[]> matchingLines = new HashSet<>();
        computeUniqueLine(columns, uniqueLines, matchingLines, sourceFileName);

        List<List<String[]>> groups = computedGroups(matchingLines, columns);

        writeResult(uniqueLines, groups, destFileName);

        System.out.println("Число групп с более чем 1 строкой: " + groups.size());
        System.out.println("Число групп с 1 строкой: " + uniqueLines.size());

        long endTime = System.nanoTime();
        System.out.println("EXECUTION TIME: " + (endTime - startTime) / 1000000 + "ms");
    }

    private static ArrayList<HashMap<String, Integer>> computeColumnMap(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        ArrayList<HashMap<String, Integer>> columns = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (!LINE_PATTERN.matcher(line).matches() || EMPTY_LINE_PATTERN.matcher(line).matches()) {
                continue;
            }
            String[] lineArr = line.split(LINE_SPLIT_REGEXP);
            if (lineArr.length > columns.size()) {
                for (int i = columns.size(); i < lineArr.length; i++) {
                    columns.add(new HashMap<>());
                }
            }
            for (int i = 0; i < lineArr.length; i++) {
                if (lineArr[i].equals(EMPTY_STRING)) {
                    continue;
                }
                HashMap<String, Integer> columnMap = columns.get(i);
                columnMap.put(lineArr[i], columnMap.getOrDefault(lineArr[i], 0) + 1);
            }
        }
        reader.close();
        return columns;
    }

    private static void computeUniqueLine(ArrayList<HashMap<String, Integer>> columns, Set<String> uniqueLines, Set<String[]> matchingLines, String fileName) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {
            if (!LINE_PATTERN.matcher(line).matches() || EMPTY_LINE_PATTERN.matcher(line).matches()) {
                continue;
            }
            String[] lineArr = line.split(LINE_SPLIT_REGEXP);
            boolean isUnique = true;
            for (int i = 0; i < lineArr.length && isUnique; i++) {
                if (lineArr[i].equals(EMPTY_STRING)) {
                    continue;
                }
                HashMap<String, Integer> columnMap = columns.get(i);
                if (columnMap.get(lineArr[i]) > 1) {
                    isUnique = false;
                }
            }
            if (isUnique) {
                uniqueLines.add(line);
            } else {
                matchingLines.add(lineArr);
            }
        }
        reader.close();
    }

    private static List<List<String[]>> computedGroups(Set<String[]> matchingLines, ArrayList<HashMap<String, Integer>> columns) {
        List<List<String[]>> matchingGroups = new ArrayList<>();
        Set<String[]> visited = new HashSet<>();
        ArrayDeque<String[]> stack = new ArrayDeque<>();

        for (String[] matchingLine : matchingLines) {
            if (visited.contains(matchingLine)) {
                continue;
            }
            List<String[]> group = new ArrayList<>();
            stack.push(matchingLine);
            visited.add(matchingLine);
            while (!stack.isEmpty()) {
                String[] line1 = stack.pop();
                group.add(line1);
                for (int i = 0; i < line1.length; i++) {
                    if (columns.get(i).getOrDefault(line1[i], 0) < 2) {
                        continue;
                    }
                    for (String[] line2 : matchingLines) {
                        if (Arrays.equals(line1, line2) || i >= line2.length || visited.contains(line2)) {
                            continue;
                        }
                        if (line1[i].equals(line2[i])) {
                            stack.add(line2);
                            visited.add(line2);
                        }
                    }
                }
            }
            matchingGroups.add(group);
        }

        matchingGroups.sort(Comparator.comparingInt(strings -> -strings.size()));
        return matchingGroups;
    }

    private static void writeResult(Set<String> uniqueLines, List<List<String[]>> groups, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        writer.write("Число групп с более чем 1 строкой: " + groups.size());
        writer.newLine();

        int groupNumber = 1;
        for (List<String[]> group : groups) {
            writer.write("Группа " + groupNumber++);
            writer.newLine();
            for (String[] strings : group) {
                writer.write(String.join(LINE_SPLIT_REGEXP, strings));
                writer.newLine();
            }
        }

        for (String uniqueLine : uniqueLines) {
            writer.write("Группа " + groupNumber++);
            writer.newLine();
            writer.write(uniqueLine);
            writer.newLine();
        }

        writer.close();
    }
}