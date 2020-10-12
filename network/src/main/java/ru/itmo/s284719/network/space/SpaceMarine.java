package ru.itmo.s284719.network.space;

import java.io.Serializable;
import java.time.ZonedDateTime;

/** Main Class for labs
 */
public class SpaceMarine implements Comparable<SpaceMarine>, Serializable {
    private int id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private ZonedDateTime creationDate = ZonedDateTime.now(); //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Long health; //Поле не может быть null, Значение поля должно быть больше 0
    private Integer height; //Поле может быть null
    private AstartesCategory category; //Поле не может быть null
    private MeleeWeapon meleeWeapon; //Поле не может быть null
    private Chapter chapter; //Поле не может быть null

    /** Constructor without parameters
     */
    public SpaceMarine() {
        this.setId(this.hashCode() > 0
                ? this.hashCode()
                : (this.hashCode()) == 0
                ? 1
                : (this.hashCode() == Integer.MIN_VALUE
                ? Integer.MAX_VALUE
                : -this.hashCode()));
        this.setCreationDate(ZonedDateTime.now());
    }
    /** Constructor with parameters
     *
     * @param name
     * @param coordinates
     * @param health
     * @param height
     * @param category
     * @param meleeWeapon
     * @param chapter
     */
    public SpaceMarine(String name, Coordinates coordinates, Long health, Integer height,
                AstartesCategory category, MeleeWeapon meleeWeapon, Chapter chapter) {
        this.setId(this.hashCode() > 0
                ? this.hashCode()
                : (this.hashCode()) == 0
                ? 1
                : (this.hashCode() == Integer.MIN_VALUE
                ? Integer.MAX_VALUE
                : -this.hashCode()));
        this.setName(name);
        this.setCoordinates(coordinates);
        this.setHealth(health);
        this.setHeight(height);
        this.setCategory(category);
        this.setMeleeWeapon(meleeWeapon);
        this.setChapter(chapter);
        this.setId(this.hashCode() > 0
                ? this.hashCode()
                : (this.hashCode()) == 0
                ? 1
                : (this.hashCode() == Integer.MIN_VALUE
                ? Integer.MAX_VALUE
                : -this.hashCode()));
        this.setCreationDate(ZonedDateTime.now());
    }

    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public Coordinates getCoordinates() {
        return this.coordinates;
    }
    public ZonedDateTime getCreationDate() {
        return this.creationDate;
    }
    public Long getHealth() {
        return this.health;
    }
    public Integer getHeight() {
        return this.height;
    }
    public AstartesCategory getCategory() {
        return this.category;
    }
    public MeleeWeapon getMeleeWeapon() {
        return this.meleeWeapon;
    }
    public Chapter getChapter() {
        return this.chapter;
    }

    public static void checkId(int id) throws IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("The value of the \"ID\" field must be greater than 0.");
        }
    }
    public static void checkName(String name) throws NullPointerException, IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException("The value of the \"Name\" field cannot be null.");
        } else if (name.equals("")) {
            throw new IllegalArgumentException("The value of the \"Name\" field cannot be empty.");
        }
    }
    public static void checkCoordinates(Coordinates coordinates) throws NullPointerException {
        if (coordinates == null) {
            throw new NullPointerException("The value of the \"Coordinates\" field cannot be null.");
        }
    }
    public static void checkCreationDate(ZonedDateTime creationDate) throws NullPointerException {
        if (creationDate == null) {
            throw new NullPointerException("The value of the \"Creation Date\" field cannot be null.");
        }
    }
    public static void checkHealth(Long health) {
        if (health == null) {
            throw new NullPointerException("The value of the \"Health\" field cannot be null.");
        } else if (health.longValue() <= 0L) {
            throw new IllegalArgumentException("The value of the \"Health\" field must be greater than 0.");
        }
    }
    public static void checkHeight(Integer height) {
        return;
    }
    public static void checkCategory(AstartesCategory category) throws NullPointerException {
        AstartesCategory.check(category);
    }
    public static void checkMeleeWeapon(MeleeWeapon meleeWeapon) throws NullPointerException {
        MeleeWeapon.check(meleeWeapon);
    }
    public static void checkChapter(Chapter chapter) {
        Chapter.check(chapter);
    }

    public void setId(int id) throws IllegalArgumentException {
        checkId(id);
        this.id = id;
    }
    public void setName(String name) throws NullPointerException, IllegalArgumentException {
        checkName(name);
        this.name = name;
    }
    public void setCoordinates(Coordinates coordinates) throws NullPointerException {
        checkCoordinates(coordinates);
        this.coordinates = coordinates;
    }
    public void setCreationDate(ZonedDateTime creationDate) throws NullPointerException {
        checkCreationDate(creationDate);
        this.creationDate = creationDate;
    }
    public void setHealth(Long health) {
        checkHealth(health);
        this.health = health;
    }
    public void setHeight(Integer height) {
        checkHeight(height);
        this.height = height;
    }
    public void setCategory(AstartesCategory category) throws NullPointerException {
        checkCategory(category);
        this.category = category;
    }
    public void setMeleeWeapon(MeleeWeapon meleeWeapon) throws NullPointerException {
        checkMeleeWeapon(meleeWeapon);
        this.meleeWeapon = meleeWeapon;
    }
    public void setChapter(Chapter chapter) {
        checkChapter(chapter);
        this.chapter = chapter;
    }

    @Override
    public String toString() {
        return "SpaceMarine: {id: " + getId() + ", name: " + getName() + ", " + getCoordinates().toString() +
                ", creationDate: " + getCreationDate() + ", health: " + getHealth() + ", height: " +
                getHeight() + ", category: " + getCategory() + ", meleeWeapon: " + getMeleeWeapon() +
                ", Chapter: " + getChapter() + "}";

    }

    @Override
    public int compareTo(SpaceMarine spaceMarine) {
        return this.getId() != spaceMarine.getId()
                ? getId() - spaceMarine.getId()
                : (!getName().equals(spaceMarine.getName())
                ? getName().compareTo(spaceMarine.getName())
                : (!getCoordinates().equals(spaceMarine.getCoordinates())
                ? getCoordinates().compareTo(spaceMarine.getCoordinates())
                : (!getCreationDate().equals(spaceMarine.getCreationDate())
                ? getCreationDate().compareTo(spaceMarine.getCreationDate())
                : (!getHealth().equals(spaceMarine.getHealth())
                ? getHealth().compareTo(spaceMarine.getHealth())
                : (!getHeight().equals(spaceMarine.getHeight())
                ? getHeight().compareTo(spaceMarine.getHeight())
                : (!getCategory().equals(spaceMarine.getCategory())
                ? getCategory().compareTo(spaceMarine.getCategory())
                : (!getMeleeWeapon().equals(spaceMarine.getMeleeWeapon())
                ? getMeleeWeapon().compareTo(spaceMarine.getMeleeWeapon())
                : getChapter().compareTo(spaceMarine.getChapter()))))))));
    }

}
