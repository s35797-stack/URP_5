import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Function;

public class ObjectInspector extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;

    public ObjectInspector() {
        setTitle("Object Inspector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton loadButton = new JButton("Select file .class");
        JButton clearButton = new JButton("Clear");
        topPanel.add(loadButton);
        topPanel.add(clearButton);

        statusLabel = new JLabel("Select file class for analysis");
        tabbedPane = new JTabbedPane();

        String[] columns = {"Type", "Modifiers", "Name", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(table);
        tabbedPane.addTab("All elements", scrollPane);

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        setSize(900, 600);

        loadButton.addActionListener(e -> loadClassFile());
        clearButton.addActionListener(e -> clearTable());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ObjectInspector inspector = new ObjectInspector();
            inspector.setVisible(true);
        });
    }

    public void loadClassFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Class files (.class)", "class");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            analyzeClassFile(selectedFile);
        }
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void analyzeClassFile(File selectedFile) {
        String fileName = selectedFile.getName();
        String className = fileName.endsWith(".class")
                ? fileName.substring(0, fileName.length() - 6)
                : fileName;

        File parentDir = selectedFile.getParentFile();
        if (parentDir == null) {
            parentDir = new File(".");
        }

        try {
            URL[] urls = new URL[]{ parentDir.toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls);
            Class<?> loadedClass = classLoader.loadClass(className);

            clearTable();
            analyzeClass(loadedClass);
            statusLabel.setText("Successfully loaded: " + loadedClass.getName());
            classLoader.close();
        } catch (Exception e) {
            statusLabel.setText("Error loading class: " + e.getMessage());
        }
    }

    public void analyzeClass(Class<?> clazz) {
        addReflectionElements(clazz.getDeclaredFields(), "FIELDS", ReflectionCLI::createFieldRow);
        addReflectionElements(clazz.getDeclaredConstructors(), "CONSTRUCTORS", ReflectionCLI::createConstructorRow);
        addReflectionElements(clazz.getDeclaredMethods(), "METHODS", ReflectionCLI::createMethodRow);
        addReflectionElements(clazz.getInterfaces(), "INTERFACES", ReflectionCLI::createInterfaceRow);
    }

    public <T> void addReflectionElements(T[] elements, String sectionTitle, Function<T, Object[]> rowCreator) {
        if (elements != null && elements.length > 0) {
            for (T element : elements) {
                tableModel.addRow(rowCreator.apply(element));
            }
        }
    }
}