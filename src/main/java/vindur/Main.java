package vindur;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private final static String EMPTY_STRING = "\"\"";
    private final static String EMPTY_STRING_REGEXP = "(\"\";)*(\"\")";
    private final static String LINE_REGEXP = "(\"[^\";]*\";)*(\"[^\";]*\")";
    private final static String LINE_SPLIT_REGEXP = ";";
    private final static Pattern EMPTY_LINE_PATTERN = Pattern.compile(EMPTY_STRING_REGEXP);
    private final static Pattern LINE_PATTERN = Pattern.compile(LINE_REGEXP);
    private final static List<String> LINES = new ArrayList<>(1000_000);

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        String sourceFileName = args.length > 0 ? args[0] : "data.txt";
        String destFileName = args.length > 1 ? args[1] : "result.txt";

        List<Map<String, List<Integer>>> columns = computeColumnMap(sourceFileName);

        System.out.println("COLUMNS " + columns.size());

        Set<Integer> uniqueLines = new HashSet<>();
        Set<Integer> matchingLines = new HashSet<>();
        computeUniqueLine(columns, uniqueLines, matchingLines);

        System.out.println("UNIQUE " + uniqueLines.size());
        System.out.println("MATCHING " + matchingLines.size());

        List<List<Integer>> groups = computedGroups(matchingLines, columns);

        writeResult(uniqueLines, groups, destFileName);

        System.out.println("Число групп с более чем 1 строкой: " + groups.size());
        System.out.println("Число групп с 1 строкой: " + uniqueLines.size());

        long endTime = System.nanoTime();
        System.out.println("EXECUTION TIME: " + (endTime - startTime) / 1000000000 + "s");
    }

    private static List<Map<String, List<Integer>>> computeColumnMap(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        List<Map<String, List<Integer>>> columns = new ArrayList<>();

        String line;
        String[] lineArr;
        Integer lineIndex = 0;
        while ((line = reader.readLine()) != null) {
            if (!LINE_PATTERN.matcher(line).matches() || EMPTY_LINE_PATTERN.matcher(line).matches()) {
                continue;
            }
            LINES.add(line);
            lineArr = line.split(";");
            if (lineArr.length > columns.size()) {
                for (int i = columns.size(); i < lineArr.length; i++) {
                    columns.add(new HashMap<>());
                }
            }
            for (int i = 0; i < lineArr.length; i++) {
                if (lineArr[i].equals(EMPTY_STRING)) {
                    continue;
                }
                List<Integer> wordSet = columns.get(i).getOrDefault(lineArr[i], new ArrayList<>());
                wordSet.add(lineIndex);
                columns.get(i).put(lineArr[i], wordSet);
            }
            lineIndex++;
        }
        reader.close();
        return columns;
    }

    private static void computeUniqueLine(List<Map<String, List<Integer>>> columns, Set<Integer> uniqueLines, Set<Integer> matchingLines) {
        for (int i = 0; i < LINES.size(); i++) {
            String[] lineArr = LINES.get(i).split(LINE_SPLIT_REGEXP);
            boolean isUnique = true;
            for (int j = 0; j < lineArr.length && isUnique; j++) {
                if (lineArr[j].equals(EMPTY_STRING)) {
                    continue;
                }
                Map<String, List<Integer>> columnMap = columns.get(j);
                if (columnMap.get(lineArr[j]).size() > 1) {
                    isUnique = false;
                }
            }
            if (isUnique) {
                uniqueLines.add(i);
            } else {
                matchingLines.add(i);
            }
        }
    }

    private static List<List<Integer>> computedGroups(Set<Integer> matchingLines, List<Map<String, List<Integer>>> columns) {
        List<List<Integer>> matchingGroups = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        for (Integer matchingLine : matchingLines) {
            if (visited.contains(matchingLine)) {
                continue;
            }
            List<Integer> group = new ArrayList<>();
            stack.push(matchingLine);
            visited.add(matchingLine);
            while (!stack.isEmpty()) {
                Integer line1Index = stack.pop();
                group.add(line1Index);
                String[] line1Arr = LINES.get(line1Index).split(LINE_SPLIT_REGEXP);
                for (int i = 0; i < line1Arr.length; i++) {
                    List<Integer> matches = columns.get(i).get(line1Arr[i]);
                    if (matches == null || matches.size() == 1) {
                        continue;
                    }
                    for (Integer line2Index : matches) {
                        if (line1Index.equals(line2Index) || visited.contains(line2Index)) {
                            continue;
                        }
                        stack.add(line2Index);
                        visited.add(line2Index);
                    }
                }
            }
            matchingGroups.add(group);
        }

        matchingGroups.sort(Comparator.comparingInt(strings -> -strings.size()));
        return matchingGroups;
    }

    private static void writeResult(Set<Integer> uniqueLines, List<List<Integer>> groups, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        writer.write("Число групп с более чем 1 строкой: " + groups.size());
        writer.newLine();

        int groupNumber = 1;
        for (List<Integer> group : groups) {
            writer.write("Группа " + groupNumber++);
            writer.newLine();
            for (Integer lineIndex : group) {
                writer.write(LINES.get(lineIndex));
                writer.newLine();
            }
        }

        for (Integer uniqueLineIndex : uniqueLines) {
            writer.write("Группа " + groupNumber++);
            writer.newLine();
            writer.write(LINES.get(uniqueLineIndex));
            writer.newLine();
        }

        writer.close();
    }
}