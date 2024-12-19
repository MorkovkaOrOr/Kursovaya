package com.hibernate;

import java.awt.BorderLayout;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class RoomService {
    
    private static final java.util.logging.Logger logger = Entitu.logger; 
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");

    public static void showRoomTable() {
        logger.info("Opening the Room table.");
        JFrame tableFrame = new JFrame("Rooms");
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);

        model.addColumn("ID");
        model.addColumn("Occupied Space");
        model.addColumn("Max Shelves");
        model.addColumn("Free Space for Shelves");

        JLabel totalOccupiedLabel = new JLabel("Total Occupied Space: 0");
        totalOccupiedLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel freeSpacePercentageLabel = new JLabel("Free Space Percentage: 0%");
        freeSpacePercentageLabel.setHorizontalAlignment(JLabel.CENTER);

        load(model, totalOccupiedLabel, freeSpacePercentageLabel); // Initial load and total occupied calculation

        JScrollPane scrollPane = new JScrollPane(table);
        tableFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        tableFrame.setSize(600, 400);

        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");

        addBtn.addActionListener(e -> add(model, totalOccupiedLabel, freeSpacePercentageLabel));
        deleteBtn.addActionListener(e -> delete(model, table, totalOccupiedLabel, freeSpacePercentageLabel));

        JPanel controlPanel = new JPanel();
        controlPanel.add(addBtn);
        controlPanel.add(deleteBtn);
        controlPanel.add(totalOccupiedLabel);
        controlPanel.add(freeSpacePercentageLabel);

        tableFrame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
        tableFrame.setVisible(true);
    }

    private static void load(DefaultTableModel model, JLabel totalOccupiedLabel, JLabel freeSpacePercentageLabel) {
        logger.info("Loading room data from the database.");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
    
        List<Room> rooms = em.createQuery("SELECT r FROM Room r", Room.class).getResultList();
    
        model.setRowCount(0);
    
        int totalOccupied = 0;
        int totalQuantity = 0; // Суммарное количество пространства
        int totalFreeSpace = 0; // Суммарное свободное пространство
    
        if (rooms.isEmpty()) {
            logger.warning("No rooms found in the database.");
        } else {
            for (Room room : rooms) {
                int roomOccupiedSpace = 0;
                int roomTotalSpace = 0;
                int roomFreeSpace = 0;
    
                // Проходим по всем полкам в комнате
                for (Shelf shelf : room.getShelves()) {
                    roomTotalSpace += shelf.getQuantity(); // Общее пространство на полке
                    roomOccupiedSpace += shelf.getQuantity() - shelf.getAvailableSpace(); // Занятое пространство
                    roomFreeSpace += shelf.getAvailableSpace(); // Свободное пространство
                }
    
                // Добавляем данные комнаты в таблицу
                model.addRow(new Object[]{
                    room.getRoomId(),
                    roomOccupiedSpace,
                    room.getMaxShelves(),
                    room.getFreeShelfSpace()
                });
    
                // Суммируем данные по всем комнатам
                totalOccupied += roomOccupiedSpace;
                totalQuantity += roomTotalSpace;
                totalFreeSpace += roomFreeSpace;
            }
        }
    
        // Рассчитываем процент свободного пространства
        double freeSpacePercentage = (double) totalFreeSpace / totalQuantity * 100;
        freeSpacePercentageLabel.setText("Free Space Percentage: " + String.format("%.2f", freeSpacePercentage) + "%");
    
        totalOccupiedLabel.setText("Total Occupied Space: " + totalOccupied);
    
        em.getTransaction().commit();
        em.close();
    }
    

    private static void delete(DefaultTableModel model, JTable table, JLabel totalOccupiedLabel, JLabel freeSpacePercentageLabel) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int confirmation = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this room?", 
                "Delete Confirmation", 
                JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                EntityManager em = null;
                try {
                    em = emf.createEntityManager();
                    em.getTransaction().begin();

                    Room room = em.find(Room.class, Integer.parseInt(model.getValueAt(selectedRow, 0).toString()));

                    if (room != null) {
                        em.remove(room);
                        em.getTransaction().commit();
                        model.removeRow(selectedRow);
                        logger.info("Room with ID " + room.getRoomId() + " was deleted successfully.");
                        JOptionPane.showMessageDialog(null, "Room deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        logger.warning("Room not found with ID: " + model.getValueAt(selectedRow, 0));
                        JOptionPane.showMessageDialog(null, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    if (em != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    logger.severe("Error occurred while deleting the room: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "An error occurred while deleting the room: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (em != null) {
                        em.close();
                    }
                }

                load(model, totalOccupiedLabel, freeSpacePercentageLabel);
            } else {
                logger.info("Room deletion was canceled by the user.");
                JOptionPane.showMessageDialog(null, "Room deletion canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            logger.warning("No row selected for deletion.");
            JOptionPane.showMessageDialog(null, "Select a row to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void add(DefaultTableModel model, JLabel totalOccupiedLabel, JLabel freeSpacePercentageLabel) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();

            try {
                Room newRoom = new Room();

                String maxShelvesInput = JOptionPane.showInputDialog("Enter max shelves for the new room:");

                    try {
                        int maxShelves = Integer.parseInt(maxShelvesInput);
                        newRoom.setMaxShelves(maxShelves);
                        logger.info("Roomjgjhgfhfghfghfgh " + newRoom.getRoomId() + " added successfully.");
                    } catch (NumberFormatException e) {
                        logger.warning("Invalid number format for maxShelves: " + maxShelvesInput);
                        JOptionPane.showMessageDialog(null, "Please enter a valid integer for max shelves", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                

                em.persist(newRoom);
                em.getTransaction().commit();

                logger.info("Room with ID " + newRoom.getRoomId() + " added successfully.");

                model.addRow(new Object[]{
                    newRoom.getRoomId(),
                    newRoom.getOccupiedSpace(),
                    newRoom.getMaxShelves(),
                    newRoom.getFreeShelfSpace()
                });

                load(model, totalOccupiedLabel, freeSpacePercentageLabel);
            } catch (NumberFormatException e) {
                logger.warning("Invalid input for maxShelves.");
                JOptionPane.showMessageDialog(null, "Please enter a valid integer", "Input Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.severe("Error occurred while adding the room: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "An error occurred while adding the room: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
