package com.hibernate;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class ObjectService {

    private static final Logger logger = Logger.getLogger(ObjectService.class.getName());
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");

    public static void showTable() {
        JFrame tableFrame = new JFrame("Object");
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);

        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Quantity");
        model.addColumn("Shelf ID");
        model.addColumn("Contract ID");

        load(model);

        JScrollPane scrollPane = new JScrollPane(table);
        tableFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        tableFrame.setSize(400, 300);

        // Create buttons for adding, editing, deleting, searching, and generating a report
        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");
        JButton editBtn = new JButton("Edit");
        JButton searchBtn = new JButton("Search");
        JButton reportBtn = new JButton("Generate Report");

        addBtn.addActionListener(e -> add(model));
        deleteBtn.addActionListener(e -> delete(model, table));
        editBtn.addActionListener(e -> edit(model, table));
        searchBtn.addActionListener(e -> search(model));
        reportBtn.addActionListener(e -> {
            try {
                ReportGenerator.generateCargoReport("CargoReport.pdf");
                JOptionPane.showMessageDialog(null, "Report generated successfully as CargoReport.pdf", 
                                              "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to generate report: " + ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "Failed to generate report: " + ex.getMessage(), 
                                              "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(addBtn);
        controlPanel.add(deleteBtn);
        controlPanel.add(editBtn);
        controlPanel.add(searchBtn);
        controlPanel.add(reportBtn); // Add the report generation button

        tableFrame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
        tableFrame.setVisible(true);
    }

    private static void load(DefaultTableModel model) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        List<Cargo> grist = em.createQuery("SELECT f FROM Cargo f WHERE f.idobject > 0", Cargo.class).getResultList();

        // Clear the model before adding new data
        model.setRowCount(0);

        if (grist.isEmpty()) {
            logger.warning("No rows found in table.");
        } else {
            for (Cargo jj : grist) {
                // Add data to the table model
                model.addRow(new Object[]{jj.getIdObject(), jj.getObjectName(), jj.getQuantity(), jj.getShelf().getShelfId(), jj.getContract().getContractId()});
            }
        }

        em.getTransaction().commit();
        em.close();
    }

    private static void search(DefaultTableModel model) {
        // Create search dialog
        JPanel searchPanel = new JPanel(new GridLayout(1, 2));
        JTextField searchField = new JTextField();
        searchPanel.add(new JLabel("Enter Object Name:"));
        searchPanel.add(searchField);

        int option = JOptionPane.showConfirmDialog(null, searchPanel, "Search for Object", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();
                List<Cargo> searchResults = em.createQuery("SELECT c FROM Cargo c WHERE c.objectName = :name", Cargo.class)
                                              .setParameter("name", searchTerm)
                                              .getResultList();
                em.getTransaction().commit();
                em.close();

                if (!searchResults.isEmpty()) {
                    int totalQuantity = 0;
                    for (Cargo cargo : searchResults) {
                        totalQuantity += cargo.getQuantity();
                    }

                    JOptionPane.showMessageDialog(null, "Total quantity for '" + searchTerm + "': " + totalQuantity, 
                                                  "Search Results", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    logger.info("No items found for search term: " + searchTerm);
                    JOptionPane.showMessageDialog(null, "No items found matching: '" + searchTerm + "'.", 
                                                  "Search Results", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a search term.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void delete(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // Confirmation dialog for deletion
            int confirmation = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this object?", 
                "Delete Confirmation", 
                JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                EntityManager em = null;
                try {
                    em = emf.createEntityManager();
                    em.getTransaction().begin();

                    // Find the object by ID
                    Cargo cargo = em.find(Cargo.class, Integer.parseInt(model.getValueAt(selectedRow, 0).toString()));

                    if (cargo != null) {
                        em.remove(cargo);
                        em.getTransaction().commit();
                        model.removeRow(selectedRow);
                        logger.info("Cargo with ID " + cargo.getIdObject() + " deleted successfully.");
                        JOptionPane.showMessageDialog(null, "Object deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        logger.warning("Cargo with ID " + model.getValueAt(selectedRow, 0) + " not found.");
                        JOptionPane.showMessageDialog(null, "Object not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    if (em != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    logger.log(Level.SEVERE, "Error occurred while deleting cargo: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(null, "An error occurred while deleting the object: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (em != null) {
                        em.close();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Object deletion canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            logger.warning("No row selected for deletion.");
            JOptionPane.showMessageDialog(null, "Select a row to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void add(DefaultTableModel model) {
        EntityManager em = emf.createEntityManager();
        List<Shelf> shelves = em.createQuery("SELECT s FROM Shelf s", Shelf.class).getResultList();
        List<Contract> contracts = em.createQuery("SELECT c FROM Contract c", Contract.class).getResultList();
    
        JComboBox<Shelf> shelfComboBox = new JComboBox<>(shelves.toArray(new Shelf[0]));
        shelfComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> 
            new JLabel("Shelf ID: " + value.getShelfId() + " Available space: " + value.getAvailableSpace()));
    
        JComboBox<Contract> contractComboBox = new JComboBox<>(contracts.toArray(new Contract[0]));
        contractComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> 
            new JLabel("Contract ID: " + value.getContractId() + " Company Name: " + value.getCompany().getCompanyName()));
    
        JPanel panel = new JPanel(new GridLayout(4, 2));
    
        JTextField objectNameField = new JTextField();
        JTextField quantityField = new JTextField();
    
        panel.add(new JLabel("Object Name:"));
        panel.add(objectNameField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Select Shelf:"));
        panel.add(shelfComboBox);
        panel.add(new JLabel("Select Contract:"));
        panel.add(contractComboBox);
    
        int option = JOptionPane.showConfirmDialog(null, panel, "Add New Object", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String objectName = objectNameField.getText().trim();
            String quantityStr = quantityField.getText().trim();
    
            if (!objectName.isEmpty() && !quantityStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(null, "Quantity must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
    
                    Shelf selectedShelf = (Shelf) shelfComboBox.getSelectedItem();
                    if (selectedShelf.getAvailableSpace() < quantity) {
                        JOptionPane.showMessageDialog(null, "Not enough space on the selected shelf.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
    
                    Contract selectedContract = (Contract) contractComboBox.getSelectedItem();
    
                    em.getTransaction().begin();
                    Cargo newCargo = new Cargo();
                    newCargo.setObjectName(objectName);
                    newCargo.setQuantity(quantity);
                    newCargo.setShelf(selectedShelf);
                    newCargo.setContract(selectedContract);
    
                    em.persist(newCargo);
                    em.getTransaction().commit();
    
                    model.addRow(new Object[]{newCargo.getIdObject(), newCargo.getObjectName(), newCargo.getQuantity(), newCargo.getShelf().getShelfId(), newCargo.getContract().getContractId()});
                    JOptionPane.showMessageDialog(null, "Cargo added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid quantity format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error adding cargo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "All fields must be filled in.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        em.close();
    }
    

    private static void edit(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        
        if (selectedRow != -1) {
            int cargoId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            EntityManager em = emf.createEntityManager();
            
            try {
                // Fetch the Cargo to edit
                Cargo cargo = em.find(Cargo.class, cargoId);
                if (cargo == null) {
                    JOptionPane.showMessageDialog(null, "Cargo not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                // Fetch Shelves and Contracts for ComboBox options
                List<Shelf> shelves = em.createQuery("SELECT s FROM Shelf s", Shelf.class).getResultList();
                List<Contract> contracts = em.createQuery("SELECT c FROM Contract c", Contract.class).getResultList();
    
                // Create combo boxes with data
                JComboBox<Shelf> shelfComboBox = new JComboBox<>(shelves.toArray(new Shelf[0]));
                shelfComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> 
                    new JLabel("Shelf ID: " + value.getShelfId() + " Available space: " + value.getAvailableSpace()));
    
                JComboBox<Contract> contractComboBox = new JComboBox<>(contracts.toArray(new Contract[0]));
                contractComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> 
                    new JLabel("Contract ID: " + value.getContractId() + " Company Name: " + value.getCompany().getCompanyName()));
    
                // Populate the fields with the current values of the selected cargo
                JTextField objectNameField = new JTextField(cargo.getObjectName());
                JTextField quantityField = new JTextField(String.valueOf(cargo.getQuantity()));
    
                // Populate the combo boxes with the selected shelf and contract
                shelfComboBox.setSelectedItem(cargo.getShelf());
                contractComboBox.setSelectedItem(cargo.getContract());
    
                JPanel panel = new JPanel(new GridLayout(4, 2));
                panel.add(new JLabel("Object Name:"));
                panel.add(objectNameField);
                panel.add(new JLabel("Quantity:"));
                panel.add(quantityField);
                panel.add(new JLabel("Select Shelf:"));
                panel.add(shelfComboBox);
                panel.add(new JLabel("Select Contract:"));
                panel.add(contractComboBox);
    
                int option = JOptionPane.showConfirmDialog(null, panel, "Edit Object", JOptionPane.OK_CANCEL_OPTION);
    
                if (option == JOptionPane.OK_OPTION) {
                    String objectName = objectNameField.getText().trim();
                    String quantityStr = quantityField.getText().trim();
    
                    if (!objectName.isEmpty() && !quantityStr.isEmpty()) {
                        try {
                            int newQuantity = Integer.parseInt(quantityStr);
                            if (newQuantity <= 0) {
                                JOptionPane.showMessageDialog(null, "Quantity must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
    
                            Shelf selectedShelf = (Shelf) shelfComboBox.getSelectedItem();
                            Contract selectedContract = (Contract) contractComboBox.getSelectedItem();
    
                            // Если изменяется полка
                            if (!cargo.getShelf().equals(selectedShelf)) {
                                // Рассчитываем, сколько места освободилось на старой полке
                                int spaceFreed = cargo.getQuantity();
    
                                // Проверяем, достаточно ли места на новой полке
                                if (selectedShelf.getAvailableSpace() < newQuantity) {
                                    JOptionPane.showMessageDialog(null, "Not enough space on the selected shelf.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
    
                                // Освобождаем место на старой полке
   
    
                                // Обновляем полку для cargo
                                cargo.setShelf(selectedShelf);
                            } else {
                                // Если полка не изменяется, просто проверяем, есть ли достаточно места на текущей полке
                                if (cargo.getShelf().getAvailableSpace() + cargo.getQuantity() < newQuantity) {
                                    JOptionPane.showMessageDialog(null, "Not enough space on the selected shelf.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                
                            }
    
                            // Обновляем данные cargo
                            em.getTransaction().begin();
                            cargo.setObjectName(objectName);
                            cargo.setQuantity(newQuantity);
                            cargo.setContract(selectedContract);
                            em.getTransaction().commit();
    
                            // Обновляем модель таблицы
                            model.setValueAt(objectName, selectedRow, 1);
                            model.setValueAt(newQuantity, selectedRow, 2);
                            model.setValueAt(selectedShelf.getShelfId(), selectedRow, 3);
                            model.setValueAt(selectedContract.getContractId(), selectedRow, 4);
    
                            JOptionPane.showMessageDialog(null, "Cargo updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Invalid quantity format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        } catch (Exception e) {
                            if (em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                            }
                            JOptionPane.showMessageDialog(null, "Error updating cargo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "All fields must be filled in.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error fetching cargo for editing: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                em.close();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Select a row to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
}
