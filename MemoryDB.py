import csv

class Node:
    def __init__(self, data=None):
        self.data = data
        self.prev = None
        self.next = None

class MemoryDB:
    def __init__(self):
        self.head = None

    def add_record(self, student_data):
        new_node = Node(student_data)
        if self.head is None:
            self.head = new_node
        else:
            cur = self.head
            while cur.next:
                cur = cur.next
            cur.next = new_node
            new_node.prev = cur

    def _reload_recursive(self, reader, i=0):
        try:
            row = next(reader)
            if row:
                self.add_record([str(i)] + row)
            self._reload_recursive(reader, i+1)
        except StopIteration:
            return

    # (Option 1) Reload entire list (recursive)
    def reload_from_csv(self, csv_path):
        with open(csv_path, 'r', encoding='utf-8', newline='') as f:
            reader = csv.reader(f)
            header = next(reader)
            self.head = None
            self._reload_recursive(reader)
        return ["RowID"] + header

    # Export recursively
    def export_to_csv_recursive(self, writer, node):
        if node is None:
            return
        writer.writerow(node.data)
        self.export_to_csv_recursive(writer, node.next)

    # Bubble Sort on linked list
    def bubble_sort(self, key_index=1):
        if self.head is None:
            return
        swapped = True
        while swapped:
            swapped = False
            cur = self.head
            while cur.next:
                if cur.data[key_index] > cur.next.data[key_index]:
                    cur.data, cur.next.data = cur.next.data, cur.data
                    swapped = True
                cur = cur.next

    # Insertion Sort on linked list
    def insertion_sort(self, key_index=1):
        if self.head is None or self.head.next is None:
            return

        sorted_head = self.head
        cur = self.head.next
        sorted_head.next = None

        while cur:
            next_node = cur.next
            if cur.data[key_index] < sorted_head.data[key_index]:
                # insert at beginning
                cur.next = sorted_head
                sorted_head.prev = cur
                cur.prev = None
                sorted_head = cur
            else:
                search = sorted_head
                while search.next and search.next.data[key_index] < cur.data[key_index]:
                    search = search.next
                cur.next = search.next
                if search.next:
                    search.next.prev = cur
                search.next = cur
                cur.prev = search
            cur = next_node

        self.head = sorted_head

    # Pretty print
    def display(self, max_rows=10):
        if not self.head:
            print("<empty>")
            return
        cur = self.head
        print("RowID | Data (first few columns)")
        print("-" * 70)
        count = 0
        while cur and count < max_rows:
            print(", ".join(cur.data[:6]), "...")
            cur = cur.next
            count += 1
        if cur:
            print("... (more rows)")


# ---------- Menu-driven program ----------
def main():
    csv_file = "student-data.csv"
    out_file = "updated_student-data.csv"

    db = MemoryDB()
    header = []

    while True:
        print("\n===== Memory Database Menu =====")
        print("1. Load from CSV (recursive)")
        print("2. Bubble Sort")
        print("3. Insertion Sort")
        print("4. Export DB to CSV (recursive)")
        print("5. Exit")

        try:
            choice = input("Enter choice: ").strip()
        except KeyboardInterrupt:
            print("\nExiting program...")
            exit(0)

        if choice == "1":
            header = db.reload_from_csv(csv_file)
            print("[OK] Loaded from file.")
            print("Columns:", header)
            db.display()

        elif choice in ["2", "3"]:
            if not header:
                print("[ERR] Load first using option 1.")
            else:
                print("Available columns for sorting:")
                for i, col in enumerate(header):
                    print(f"{i}. {col}")
                try:
                    key_index = int(input("Enter column index to sort by: ").strip())
                except ValueError:
                    print("[ERR] Invalid input.")
                    continue

                if choice == "2":
                    db.bubble_sort(key_index=key_index)
                    print(f"[OK] Bubble Sorted by {header[key_index]}")
                else:
                    db.insertion_sort(key_index=key_index)
                    print(f"[OK] Insertion Sorted by {header[key_index]}")
                db.display()

        elif choice == "4":
            if not header:
                print("[ERR] Nothing loaded yet.")
            else:
                with open(out_file, "w", newline='', encoding='utf-8') as f:
                    writer = csv.writer(f)
                    writer.writerow(header)
                    db.export_to_csv_recursive(writer, db.head)
                print(f"[OK] Exported to {out_file}")

        elif choice == "5":
            print("Goodbye!")
            break

        else:
            print("Invalid choice, try again.")

if __name__ == "__main__":
    main()
