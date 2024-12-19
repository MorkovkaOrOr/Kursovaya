package com.hibernate;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

public class ContractService {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
    private static final Logger logger = Logger.getLogger(ContractService.class.getName());

    public static void showJudgeTable() {
        JFrame tableFrame = new JFrame("Contract");
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
    
        model.addColumn("ID");
        model.addColumn("Company");
        model.addColumn("Date end");
        load(model);
    
        JScrollPane scrollPane = new JScrollPane(table);
        tableFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        tableFrame.setSize(400, 300);
    
        // Create buttons for adding, editing, deleting, and searching
        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");
        JButton editBtn = new JButton("Edit");
    
        addBtn.addActionListener(e -> add(model));
        deleteBtn.addActionListener(e -> delete(model, table));
        editBtn.addActionListener(e -> edit(model, table));
    
        JPanel controlPanel = new JPanel();
        controlPanel.add(addBtn);
        controlPanel.add(deleteBtn);
        controlPanel.add(editBtn);
    
        tableFrame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
    
        // Double-click listener for viewing related cargos
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click detected
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int contractId = Integer.parseInt(model.getValueAt(row, 0).toString());
                        showCargoDialog(contractId);
                    }
                }
            }
        });
    
        tableFrame.setVisible(true);
    }
    private static void showCargoDialog(int contractId) {
        EntityManager em = emf.createEntityManager();
        Contract contract = em.find(Contract.class, contractId);
        
        if (contract != null) {
            // Create a dialog to display cargos related to the contract
            JFrame cargoFrame = new JFrame("Cargo for Contract " + contractId);
            cargoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
            // Prepare the table for displaying cargos
            DefaultTableModel cargoModel = new DefaultTableModel();
            cargoModel.addColumn("Cargo ID");
            cargoModel.addColumn("Cargo Name");
            cargoModel.addColumn("Cargo Quantity");
    
            // Fill the table with cargos related to the contract
            List<Cargo> cargos = contract.getCargos();
            for (Cargo cargo : cargos) {
                cargoModel.addRow(new Object[]{cargo.getIdObject(), cargo.getObjectName(), cargo.getQuantity()});
            }
    
            JTable cargoTable = new JTable(cargoModel);
            JScrollPane scrollPane = new JScrollPane(cargoTable);
            cargoFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    
            cargoFrame.setSize(400, 300);
            cargoFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Contract not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        em.close();
    }
        

    private static void load(DefaultTableModel model) {
        logger.info("Loading contract data from the database.");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        
        List<Contract> contracts = em.createQuery("SELECT f FROM Contract f WHERE f.contractId > 0", Contract.class).getResultList();
        model.setRowCount(0);  // Clear the model before adding new data
        
        for (Contract contract : contracts) {
            model.addRow(new Object[]{contract.getContractId(), contract.getCompany().getCompanyName(), contract.getContractDate()});
        }
        
        em.getTransaction().commit();
        em.close();
        logger.info("Loaded " + contracts.size() + " contracts.");
    }

    private static void delete(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // Подтверждение на удаление
            int confirmation = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this contract?", 
                "Delete Confirmation", 
                JOptionPane.YES_NO_OPTION);
    
            if (confirmation == JOptionPane.YES_OPTION) {
                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();
    
                Contract contract = em.find(Contract.class, Integer.parseInt(model.getValueAt(selectedRow, 0).toString()));
                if (contract != null) {
                    em.remove(contract);
                    em.getTransaction().commit();
                    model.removeRow(selectedRow);
                    logger.info("Contract with ID " + contract.getContractId() + " deleted successfully.");
                    JOptionPane.showMessageDialog(null, "Contract deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    logger.warning("Contract not found for deletion.");
                    JOptionPane.showMessageDialog(null, "Contract not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                em.close();
            } else {
                JOptionPane.showMessageDialog(null, "Contract deletion canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            logger.warning("No contract selected for deletion.");
            JOptionPane.showMessageDialog(null, "Select a row to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void add(DefaultTableModel model) {
        // Panel to hold input fields
        JPanel panel = new JPanel(new GridLayout(2, 2));
    
        // Fetch available companies for JComboBox
        EntityManager em = emf.createEntityManager();
        List<Company> companies = em.createQuery("SELECT f FROM Company f WHERE f.idcompany > 0", Company.class).getResultList();
        em.close();
    
        // JComboBox for selecting Company ID
        JComboBox<String> companyComboBox = new JComboBox<>();
        for (Company company : companies) {
            // Adding company name as the display value, but we store the company ID
            companyComboBox.addItem(company.getCompanyName() + " (ID: " + company.getIdcompany() + ")");
        }
    
        // Text field for Contract Date
        JTextField contractDateField = new JTextField();
    
        // Adding components to the panel
        panel.add(new JLabel("Select Company:"));
        panel.add(companyComboBox);
        panel.add(new JLabel("Contract End Date (yyyy-MM-dd):"));
        panel.add(contractDateField);
    
        // Show the dialog with the input fields
        int option = JOptionPane.showConfirmDialog(null, panel, "Enter Contract Details", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedCompany = (String) companyComboBox.getSelectedItem();
            String companyIdString = selectedCompany.split(" \\(ID: ")[1].replace(")", ""); // Extract the ID
            String contractDate = contractDateField.getText().trim();
    
            if (!companyIdString.isEmpty() && !contractDate.isEmpty()) {
                int companyId = Integer.parseInt(companyIdString);
                EntityManager emAdd = emf.createEntityManager();
                emAdd.getTransaction().begin();
    
                // Get the Company object based on selected Company ID
                Company company = emAdd.find(Company.class, companyId);
                if (company == null) {
                    logger.warning("Company with ID " + companyId + " not found.");
                    JOptionPane.showMessageDialog(null, "The company with ID " + companyId + " is not active.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                try {
                    LocalDate contractEndDate = LocalDate.parse(contractDate);
    
                    // Check if the contract date is earlier than 12th December 2024
                    LocalDate minDate = LocalDate.of(2024, 12, 12);
                    if (contractEndDate.isBefore(minDate)) {
                        JOptionPane.showMessageDialog(null, "The contract end date cannot be earlier than December 12, 2024.", "Date Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
    
                    // Create a new Contract object
                    Contract newContract = new Contract();
                    newContract.setCompany(company);
                    newContract.setContractDate(contractEndDate);
    
                    emAdd.persist(newContract);
                    emAdd.getTransaction().commit();
                    emAdd.close();
    
                    // Add the new contract details to the table
                    model.addRow(new Object[]{newContract.getContractId(), company.getCompanyName(), newContract.getContractDate()});
                    logger.info("New contract added with ID: " + newContract.getContractId());
                } catch (DateTimeParseException ex) {
                    logger.log(Level.SEVERE, "Invalid date format: " + contractDate, ex);
                    JOptionPane.showMessageDialog(null, "Invalid date format. Please use yyyy-MM-dd.", "Date Format Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                logger.warning("Input fields are empty during contract creation.");
                JOptionPane.showMessageDialog(null, "All fields must be filled in.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

private static void edit(DefaultTableModel model, JTable table) {
    int selectedRow = table.getSelectedRow();

    if (selectedRow != -1) {
        int contractId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());

        // Panel to hold input fields
        JPanel panel = new JPanel(new GridLayout(2, 2));

        // Fetch available companies for JComboBox
        EntityManager em = emf.createEntityManager();
        List<Company> companies = em.createQuery("SELECT f FROM Company f WHERE f.idcompany > 0", Company.class).getResultList();
        em.close();

        // JComboBox for selecting Company ID
        JComboBox<String> companyComboBox = new JComboBox<>();
        for (Company company : companies) {
            // Adding company name as the display value, but we store the company ID
            companyComboBox.addItem(company.getCompanyName() + " (ID: " + company.getIdcompany() + ")");
        }

        // Text field for Contract Date
        JTextField contractDateField = new JTextField(model.getValueAt(selectedRow, 2).toString());

        // Adding components to the panel
        panel.add(new JLabel("Select New Company:"));
        panel.add(companyComboBox);
        panel.add(new JLabel("New Contract End Date (yyyy-MM-dd):"));
        panel.add(contractDateField);

        // Show the dialog with the input fields
        int option = JOptionPane.showConfirmDialog(null, panel, "Edit Contract Details", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedCompany = (String) companyComboBox.getSelectedItem();
            String companyIdString = selectedCompany.split(" \\(ID: ")[1].replace(")", ""); // Extract the ID
            String contractDate = contractDateField.getText().trim();

            if (!companyIdString.isEmpty() && !contractDate.isEmpty()) {
                int companyId = Integer.parseInt(companyIdString);
                EntityManager emEdit = emf.createEntityManager();
                emEdit.getTransaction().begin();

                Contract contract = emEdit.find(Contract.class, contractId);
                if (contract == null) {
                    logger.warning("Contract with ID " + contractId + " not found.");
                    JOptionPane.showMessageDialog(null, "Contract not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Company newCompany = emEdit.find(Company.class, companyId);
                if (newCompany == null) {
                    logger.warning("Company with ID " + companyId + " not found.");
                    JOptionPane.showMessageDialog(null, "The company with ID " + companyId + " is not active.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    LocalDate newContractEndDate = LocalDate.parse(contractDate);

                    // Check if the contract date is earlier than 12th December 2024
                    LocalDate minDate = LocalDate.of(2024, 12, 12);
                    if (newContractEndDate.isBefore(minDate)) {
                        JOptionPane.showMessageDialog(null, "The contract end date cannot be earlier than December 12, 2024.", "Date Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    contract.setCompany(newCompany);
                    contract.setContractDate(newContractEndDate);

                    emEdit.getTransaction().commit();
                    emEdit.close();

                    // Update the table with new details
                    model.setValueAt(companyId, selectedRow, 1);
                    model.setValueAt(contractDate, selectedRow, 2);
                    logger.info("Contract with ID " + contractId + " updated.");
                } catch (DateTimeParseException ex) {
                    logger.log(Level.SEVERE, "Invalid date format: " + contractDate, ex);
                    JOptionPane.showMessageDialog(null, "Invalid date format. Please use yyyy-MM-dd.", "Date Format Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                logger.warning("Input fields are empty during contract edit.");
                JOptionPane.showMessageDialog(null, "All fields must be filled in.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } else {
        logger.warning("No contract selected for editing.");
        JOptionPane.showMessageDialog(null, "Select a row to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
    }
}

}
