package com.hibernate;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.UnitValue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class ReportGenerator {

    public static void generateCargoReport(String outputPath) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_persistence");
        EntityManager em = emf.createEntityManager();

        try {
            // �����祭�� ������ �� ���� ������
            List<Cargo> cargos = em.createQuery("SELECT c FROM Cargo c", Cargo.class).getResultList();

            // �������� PDF
            PdfWriter writer = new PdfWriter(outputPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ���������
            document.add(new Paragraph("Cargo Report")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
            );

            // �������� ⠡����
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 3}));
            table.setWidth(UnitValue.createPercentValue(100));

            // ��������� ⠡����
            table.addHeaderCell(new Cell().add(new Paragraph("ID")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Object Name")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Quantity")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Shelf ID")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Contract Number")).setBold());

            // ���������� ⠡���� ����묨
            for (Cargo cargo : cargos) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(cargo.getIdObject()))));
                table.addCell(new Cell().add(new Paragraph(cargo.getObjectName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(cargo.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(cargo.getShelf().getShelfId()))));

                // ���������� ����� ����ࠪ�
                String contractNumber = cargo.getContract() != null ? String.valueOf(cargo.getContract().getContractId()) : "N/A";
                table.addCell(new Cell().add(new Paragraph(contractNumber)));

            }

            // ���������� ⠡���� � ���㬥��
            document.add(table);

            // �����⨥ ���㬥��
            document.close();
            System.out.println("Report generated successfully at " + outputPath);
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
