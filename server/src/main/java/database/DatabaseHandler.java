package database;

import network.parser.Pair;
import network.space.SpaceMarine;
import network.parser.Pair;
import network.space.*;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseHandler extends Configs {
    private Lock lock = new ReentrantLock();
    public Connection dbConnection = null;
    private String adminLogin, adminPassword;

    public DatabaseHandler(String adminLogin, String adminPassword) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException {
        // save the admin's data.
        this.adminLogin = adminLogin;
        this.adminPassword = adminPassword;
        User admin = new User(adminLogin, adminPassword);
        // try to connection to the database.
        lock.lock();
        Class.forName("org.postgresql.Driver");
        dbConnection = DriverManager.getConnection(connectionString, adminLogin, adminPassword);
        // is the login free?
        String selectLogin = "SELECT login FROM users WHERE login = ?";
        PreparedStatement prSt = dbConnection.prepareStatement(selectLogin);
        prSt.setString(1, adminLogin);
        ResultSet resultSet = prSt.executeQuery();
        // if it's true, than: register the admin.
        if (!resultSet.next()) {
            String insertUser = "INSERT INTO users(login, hash_password) VALUES(?,?)";
            prSt = dbConnection.prepareStatement(insertUser);
            prSt.setString(1, adminLogin);
            prSt.setBytes(2, admin.getHashPassword());
            prSt.executeUpdate();
            return;
        }
        // else: is the login corrected?
        String selectUserLogin = "SELECT login FROM users " +
                "WHERE login = ? AND hash_password = ?";
        prSt = dbConnection.prepareStatement(selectUserLogin);
        prSt.setString(1, adminLogin);
        prSt.setBytes(2, admin.getHashPassword());
        resultSet = prSt.executeQuery();
        // if it's false: throw PasswordIncorrectException;
        if (!resultSet.next())
            throw new SQLDataException();
        lock.unlock();
    }

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        lock.lock();
        if (dbConnection == null || !dbConnection.isValid(0)) {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(connectionString, adminLogin, adminPassword);
        }
        lock.unlock();
        return dbConnection;
    }

    public boolean isLoginFree(String login) throws ClassNotFoundException, SQLException {
        lock.lock();
        String selectLogin = "SELECT login FROM users WHERE login = ?";
        PreparedStatement prSt = getDbConnection().prepareStatement(selectLogin);
        prSt.setString(1, login);
        ResultSet resultSet = prSt.executeQuery();
        lock.unlock();
        return !resultSet.next();
    }

    public void registerUser(User user) throws ClassNotFoundException, SQLException {
        lock.lock();
        String insertUser = "INSERT INTO users(login, hash_password) VALUES(?,?)";
        PreparedStatement prSt = getDbConnection().prepareStatement(insertUser);
        prSt.setString(1, user.getLogin());
        prSt.setBytes(2, user.getHashPassword());
        prSt.executeUpdate();
        lock.unlock();
    }

    public void deleteUser(String login) throws ClassNotFoundException, SQLException {
        lock.lock();
        String deleteUser = "DELETE FROM users WHERE login = ?";
        PreparedStatement prSt = getDbConnection().prepareStatement(deleteUser);
        prSt.setString(1, login);
        prSt.executeQuery();
        lock.unlock();
    }

    public boolean isRegisteredUser(User user) throws ClassNotFoundException, SQLException {
        lock.lock();
        String selectUserLogin = "SELECT login FROM users " +
                "WHERE login = ? AND hash_password = ?";
        PreparedStatement prSt = getDbConnection().prepareStatement(selectUserLogin);
        prSt.setString(1, user.getLogin());
        prSt.setBytes(2, user.getHashPassword());
        ResultSet resultSet = prSt.executeQuery();
        lock.unlock();
        return resultSet.next();
    }

    public int addSpaceMarineWithCreator(SpaceMarine spaceMarine, User user)
            throws SQLException, ClassNotFoundException {
        lock.lock();
        String selectIdInsertedSpaceMarine = "INSERT INTO space_marines" +
            "(" +
                getFieldSpaceMarineWithoutIdWithCreatorLogin() +
            ")" +
            "VALUES(?, ?,?, ?,?,?,?,?,?,?,?, ?,?,?,?, ?,?,?,?, ?) RETURNING id";
        PreparedStatement prSt = getDbConnection().prepareStatement(selectIdInsertedSpaceMarine);
        prSt.setString(1, spaceMarine.getName());

        Coordinates coordinates = spaceMarine.getCoordinates();
        prSt.setLong(2, coordinates.getX());
        if (coordinates.getY() == null)
            prSt.setNull(3, Types.BIGINT);
        else
            prSt.setLong(3, coordinates.getY());

        ZonedDateTime creationDate = spaceMarine.getCreationDate();
        prSt.setInt(4, creationDate.getYear());
        prSt.setInt(5, creationDate.getMonthValue());
        prSt.setInt(6, creationDate.getDayOfMonth());
        prSt.setInt(7, creationDate.getHour());
        prSt.setInt(8, creationDate.getMinute());
        prSt.setInt(9, creationDate.getSecond());
        prSt.setInt(10, creationDate.getNano());
        prSt.setString(11, creationDate.getZone().toString());

        prSt.setLong(12, spaceMarine.getHealth());
        if (spaceMarine.getHeight() == null)
            prSt.setNull(13, Types.INTEGER);
        else
            prSt.setInt(13, spaceMarine.getHeight());
        prSt.setString(14, spaceMarine.getCategory().toString());
        prSt.setString(15, spaceMarine.getMeleeWeapon().toString());

        Chapter chapter = spaceMarine.getChapter();
        prSt.setString(16, chapter.getName());
        prSt.setString(17, chapter.getParentLegion());
        prSt.setInt(18, chapter.getMarinesCount());
        prSt.setString(19, chapter.getWorld());

        prSt.setString(20, user.getLogin());

        ResultSet resultSet = prSt.executeQuery();
        resultSet.next();
        lock.unlock();
        return resultSet.getInt("id");
    }

    public void deleteSpaceMarineWithCreator(int id)
            throws SQLException, ClassNotFoundException {
        lock.lock();
        String deleteSpaceMarine = "DELETE FROM space_marines WHERE id = ?";
        PreparedStatement prSt = getDbConnection().prepareStatement(deleteSpaceMarine);
        prSt.setInt(1, id);
        prSt.executeUpdate();
        lock.unlock();
    }

    public PriorityBlockingQueue<Pair<SpaceMarine, String>> getPriorityBlockingQueuePair()
            throws SQLException, ClassNotFoundException {
        lock.lock();
        PriorityBlockingQueue<Pair<SpaceMarine, String>> queue = new PriorityBlockingQueue<>();
        ResultSet resultSet = getDbConnection().prepareStatement("SELECT * FROM space_marines").executeQuery();
        while (resultSet.next()) {
            SpaceMarine spaceMarine = new SpaceMarine();

            int id = resultSet.getInt("id");
            spaceMarine.setId(id);

            String name = resultSet.getString("name");
            spaceMarine.setName(name);

            long coordinateX = resultSet.getLong("coordinate_x");
            Long coordinateY = resultSet.getObject("coordinate_y", Long.class);
            Coordinates coordinates = new Coordinates(coordinateX, coordinateY);
            spaceMarine.setCoordinates(coordinates);

            int creationDateYear = resultSet.getInt("creation_date_year");
            int creationDateMonth = resultSet.getInt("creation_date_month");
            int creationDateDay = resultSet.getInt("creation_date_day");
            int creationTimeHour = resultSet.getInt("creation_time_hour");
            int creationTimeMinute = resultSet.getInt("creation_time_minute");
            int creationTimeSecond = resultSet.getInt("creation_time_second");
            int creationTimeNano = resultSet.getInt("creation_time_nano");
            ZoneId creationZoneId = ZoneId.of(resultSet.getString("creation_zone_id"));
            ZonedDateTime creationDate = ZonedDateTime.of(
                    creationDateYear, creationDateMonth, creationDateDay,
                    creationTimeHour, creationTimeMinute, creationTimeSecond,
                    creationTimeNano, creationZoneId
            );
            spaceMarine.setCreationDate(creationDate);

            long health = resultSet.getLong("health");
            Integer height = resultSet.getObject("height", Integer.class);
            AstartesCategory category = AstartesCategory.valueOf(resultSet.getString("category"));
            MeleeWeapon meleeWeapon = MeleeWeapon.valueOf(resultSet.getString("melee_weapon"));
            spaceMarine.setHealth(health);
            spaceMarine.setHeight(height);
            spaceMarine.setCategory(category);
            spaceMarine.setMeleeWeapon(meleeWeapon);

            String chapterName = resultSet.getString("chapter_name");
            String chapter_parent_legion = resultSet.getString("chapter_parent_legion");
            int chapterMarinesCount = resultSet.getInt("chapter_marines_count");
            String chapterWorld = resultSet.getString("chapter_world");
            Chapter chapter = new Chapter(chapterName, chapter_parent_legion, chapterMarinesCount, chapterWorld);
            spaceMarine.setChapter(chapter);

            String creatorUserLogin = resultSet.getString("creator_user_login");

            queue.add(new Pair<>(spaceMarine, creatorUserLogin));
        }
        lock.unlock();
        return queue;
    }

    public String getFieldSpaceMarineWithoutIdWithCreatorLogin() {
        return getFieldSpaceMarineWithoutId() + ", creator_user_login";
    }

    public String getFieldSpaceMarine() {
        return "id, " + getFieldSpaceMarineWithoutId();
    }

    public String getFieldSpaceMarineWithoutId() {
        return
            "name, " +

            "coordinate_x, " +
            "coordinate_y, " +

            "creation_date_year, " +
            "creation_date_month, " +
            "creation_date_day, " +
            "creation_time_hour, " +
            "creation_time_minute, " +
            "creation_time_second, " +
            "creation_time_nano, " +
            "creation_zone_id, " +

            "health, " +
            "height, " +
            "category, " +
            "melee_weapon, " +

            "chapter_name, " +
            "chapter_parent_legion, " +
            "chapter_marines_count, " +
            "chapter_world";
    }
}
