import org.apache.poi.xwpf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MyApp {
    private static Random random = new Random();
    private static Connection connection;
    private static PreparedStatement preparedStatement;
    private static String CREATE_ADVERT = "insert into advertisement" +
            "(title, text, date, categoryId, price, currency, userId, status, id) values" +
            "(?,?,?,?,?,?,?,?,?);";
    private static String CREATE_CATEGORY = "insert into category" +
            "(id, category, parentId) values (?,?,?)";
    private static String CREATE_USER = "insert into user" +
            "(name, email, password, phone, status, type, dislikeAmount, picture, id) values" +
            "(?,?,?,?,?,?,?,?,?)";
    private static String CREATE_SUB_CATEGORY = "insert into category(category, parentId) values (?,?)";

    private static String DELETE_USERS = "DELETE FROM user WHERE id > 0;";
    private static String DELETE_CATEGORY = "DELETE FROM category WHERE id > 0;";
    private static String DELETE_ADVERT = "DELETE FROM advertisement WHERE id > 0;";

    private static String UPDATE_USER_ID = "SET  @num := 0;\n" +
            "UPDATE salesa.user SET id = @num := (@num+1);\n" +
            "ALTER TABLE salesa.user AUTO_INCREMENT =1;";
    private static String UPDATE_CATEGORY_ID = "SET  @num := 0;\n" +
            "UPDATE salesa.category SET id = @num := (@num+1);\n" +
            "ALTER TABLE salesa.category AUTO_INCREMENT =1;";
    private static String UPDATE_ADVERTISEMENT_ID = "SET  @num := 0;\n" +
            "UPDATE salesa.advertisement SET id = @num := (@num+1);\n" +
            "ALTER TABLE salesa.advertisement AUTO_INCREMENT =1;";


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        XWPFDocument advertDoc = new XWPFDocument(new FileInputStream("ADVERT_LIST.docx"));
        XWPFDocument categoryDoc = new XWPFDocument(new FileInputStream("CATEGORY_LIST.docx"));

        deleteUsers(DELETE_USERS);
        deleteCategory(DELETE_CATEGORY);
        deleteAdvert(DELETE_ADVERT);

        createUser();
        createCategory(categoryDoc);
        createAdvert(advertDoc);

        extractImages("ADVERT_LIST.docx");

//        updateUserId(UPDATE_USER_ID);
//        updateCategoryId(UPDATE_CATEGORY_ID);
//        updateAdvertisementId(UPDATE_ADVERTISEMENT_ID);
    }

    public static void createCategory(XWPFDocument docx) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(CREATE_CATEGORY);
        List<XWPFTable> tables = docx.getTables();
        XWPFTable xwpfTable = tables.get(0);

        for (int i = 0; i < xwpfTable.getRows().size(); i++) {
            XWPFTableRow row = xwpfTable.getRow(i);
            XWPFTableCell cell = row.getCell(1);
            preparedStatement.setInt(1, i + 1);
            preparedStatement.setString(2, cell.getText());
            preparedStatement.setInt(3, 0);
            preparedStatement.executeUpdate();
        }

        // Implement sub category query

        preparedStatement = connection.prepareStatement(CREATE_SUB_CATEGORY);

        for (int i = 0; i < xwpfTable.getRows().size(); i++) {
            XWPFTableRow row = xwpfTable.getRow(i);
            XWPFTableCell tableCell = row.getCell(2);
            //Split by UpperCase
            String[] subCategoryArray = tableCell.getText().split("(?<=.)(?=\\p{Lu})");

            for (String aSubCategoryArray : subCategoryArray) {
                if (!aSubCategoryArray.equals("")) {
                    preparedStatement.setString(1, aSubCategoryArray);
                    preparedStatement.setInt(2, i + 1);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    public static void createUser() throws SQLException, ClassNotFoundException {
        //Create random users depends on userCount
        int userCount = 5;
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(CREATE_USER);
        //Fill user data
        for (int i = 0; i < userCount; i++) {
            String userName = "User_0" + i;
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userName + "@example.com");
            preparedStatement.setString(3, "password");
            preparedStatement.setString(4, "0-900-0000000");
            preparedStatement.setString(5, "U");
            preparedStatement.setString(6, "A");
            preparedStatement.setInt(7, random.nextInt(10));
            preparedStatement.setString(8, "null");
            preparedStatement.setInt(9, i + 1);
            preparedStatement.executeUpdate();
        }
    }

    public static void createAdvert(XWPFDocument docx) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(CREATE_ADVERT);

        List<XWPFTable> tables = docx.getTables();
        for (int i = 0; i < tables.size(); i++) {
            XWPFTable table = tables.get(i);
            XWPFTableRow x = table.getRow(0);
            XWPFTableRow x2 = table.getRow(1);
            java.util.Date javaDate = new java.util.Date();
            Date date = new Date(javaDate.getTime());
            int randId = random.nextInt(12) + 1;
            int userId = random.nextInt(3) + 1;

            preparedStatement.setString(1, x.getCell(1).getText());     //title
            preparedStatement.setString(2, x2.getCell(2).getText());    //text
            preparedStatement.setDate(3, date);                         //date
            preparedStatement.setInt(4, randId);                         //categoryId
            preparedStatement.setInt(5, random.nextInt(100));           //price
            preparedStatement.setString(6, "UAH");                      //currency
            preparedStatement.setInt(7, userId);                        //userID
            preparedStatement.setString(8, "A");                        //status
            preparedStatement.setInt(9, i + 1);                        //userID
            preparedStatement.executeUpdate();
        }
    }

    private static void deleteUsers(String deleteUsers) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(deleteUsers);
        preparedStatement.executeUpdate();
    }

    private static void deleteCategory(String deleteCategory) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(deleteCategory);
        preparedStatement.executeUpdate();
    }

    private static void deleteAdvert(String deleteCategory) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(deleteCategory);
        preparedStatement.executeUpdate();
    }

    public static void updateUserId(String UPDATE_USER_ID) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(UPDATE_USER_ID);
        preparedStatement.executeUpdate();
    }

    public static void updateCategoryId(String UPDATE_CATEGORY_ID) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(UPDATE_CATEGORY_ID);
        preparedStatement.executeUpdate();
    }

    public static void updateAdvertisementId(String UPDATE_ADVERTISEMENT_ID) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(UPDATE_ADVERTISEMENT_ID);
        preparedStatement.executeUpdate();
    }

    public static void extractImages(String src) {
        try {
            FileInputStream fs = new FileInputStream(src);
            XWPFDocument docx = new XWPFDocument(fs);
            List<XWPFPictureData> allPictures = docx.getAllPictures();
            Iterator<XWPFPictureData> iterator = allPictures.iterator();

            File imagesFolder = new File("src/main/resources/images");
            imagesFolder.mkdir();

            int i = 0;
            while (iterator.hasNext()) {
                XWPFPictureData pic = iterator.next();
                byte[] bytepic = pic.getData();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytepic));
                ImageIO.write(image, "jpg", new File("src/main/resources/images/image_0" + i + ".jpg"));
                i++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}