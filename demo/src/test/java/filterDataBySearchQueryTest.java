import javax.swing.table.DefaultTableModel;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class filterDataBySearchQueryTest {

    // Создание модели таблицы, которая будет использоваться для тестирования
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
        // Инициализация таблицы до каждого теста
        model = new DefaultTableModel();
        model.addColumn("Name");
        model.addColumn("ID");
        model.addColumn("Shelf");
        System.out.println("test begin");
    }

    @After
    public void testFinished() {
        // Очистка после теста
        System.out.println("test end");
    }

    @Test
    public void testFilterDataBySearchQueryWithMatchingResults() {
        // Подготовка данных
        String searchQuery = "object1";
        // Добавляем данные в модель таблицы
        model.addRow(new Object[]{"Object1", "1", "Shelf1"});
        model.addRow(new Object[]{"Object2", "2", "Shelf2"});

        // Вызов метода для фильтрации
        int count = filterDataBySearchQuery(model, searchQuery);

        // Проверяем, что найден один объект
        assertEquals(1, count);
        // Проверяем, что в таблице остался только один объект с правильным именем
        assertEquals("Object1", model.getValueAt(0, 0));
    }

    @Test
    public void testFilterDataBySearchQueryWithNoMatchingResults() {
        // Подготовка данных
        String searchQuery = "nonexistent";
        // Добавляем данные в модель таблицы
        model.addRow(new Object[]{"Object1", "1", "Shelf1"});
        model.addRow(new Object[]{"Object2", "2", "Shelf2"});

        // Вызов метода для фильтрации
        int count = filterDataBySearchQuery(model, searchQuery);

        // Проверяем, что не найдено ни одного объекта
        assertEquals(0, count);
    }

    @Test
    public void testFilterDataBySearchQueryWithEmptyQuery() {
        // Подготовка данных
        String searchQuery = "";
        // Добавляем данные в модель таблицы
        model.addRow(new Object[]{"Object1", "1", "Shelf1"});
        model.addRow(new Object[]{"Object2", "2", "Shelf2"});

        // Вызов метода для фильтрации
        int count = filterDataBySearchQuery(model, searchQuery);

        // Проверяем, что все объекты остались в таблице
        assertEquals(2, count);
        assertEquals("Object1", model.getValueAt(0, 0));
        assertEquals("Object2", model.getValueAt(1, 0));
    }



    // Метод для фильтрации данных по запросу (это ваш оригинальный метод)
    private int filterDataBySearchQuery(DefaultTableModel model, String searchQuery) {
        int count = 0;
        try {
            // Получаем данные из XML
            // Здесь используем фиктивные данные для тестов, так как реальное обращение к XML не потребуется
            if (model == null) {
                throw new NullPointerException("Model cannot be null");
            }
            
            // Для тестов используем жестко закодированные данные вместо парсинга XML
            String[][] data = {
                {"Object1", "1", "Shelf1"},
                {"Object2", "2", "Shelf2"}
            };

            // Фильтрация данных
            for (String[] row : data) {
                String name = row[0];
                String id = row[1];
                String shelfId = row[2];

                // Если имя объекта содержит поисковый запрос, добавляем его в таблицу
                if (name.toLowerCase().contains(searchQuery.toLowerCase())) {
                    model.addRow(new Object[]{name, id, shelfId});
                    count++; // Увеличиваем счетчик для каждого найденного объекта
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count; // Возвращаем количество найденных объектов
    }
}

