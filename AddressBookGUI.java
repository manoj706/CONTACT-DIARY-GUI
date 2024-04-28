import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class AddressBookGUI extends JFrame {

    private JTextField nameField, emailField, phoneField, searchField;
    private JButton addButton, deleteButton, clearAllButton, editButton, importButton, exportButton, sortButton;
    private JList<String> displayList;
    private DefaultListModel<String> listModel;
    private ArrayList<Contact> contacts;

    public AddressBookGUI() {
        super("Address Book");
        // Initialize contacts arraylist
        contacts = new ArrayList<>();
        // Create components
        nameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        addButton = new JButton("Add Contact");
        deleteButton = new JButton("Delete Contact");
        clearAllButton = new JButton("Clear All");
        editButton = new JButton("Edit Contact");
        importButton = new JButton("Import");
        exportButton = new JButton("Export");
        sortButton = new JButton("Sort");
        displayList = new JList<>();
        listModel = new DefaultListModel<>();
        displayList.setModel(listModel);
        searchField = new JTextField(20);
        // Layout
        JPanel inputPanel = new JPanel(new GridLayout(8, 2));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(phoneField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        inputPanel.add(new JLabel("Search:"));
        inputPanel.add(searchField);
        inputPanel.add(editButton);
        inputPanel.add(clearAllButton);
        inputPanel.add(importButton);
        inputPanel.add(exportButton);
        inputPanel.add(new JLabel("Sort by:"));
        inputPanel.add(sortButton);
        JScrollPane scrollPane = new JScrollPane(displayList);
        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        // Add action listeners
        addButton.addActionListener(e -> addContact());
        deleteButton.addActionListener(e -> deleteContact());
        clearAllButton.addActionListener(e -> clearAllContacts());
        editButton.addActionListener(e -> editContact());
        importButton.addActionListener(e -> importContacts());
        exportButton.addActionListener(e -> exportContacts());
        sortButton.addActionListener(e -> sortContacts());
        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 380); // Adjust size for additional button
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
        // Load contacts from file
        loadContacts();
    }

    private void addContact() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        Contact contact = new Contact(name, email, phone);
        contacts.add(contact);
        listModel.addElement(contact.toString());
        // Clear fields
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        // Save contacts to file
        saveContacts();
    }

    private void deleteContact() {
        int index = displayList.getSelectedIndex();
        if (index >= 0 && index < contacts.size()) {
            contacts.remove(index);
            listModel.remove(index);
            // Save contacts to file
            saveContacts();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete.");
        }
    }

    private void clearAllContacts() {
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all contacts?", "Clear All Contacts", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            contacts.clear();
            listModel.clear(); // Clear the listModel
            // Save contacts to file
            saveContacts();
        }
    }

    private void editContact() {
        int index = displayList.getSelectedIndex();
        if (index >= 0 && index < contacts.size()) {
            String newName = nameField.getText();
            String newEmail = emailField.getText();
            String newPhone = phoneField.getText();
            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            Contact editedContact = new Contact(newName, newEmail, newPhone);
            contacts.set(index, editedContact);
            listModel.setElementAt(editedContact.toString(), index);
            // Clear fields
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            // Save contacts to file
            saveContacts();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a contact to edit.");
        }
    }

    private void importContacts() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        Contact contact = new Contact(parts[0], parts[1], parts[2]);
                        contacts.add(contact);
                        listModel.addElement(contact.toString());
                    }
                }
                // Save contacts to file
                saveContacts();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading from file: " + e.getMessage());
            }
        }
    }

    private void exportContacts() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Contact contact : contacts) {
                    writer.write(contact.getName() + "," + contact.getEmail() + "," + contact.getPhone() + "\n");
                }
                JOptionPane.showMessageDialog(this, "Contacts exported successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error writing to file: " + e.getMessage());
            }
        }
    }

    private void sortContacts() {
        String sortCriteria = (String) JOptionPane.showInputDialog(this, "Select sorting criteria:", "Sort Contacts", JOptionPane.QUESTION_MESSAGE, null, new String[]{"Name", "Email", "Phone"}, "Name");
        if (sortCriteria != null) {
            switch (sortCriteria) {
                case "Name":
                    contacts.sort(Comparator.comparing(Contact::getName));
                    break;
                case "Email":
                    contacts.sort(Comparator.comparing(Contact::getEmail));
                    break;
                case "Phone":
                    contacts.sort(Comparator.comparing(Contact::getPhone));
                    break;
            }
            // Update the displayed list
            updateList(searchField.getText());
        }
    }

    private void updateList(String searchText) {
        listModel.clear();
        for (Contact contact : contacts) {
            if (contact.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                contact.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                contact.getPhone().toLowerCase().contains(searchText.toLowerCase())) {
                listModel.addElement(contact.toString());
            }
        }
    }

    private void saveContacts() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("contacts.dat"))) {
            outputStream.writeObject(contacts);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving contacts: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadContacts() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("contacts.dat"))) {
            contacts = (ArrayList<Contact>) inputStream.readObject();
            for (Contact contact : contacts) {
                listModel.addElement(contact.toString());
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading contacts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AddressBookGUI::new);
    }
}

class Contact implements Serializable {
    private String name;
    private String email;
    private String phone;

    public Contact(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return name + " | " + email + " | " + phone;
    }
}
