package com.hibernate;

import javax.persistence.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;

public class CompanyService {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CompanyService.class.getName());

    public static void showJudgeTable() {
        JFrame tableFrame = new JFrame("Company");
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);

        model.addColumn("ID");
        model.addColumn("Company Name");
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
        tableFrame.setVisible(true);
    }

    private static void load(DefaultTableModel model) {
        logger.info("Loading company data from the database.");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        List<Company> companies = em.createQuery("SELECT f FROM Company f WHERE f.idcompany > 0", Company.class).getResultList();

        model.setRowCount(0); // Clear existing rows

        if (companies.isEmpty()) {
            logger.warning("No companies found in the database.");
        } else {
            for (Company company : companies) {
                model.addRow(new Object[]{company.getIdcompany(), company.getCompanyName()});
            }
        }

        em.getTransaction().commit();
        em.close();
    }

    private static void delete(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int confirmation = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this company?", 
                "Delete Confirmation", 
                JOptionPane.YES_NO_OPTION);
    
            if (confirmation == JOptionPane.YES_OPTION) {
                EntityManager em = null;
                try {
                    em = emf.createEntityManager();
                    em.getTransaction().begin();

                    Company company = em.find(Company.class, Integer.parseInt(model.getValueAt(selectedRow, 0).toString()));

                    if (company != null) {
                        em.remove(company);
                        em.getTransaction().commit();
                        model.removeRow(selectedRow);
                        logger.info("Company with ID " + company.getIdcompany() + " deleted successfully.");
                        JOptionPane.showMessageDialog(null, "Company deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        logger.warning("Company not found for deletion.");
                        JOptionPane.showMessageDialog(null, "Company not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    if (em != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    logger.log(Level.SEVERE, "An error occurred while deleting the company.", e);
                    JOptionPane.showMessageDialog(null, "An error occurred while deleting the company: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (em != null) {
                        em.close();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Company deletion canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            logger.warning("No company selected for deletion.");
            JOptionPane.showMessageDialog(null, "Select a row to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void add(DefaultTableModel model) {
        String name = JOptionPane.showInputDialog("Enter Company Name:");
        if (name != null && !name.trim().isEmpty()) {
            EntityManager em = null;
            try {
                em = emf.createEntityManager();
                em.getTransaction().begin();

                Company newCompany = new Company();
                newCompany.setCompanyName(name);
                em.persist(newCompany);

                em.getTransaction().commit();
                logger.info("New company added with ID: " + newCompany.getIdcompany());

                model.addRow(new Object[]{newCompany.getIdcompany(), newCompany.getCompanyName()});
                JOptionPane.showMessageDialog(null, "Company added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                logger.log(Level.SEVERE, "An error occurred while adding the company.", e);
                JOptionPane.showMessageDialog(null, "An error occurred while adding the company: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Company name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void edit(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String newName = JOptionPane.showInputDialog("Enter new Company Name:", model.getValueAt(selectedRow, 1));
    
            if (newName != null && !newName.trim().isEmpty()) {
                EntityManager em = null;
                try {
                    em = emf.createEntityManager();
                    em.getTransaction().begin();

                    Company company = em.find(Company.class, Integer.parseInt(model.getValueAt(selectedRow, 0).toString()));
                    
                    if (company == null) {
                        logger.warning("Company not found for editing.");
                        JOptionPane.showMessageDialog(null, "Company not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    company.setCompanyName(newName);
                    em.getTransaction().commit();

                    model.setValueAt(newName, selectedRow, 1);
                    logger.info("Company with ID " + company.getIdcompany() + " updated successfully.");
                    JOptionPane.showMessageDialog(null, "Company updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    if (em != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    logger.log(Level.SEVERE, "An error occurred while editing the company.", e);
                    JOptionPane.showMessageDialog(null, "An error occurred while editing the company: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (em != null) {
                        em.close();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Company name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            logger.warning("No company selected for editing.");
            JOptionPane.showMessageDialog(null, "Select a row to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
