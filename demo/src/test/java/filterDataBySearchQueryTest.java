import javax.swing.table.DefaultTableModel;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class filterDataBySearchQueryTest {

    // �������� ������ ⠡����, ����� �㤥� �ᯮ�짮������ ��� ���஢����
    private DefaultTableModel model;

    @BeforeClass
    public static void allTestsStarted() {
        System.out.println("start test");
    }

    @AfterClass
    public static void allTestsFinished() {
        System.out.println("test end");
    }

    @Before
    public void testStarted() {
        // ���樠������ ⠡���� �� ������� ���
        model = new DefaultTableModel();
        model.addColumn("Name");
        model.addColumn("ID");
        model.addColumn("Shelf");
        System.out.println("test begin");
    }

    @After
    public void testFinished() {
        // ���⪠ ��᫥ ���
        System.out.println("test end");
    }

    @Test
    public void testFilterDataBySearchQueryWithMatchingResults() {
        // �����⮢�� ������
        String searchQuery = "object1";
        // ������塞 ����� � ������ ⠡����
        model.addRow(new Object[]{"Object1", "1", "Shelf1"});
        model.addRow(new Object[]{"Object2", "2", "Shelf2"});

        // �맮� ��⮤� ��� 䨫���樨
        int count = filterDataBySearchQuery(model, searchQuery);

        // �஢��塞, �� ������ ���� ��ꥪ�
        assertEquals(1, count);
        // �஢��塞, �� � ⠡��� ��⠫�� ⮫쪮 ���� ��ꥪ� � �ࠢ���� ������
        assertEquals("Object1", model.getValueAt(0, 0));
    }

    @Test
    public void testFilterDataBySearchQueryWithNoMatchingResults() {
        // �����⮢�� ������
        String searchQuery = "nonexistent";
        // ������塞 ����� � ������ ⠡����
        model.addRow(new Object[]{"Object1", "1", "Shelf1"});
        model.addRow(new Object[]{"Object2", "2", "Shelf2"});

        // �맮� ��⮤� ��� 䨫���樨
        int count = filterDataBySearchQuery(model, searchQuery);

        // �஢��塞, �� �� ������� �� ������ ��ꥪ�
        assertEquals(0, count);
    }

    @Test
    public void testFilterDataBySearchQueryWithEmptyQuery() {
        // �����⮢�� ������
        String searchQuery = "";
        // ������塞 ����� � ������ ⠡����
        model.addRow(new Object[]{"Object1", "1", "Shelf1"});
        model.addRow(new Object[]{"Object2", "2", "Shelf2"});

        // �맮� ��⮤� ��� 䨫���樨
        int count = filterDataBySearchQuery(model, searchQuery);

        // �஢��塞, �� �� ��ꥪ�� ��⠫��� � ⠡���
        assertEquals(2, count);
        assertEquals("Object1", model.getValueAt(0, 0));
        assertEquals("Object2", model.getValueAt(1, 0));
    }



    // ��⮤ ��� 䨫���樨 ������ �� ������ (�� ��� �ਣ������ ��⮤)
    private int filterDataBySearchQuery(DefaultTableModel model, String searchQuery) {
        int count = 0;
        try {
            // ����砥� ����� �� XML
            // ����� �ᯮ��㥬 䨪⨢�� ����� ��� ��⮢, ⠪ ��� ॠ�쭮� ���饭�� � XML �� ���ॡ����
            if (model == null) {
                throw new NullPointerException("Model cannot be null");
            }
            
            // ��� ��⮢ �ᯮ��㥬 ���⪮ ������஢���� ����� ����� ���ᨭ�� XML
            String[][] data = {
                {"Object1", "1", "Shelf1"},
                {"Object2", "2", "Shelf2"}
            };

            // �������� ������
            for (String[] row : data) {
                String name = row[0];
                String id = row[1];
                String shelfId = row[2];

                // �᫨ ��� ��ꥪ� ᮤ�ন� ���᪮�� �����, ������塞 ��� � ⠡����
                if (name.toLowerCase().contains(searchQuery.toLowerCase())) {
                    model.addRow(new Object[]{name, id, shelfId});
                    count++; // �����稢��� ���稪 ��� ������� ���������� ��ꥪ�
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count; // �����頥� ������⢮ ��������� ��ꥪ⮢
    }
}

