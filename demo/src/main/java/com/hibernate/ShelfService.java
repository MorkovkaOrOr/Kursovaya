package com.hibernate;

import javax.persistence.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;

public class ShelfService {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");

    // Using the logger from the Entitu class
    private static final java.util.logging.Logger logger = Entitu.logger;

    public static void showShelfTable() {
        logger.info("Opening the Shelf Management table.");
    
        JFrame shelfFrame = new JFrame("Shelf Management");
        shelfFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        model.addColumn("Shelf ID");
        model.addColumn("Max Quantity");
        model.addColumn("Room ID");
        model.addColumn("Available Space");
        load(model);
    
        JScrollPane scrollPane = new JScrollPane(table);
        shelfFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    
        // Buttons for adding, deleting, and editing shelves
        JButton addShelfButton = new JButton("Add Shelf");
        addShelfButton.addActionListener(e -> addShelf(model));
    
        JButton deleteShelfButton = new JButton("Delete Shelf");
        deleteShelfButton.addActionListener(e -> deleteShelf(model, table));
    
        JButton editShelfButton = new JButton("Edit Shelf");
        editShelfButton.addActionListener(e -> editShelf(model, table));
    
        JPanel controlPanel = new JPanel();
        controlPanel.add(addShelfButton);
        controlPanel.add(deleteShelfButton);
        controlPanel.add(editShelfButton);  // Edit button
        shelfFrame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
    
        // Double-click event handler on the shelf
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // If double-click is detected
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        int shelfId = (int) model.getValueAt(selectedRow, 0);
                        showProductsOnShelf(shelfId); // Show products on the shelf
                    }
                }
            }
        });
    
        shelfFrame.setSize(500, 300);
        shelfFrame.setVisible(true);
    }
    
    private static void showProductsOnShelf(int shelfId) {
        logger.info("Displaying products on shelf ID: " + shelfId);
    
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
    
        Shelf shelf = em.find(Shelf.class, shelfId);
        if (shelf == null) {
            JOptionPane.showMessageDialog(null, "Shelf not found.", "Error", JOptionPane.ERROR_MESSAGE);
            em.getTransaction().rollback();
            em.close();
            return;
        }
    
        // Create a window to display products
        JFrame cargoFrame = new JFrame("Products on Shelf " + shelfId);
        DefaultTableModel cargoModel = new DefaultTableModel();
        JTable cargoTable = new JTable(cargoModel);
        cargoModel.addColumn("Cargo ID");
        cargoModel.addColumn("Cargo Name");
        cargoModel.addColumn("Cargo Quantity");

        // Fill the table with cargos related to the shelf
        List<Cargo> cargos = shelf.getCargos();
        for (Cargo cargo : cargos) {
            cargoModel.addRow(new Object[]{cargo.getIdObject(), cargo.getObjectName(), cargo.getQuantity()});
        }
    
        JScrollPane cargoScrollPane = new JScrollPane(cargoTable);
        cargoFrame.getContentPane().add(cargoScrollPane, BorderLayout.CENTER);
        cargoFrame.setSize(400, 300);
        cargoFrame.setVisible(true);
    
        em.getTransaction().commit();
        em.close();
    }

    private static void load(DefaultTableModel model) {
        logger.info("Loading shelf data from the database.");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        List<Shelf> shelves = em.createQuery("SELECT s FROM Shelf s", Shelf.class).getResultList();

        model.setRowCount(0); // Clear the table before adding data

        for (Shelf shelf : shelves) {
            model.addRow(new Object[]{shelf.getShelfId(), shelf.getQuantity(), shelf.getRoom().getRoomId(), shelf.getAvailableSpace()});
        }

        em.getTransaction().commit();
        em.close();
    }

    private static void addShelf(DefaultTableModel model) {
        logger.info("Adding a new shelf.");
        EntityManager em = emf.createEntityManager();
        List<Room> rooms = em.createQuery("SELECT r FROM Room r", Room.class).getResultList();

        // Create a dropdown list to select a room
        JComboBox<Room> roomComboBox = new JComboBox<>(rooms.toArray(new Room[0]));
        roomComboBox.setRenderer(new ListCellRenderer<Room>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Room> list, Room value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel("Room ID: " + value.getRoomId() + " Available space: " + value.getFreeShelfSpace()); // Display Room ID
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JTextField quantityField = new JTextField();

        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Select Room:"));
        panel.add(roomComboBox);

        int option = JOptionPane.showConfirmDialog(null, panel, "Add New Shelf", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String quantityStr = quantityField.getText().trim();
            if (!quantityStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(null, "Quantity must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Room selectedRoom = (Room) roomComboBox.getSelectedItem();
                    if (selectedRoom == null) {
                        JOptionPane.showMessageDialog(null, "Please select a valid Room.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Check how many shelves are already in the room and if it exceeds maxShelves
                    if (selectedRoom.getShelves().size() >= selectedRoom.getMaxShelves()) {
                        JOptionPane.showMessageDialog(null, "The room has reached its maximum shelf capacity.", "Capacity Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Add the shelf to the database
                    em.getTransaction().begin();
                    Shelf newShelf = new Shelf();
                    newShelf.setQuantity(quantity);
                    newShelf.setRoom(selectedRoom);

                    em.persist(newShelf);
                    em.getTransaction().commit();

                    // Update the table
                    model.addRow(new Object[]{newShelf.getShelfId(), newShelf.getQuantity(), newShelf.getRoom().getRoomId()});

                    logger.info("Shelf added successfully.");
                    JOptionPane.showMessageDialog(null, "Shelf added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    logger.log(Level.SEVERE, "Invalid quantity format while adding a shelf.", e);
                    JOptionPane.showMessageDialog(null, "Invalid quantity format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "An error occurred while adding the shelf.", e);
                    JOptionPane.showMessageDialog(null, "An error occurred while adding the shelf: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "All fields must be filled in.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        em.close();
    }

    private static void deleteShelf(DefaultTableModel model, JTable table) {
        logger.info("Attempting to delete a shelf.");
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a shelf to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int shelfId = (int) model.getValueAt(selectedRow, 0); // Get the ID of the selected shelf

        // Request confirmation for deletion
        int confirmation = JOptionPane.showConfirmDialog(
            null, 
            "Are you sure you want to delete this shelf?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Shelf shelfToDelete = em.find(Shelf.class, shelfId);
            if (shelfToDelete != null) {
                try {
                    em.remove(shelfToDelete);
                    em.getTransaction().commit();

                    model.removeRow(selectedRow);
                    logger.info("Shelf deleted successfully.");
                    JOptionPane.showMessageDialog(null, "Shelf deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    em.getTransaction().rollback();
                    logger.log(Level.SEVERE, "An error occurred while deleting the shelf.", e);
                    JOptionPane.showMessageDialog(null, "An error occurred while deleting the shelf: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                em.getTransaction().rollback();
                JOptionPane.showMessageDialog(null, "Shelf not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            em.close();
        }
    }

    private static void editShelf(DefaultTableModel model, JTable table) {
        logger.info("Attempting to edit a shelf.");
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a shelf to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int shelfId = (int) model.getValueAt(selectedRow, 0); // Get the ID of the selected shelf
        int currentQuantity = (int) model.getValueAt(selectedRow, 1); // Current shelf capacity
        Shelf shelfToEdit = null;
    
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        shelfToEdit = em.find(Shelf.class, shelfId);
    
        if (shelfToEdit == null) {
            JOptionPane.showMessageDialog(null, ShelfNotFoundException.MESSAGE, "Error", JOptionPane.ERROR_MESSAGE);
            em.getTransaction().rollback();
            em.close();
            return;
        }
    
        List<Room> rooms = em.createQuery("SELECT r FROM Room r", Room.class).getResultList();
        JComboBox<Room> roomComboBox = new JComboBox<>(rooms.toArray(new Room[0]));
        roomComboBox.setRenderer(new ListCellRenderer<Room>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Room> list, Room value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel("Room ID: " + value.getRoomId() + " Available space: " + value.getFreeShelfSpace());
            }
        });
    
        roomComboBox.setSelectedItem(shelfToEdit.getRoom());
    
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
    
        JTextField quantityField = new JTextField(String.valueOf(currentQuantity));
    
        panel.add(new JLabel("New Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Select Room:"));
        panel.add(roomComboBox);
    
        int option = JOptionPane.showConfirmDialog(null, panel, "Edit Shelf", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String newQuantityStr = quantityField.getText().trim();
            if (newQuantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, QuantityEmptyException.MESSAGE, "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            try {
                int newQuantity = Integer.parseInt(newQuantityStr);
                if (newQuantity - shelfToEdit.getQuantity() + shelfToEdit.getAvailableSpace() < 0) {
                    throw new InvalidQuantityException();
                }

                if (newQuantity <= 0) {
                    throw new InvalidQuantityException(); 
                }
    
                Room newRoom = (Room) roomComboBox.getSelectedItem();
                if (newRoom == null) {
                    throw new InvalidRoomSelectionException(); 
                }
    
                if (newRoom.getShelves().size() >= newRoom.getMaxShelves()) {
                    throw new RoomCapacityExceededException(); 
                }
    
                shelfToEdit.setQuantity(newQuantity);
                shelfToEdit.setRoom(newRoom);
                em.getTransaction().commit();
    
                model.setValueAt(newQuantity, selectedRow, 1);
                model.setValueAt(newRoom.getRoomId(), selectedRow, 2);
    
                logger.info("Shelf updated successfully.");
                JOptionPane.showMessageDialog(null, "Shelf updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    
            } catch (InvalidQuantityException | InvalidRoomSelectionException | RoomCapacityExceededException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                logger.log(Level.SEVERE, InvalidQuantityFormatException.MESSAGE, e);
                JOptionPane.showMessageDialog(null, InvalidQuantityFormatException.MESSAGE, "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                logger.log(Level.SEVERE, GenericException.MESSAGE, e);
                JOptionPane.showMessageDialog(null, GenericException.MESSAGE, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    
        em.close();
    }
}
