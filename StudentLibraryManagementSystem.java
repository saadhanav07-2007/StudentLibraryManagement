import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Student Library Management System
 * Single-file Java Swing implementation containing:
 * - Book model
 * - Library (manager)
 * - GUI (CardLayout with Home, Add, View, Issue, Return)
 * - Run the main() method.
 */
public class StudentLibraryManagementSystem {

    // ====== Model classes ======
    public static class Book {
        private final String id;
        private final String title;
        private final String author;
        private boolean issued;

        public Book(String id, String title, String author) {
            this.id = id.trim();
            this.title = title.trim();
            this.author = author.trim();
            this.issued = false;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public boolean isIssued() { return issued; }
        public void setIssued(boolean issued) { this.issued = issued; }
    }

    public static class Library {
        private final List<Book> books = new ArrayList<>();

        public synchronized boolean addBook(Book b) {
            if (b == null || b.getId().isEmpty()) return false;
            if (findBookById(b.getId()) != null) return false; // duplicate ID
            books.add(b);
            return true;
        }

        public synchronized Book findBookById(String id) {
            if (id == null) return null;
            for (Book b : books) {
                if (b.getId().equalsIgnoreCase(id.trim())) return b;
            }
            return null;
        }

        public synchronized boolean issueBook(String id) {
            Book b = findBookById(id);
            if (b == null) return false;
            if (b.isIssued()) return false;
            b.setIssued(true);
            return true;
        }

        public synchronized boolean returnBook(String id) {
            Book b = findBookById(id);
            if (b == null) return false;
            if (!b.isIssued()) return false;
            b.setIssued(false);
            return true;
        }

        public synchronized List<Book> getAllBooks() {
            return new ArrayList<>(books);
        }
    }

    // ====== GUI ======
    public static class LibraryGUI {
        private final JFrame frame;
        private final CardLayout cardLayout = new CardLayout();
        private final JPanel cards = new JPanel(cardLayout);
        private final Library library;
        private final BooksTableModel tableModel;

        public LibraryGUI(Library library) {
            this.library = library;
            this.tableModel = new BooksTableModel(library);
            frame = new JFrame("Student Library Management System");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    exitApplication();
                }
            });

            initComponents();
        }

        private void initComponents() {
            cards.add(homePanel(), "HOME");
            cards.add(addBookPanel(), "ADD");
            cards.add(viewBooksPanel(), "VIEW");
            cards.add(issueBookPanel(), "ISSUE");
            cards.add(returnBookPanel(), "RETURN");

            frame.add(cards, BorderLayout.CENTER);
            frame.add(bottomNav(), BorderLayout.SOUTH);
        }

        public void show() {
            frame.setVisible(true);
        }

        private JPanel topBar(String title) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JLabel label = new JLabel(title);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
            p.add(label, BorderLayout.WEST);
            return p;
        }

        private JPanel homePanel() {
            JPanel p = new JPanel(new BorderLayout());
            p.add(topBar("Home"), BorderLayout.NORTH);
            JPanel center = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JButton btnAdd = new JButton("Add Book");
            btnAdd.addActionListener(e -> cardLayout.show(cards, "ADD"));
            JButton btnView = new JButton("View Books");
            btnView.addActionListener(e -> { tableModel.refresh(); cardLayout.show(cards, "VIEW"); });
            JButton btnIssue = new JButton("Issue Book");
            btnIssue.addActionListener(e -> cardLayout.show(cards, "ISSUE"));
            JButton btnReturn = new JButton("Return Book");
            btnReturn.addActionListener(e -> cardLayout.show(cards, "RETURN"));
            JButton btnExit = new JButton("Exit");
            btnExit.addActionListener(e -> exitApplication());

            gbc.gridx = 0; gbc.gridy = 0; center.add(btnAdd, gbc);
            gbc.gridx = 1; center.add(btnView, gbc);
            gbc.gridx = 0; gbc.gridy = 1; center.add(btnIssue, gbc);
            gbc.gridx = 1; center.add(btnReturn, gbc);
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; center.add(btnExit, gbc);

            p.add(center, BorderLayout.CENTER);
            return p;
        }

        private JPanel addBookPanel() {
            JPanel p = new JPanel(new BorderLayout());
            p.add(topBar("Add Book"), BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lblId = new JLabel("Book ID:");
            JLabel lblTitle = new JLabel("Title:");
            JLabel lblAuthor = new JLabel("Author:");
            JTextField tfId = new JTextField(20);
            JTextField tfTitle = new JTextField(20);
            JTextField tfAuthor = new JTextField(20);

            gbc.gridx = 0; gbc.gridy = 0; form.add(lblId, gbc);
            gbc.gridx = 1; form.add(tfId, gbc);
            gbc.gridx = 0; gbc.gridy = 1; form.add(lblTitle, gbc);
            gbc.gridx = 1; form.add(tfTitle, gbc);
            gbc.gridx = 0; gbc.gridy = 2; form.add(lblAuthor, gbc);
            gbc.gridx = 1; form.add(tfAuthor, gbc);

            JButton btnAdd = new JButton("Add Book");
            btnAdd.addActionListener(e -> {
                String id = tfId.getText();
                String title = tfTitle.getText();
                String author = tfAuthor.getText();
                if (id.isBlank() || title.isBlank() || author.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Book b = new Book(id, title, author);
                boolean ok = library.addBook(b);
                if (!ok) {
                    JOptionPane.showMessageDialog(frame, "Failed to add book. ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Book added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    tfId.setText(""); tfTitle.setText(""); tfAuthor.setText("");
                    tableModel.refresh();
                }
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnBack = new JButton("Back");
            btnBack.addActionListener(e -> cardLayout.show(cards, "HOME"));
            bottom.add(btnBack); bottom.add(btnAdd);

            p.add(form, BorderLayout.CENTER);
            p.add(bottom, BorderLayout.SOUTH);
            return p;
        }

        private JPanel viewBooksPanel() {
            JPanel p = new JPanel(new BorderLayout());
            p.add(topBar("View Books"), BorderLayout.NORTH);

            JTable table = new JTable(tableModel);
            table.setFillsViewportHeight(true);
            JScrollPane scroll = new JScrollPane(table);

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnRefresh = new JButton("Refresh");
            btnRefresh.addActionListener(e -> tableModel.refresh());
            top.add(btnRefresh);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnBack = new JButton("Back");
            btnBack.addActionListener(e -> cardLayout.show(cards, "HOME"));
            bottom.add(btnBack);

            p.add(top, BorderLayout.NORTH);
            p.add(scroll, BorderLayout.CENTER);
            p.add(bottom, BorderLayout.SOUTH);
            return p;
        }

        private JPanel issueBookPanel() {
            JPanel p = new JPanel(new BorderLayout());
            p.add(topBar("Issue Book"), BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6,6,6,6);
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lblId = new JLabel("Enter Book ID to Issue:");
            JTextField tfId = new JTextField(20);
            JButton btnIssue = new JButton("Issue");

            gbc.gridx = 0; gbc.gridy = 0; form.add(lblId, gbc);
            gbc.gridx = 1; form.add(tfId, gbc);
            gbc.gridx = 1; gbc.gridy = 1; form.add(btnIssue, gbc);

            btnIssue.addActionListener(e -> {
                String id = tfId.getText();
                if (id.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please enter Book ID.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Book b = library.findBookById(id);
                if (b == null) {
                    JOptionPane.showMessageDialog(frame, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (b.isIssued()) {
                    JOptionPane.showMessageDialog(frame, "Book is already issued.", "Unavailable", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                boolean ok = library.issueBook(id);
                if (ok) {
                    JOptionPane.showMessageDialog(frame, "Book issued successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    tfId.setText("");
                    tableModel.refresh();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to issue book.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnBack = new JButton("Back");
            btnBack.addActionListener(e -> cardLayout.show(cards, "HOME"));
            bottom.add(btnBack);

            p.add(form, BorderLayout.CENTER);
            p.add(bottom, BorderLayout.SOUTH);
            return p;
        }

        private JPanel returnBookPanel() {
            JPanel p = new JPanel(new BorderLayout());
            p.add(topBar("Return Book"), BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6,6,6,6);
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lblId = new JLabel("Enter Book ID to Return:");
            JTextField tfId = new JTextField(20);
            JButton btnReturn = new JButton("Return");

            gbc.gridx = 0; gbc.gridy = 0; form.add(lblId, gbc);
            gbc.gridx = 1; form.add(tfId, gbc);
            gbc.gridx = 1; gbc.gridy = 1; form.add(btnReturn, gbc);

            btnReturn.addActionListener(e -> {
                String id = tfId.getText();
                if (id.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please enter Book ID.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Book b = library.findBookById(id);
                if (b == null) {
                    JOptionPane.showMessageDialog(frame, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!b.isIssued()) {
                    JOptionPane.showMessageDialog(frame, "Book is not issued.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                boolean ok = library.returnBook(id);
                if (ok) {
                    JOptionPane.showMessageDialog(frame, "Book returned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    tfId.setText("");
                    tableModel.refresh();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to return book.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnBack = new JButton("Back");
            btnBack.addActionListener(e -> cardLayout.show(cards, "HOME"));
            bottom.add(btnBack);

            p.add(form, BorderLayout.CENTER);
            p.add(bottom, BorderLayout.SOUTH);
            return p;
        }

        private JPanel bottomNav() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton btnHome = new JButton("Home");
            btnHome.addActionListener(e -> cardLayout.show(cards, "HOME"));
            JButton btnView = new JButton("View Books");
            btnView.addActionListener(e -> { tableModel.refresh(); cardLayout.show(cards, "VIEW"); });
            JButton btnAdd = new JButton("Add Book");
            btnAdd.addActionListener(e -> cardLayout.show(cards, "ADD"));
            p.add(btnHome); p.add(btnView); p.add(btnAdd);
            return p;
        }

        private void exitApplication() {
            int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                frame.dispose();
                System.exit(0);
            }
        }

        // Table model for displaying books
        private static class BooksTableModel extends AbstractTableModel {
            private final String[] cols = {"ID", "Title", "Author", "Status"};
            private List<Book> data = new ArrayList<>();
            private final Library library;

            public BooksTableModel(Library library) {
                this.library = library;
                refresh();
            }

            public void refresh() {
                this.data = library.getAllBooks();
                fireTableDataChanged();
            }

            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return cols.length;
            }

            @Override
            public String getColumnName(int column) {
                return cols[column];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                Book b = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> b.getId();
                    case 1 -> b.getTitle();
                    case 2 -> b.getAuthor();
                    case 3 -> b.isIssued() ? "Issued" : "Available";
                    default -> "";
                };
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        }
    }

    // ====== Main ======
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Library library = new Library();

            // Seed with sample books
            library.addBook(new Book("B001", "Introduction to Java", "James Gosling"));
            library.addBook(new Book("B002", "Data Structures", "Robert Lafore"));
            library.addBook(new Book("B003", "Algorithms", "Cormen et al."));

            LibraryGUI gui = new LibraryGUI(library);
            gui.show();
        });
    }
}
