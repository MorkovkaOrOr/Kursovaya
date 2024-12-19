package com.hibernate;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;





public class Entitu {
    public static final Logger logger = Logger.getLogger(Entitu.class.getName());
    private static void setupLogger() {
        try {
            // ����ந� ���᮫�� ��ࠡ��稪
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL); // �஢��� ����஢����: �� ᮮ�饭��
            logger.addHandler(consoleHandler);

            // ����ந� ��ࠡ��稪 ��� ����� ����� � 䠩�
            FileHandler fileHandler = new FileHandler("application.log", true); // true ��� ���������� � 䠩�
            fileHandler.setLevel(Level.ALL); // �஢��� ����஢����: �� ᮮ�饭��
            fileHandler.setFormatter(new SimpleFormatter()); // �ᯮ��㥬 ���⮩ �ଠ�
            logger.addHandler(fileHandler);

            logger.info("Logger setup complete.");
        } catch (Exception e) {
            System.err.println("Error setting up logger: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void run()
    {
        setupLogger();
        JFrame frame = new JFrame("Button Table Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel buttonPanel = new JPanel();
        
        buttonPanel.setLayout(new FlowLayout());

        JPanel reportButtonPanel = new JPanel();
        reportButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the button


        frame.getContentPane().add(reportButtonPanel, BorderLayout.SOUTH);
        JButton roomsBtn = new JButton("Rooms");
        JButton addBtn = new JButton("Shelves");
        JButton removeBtn = new JButton("Contracts");
        JButton objectsBtn = new JButton("Objects");
        JButton companiesBtn = new JButton("Companies");
        
        buttonPanel.add(roomsBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(objectsBtn);
        buttonPanel.add(companiesBtn);


        frame.getContentPane().add(buttonPanel, BorderLayout.CENTER);

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ShelfService.showShelfTable();
            }
        });

        removeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ContractService.showJudgeTable();
            }
        });

        objectsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ObjectService.showTable();
            }
        });

        companiesBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompanyService.showJudgeTable();
            }
        });
        roomsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RoomService.showRoomTable();
            }
        });


        frame.setSize(400, 200);
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        run();
    }
}
