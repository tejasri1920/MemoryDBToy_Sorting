import java.io.*;
import java.util.*;

class Node {
    List<String> data;
    Node prev;
    Node next;

    Node(List<String> data) {
        this.data = data;
        this.prev = null;
        this.next = null;
    }
}

class MemoryDB {
    private Node head = null;

    // Add record at the end
    public void addRecord(List<String> studentData) {
        Node newNode = new Node(studentData);
        if (head == null) {
            head = newNode;
        } else {
            Node cur = head;
            while (cur.next != null) {
                cur = cur.next;
            }
            cur.next = newNode;
            newNode.prev = cur;
        }
    }

    // Recursive loader helper
    private void reloadRecursive(Iterator<String[]> iter, int i) {
        if (!iter.hasNext()) return;
        String[] row = iter.next();
        if (row.length > 0) {
            List<String> newRow = new ArrayList<>();
            newRow.add(String.valueOf(i)); // RowID
            newRow.addAll(Arrays.asList(row));
            addRecord(newRow);
        }
        reloadRecursive(iter, i + 1);
    }

    // Reload from CSV
    public List<String> reloadFromCSV(String csvPath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = br.readLine();
            if (headerLine == null) return Collections.emptyList();
            String[] header = headerLine.split(",");

            // reset linked list
            head = null;

            // collect all rows
            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split(","));
            }
            reloadRecursive(rows.iterator(), 0);

            List<String> headerList = new ArrayList<>();
            headerList.add("RowID");
            headerList.addAll(Arrays.asList(header));
            return headerList;
        }
    }

    // Recursive export
    public void exportToCSVRecursive(PrintWriter pw, Node node) {
        if (node == null) return;
        pw.println(String.join(",", node.data));
        exportToCSVRecursive(pw, node.next);
    }

    // Bubble Sort
    public void bubbleSort(int keyIndex) {
        if (head == null) return;
        boolean swapped;
        do {
            swapped = false;
            Node cur = head;
            while (cur.next != null) {
                if (cur.data.get(keyIndex).compareTo(cur.next.data.get(keyIndex)) > 0) {
                    List<String> temp = cur.data;
                    cur.data = cur.next.data;
                    cur.next.data = temp;
                    swapped = true;
                }
                cur = cur.next;
            }
        } while (swapped);
    }

    // Insertion Sort
    public void insertionSort(int keyIndex) {
        if (head == null || head.next == null) return;

        Node sorted = head;
        Node cur = head.next;
        sorted.next = null;

        while (cur != null) {
            Node nextNode = cur.next;
            if (cur.data.get(keyIndex).compareTo(sorted.data.get(keyIndex)) < 0) {
                // insert at beginning
                cur.next = sorted;
                sorted.prev = cur;
                cur.prev = null;
                sorted = cur;
            } else {
                Node search = sorted;
                while (search.next != null &&
                        search.next.data.get(keyIndex).compareTo(cur.data.get(keyIndex)) < 0) {
                    search = search.next;
                }
                cur.next = search.next;
                if (search.next != null) {
                    search.next.prev = cur;
                }
                search.next = cur;
                cur.prev = search;
            }
            cur = nextNode;
        }
        head = sorted;
    }

    // Display (first few columns for readability)
    public void display(int maxRows) {
        if (head == null) {
            System.out.println("<empty>");
            return;
        }
        Node cur = head;
        int count = 0;
        while (cur != null && count < maxRows) {
            System.out.println(String.join(", ", cur.data));
            cur = cur.next;
            count++;
        }
        if (cur != null) {
            System.out.println("... (more rows)");
        }
    }

    public Node getHead() {
        return head;
    }
}

public class MemoryDBApp {
    public static void main(String[] args) {
        String csvFile = "student-data.csv";
        String outFile = "updated_student-data.csv";

        MemoryDB db = new MemoryDB();
        List<String> header = new ArrayList<>();

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Memory Database Menu =====");
            System.out.println("1. Load from CSV (recursive)");
            System.out.println("2. Bubble Sort");
            System.out.println("3. Insertion Sort");
            System.out.println("4. Export DB to CSV (recursive)");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        header = db.reloadFromCSV(csvFile);
                        System.out.println("[OK] Loaded from file.");
                        System.out.println("Columns: " + header);
                        db.display(10);
                        break;

                    case "2":
                    case "3":
                        if (header.isEmpty()) {
                            System.out.println("[ERR] Load first using option 1.");
                            break;
                        }
                        // show available columns
                        System.out.println("Available columns for sorting:");
                        for (int i = 0; i < header.size(); i++) {
                            System.out.println(i + ". " + header.get(i));
                        }
                        System.out.print("Enter column index: ");
                        int keyIndex = Integer.parseInt(sc.nextLine().trim());

                        if (choice.equals("2")) {
                            db.bubbleSort(keyIndex);
                            System.out.println("[OK] Bubble Sorted by " + header.get(keyIndex));
                        } else {
                            db.insertionSort(keyIndex);
                            System.out.println("[OK] Insertion Sorted by " + header.get(keyIndex));
                        }
                        db.display(10);
                        break;

                    case "4":
                        if (header.isEmpty()) {
                            System.out.println("[ERR] Nothing loaded yet.");
                        } else {
                            try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
                                pw.println(String.join(",", header));
                                db.exportToCSVRecursive(pw, db.getHead());
                            }
                            System.out.println("[OK] Exported to " + outFile);
                        }
                        break;

                    case "5":
                        System.out.println("Goodbye!");
                        return;

                    default:
                        System.out.println("Invalid choice, try again.");
                }
            } catch (IOException e) {
                System.out.println("[ERR] " + e.getMessage());
            }
        }
    }
}
