package ru.itmo.s284719.network.space;

import java.io.Serializable;

public class Chapter implements Comparable<Chapter>, Serializable {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private String parentLegion; //
    private int marinesCount; //Значение поля должно быть больше 0, Максимальное значение поля: 1000
    private String world; //

    //mine code here
    public Chapter() {}

    public Chapter(String name, String parentLegion, int marinesCount, String world)
            throws NullPointerException, IllegalArgumentException{
        this.setName(name);
        this.setParentLegion(parentLegion);
        this.setMarinesCount(marinesCount);
        this.setWorld(world);
    }

    public static void checkName(String name) throws NullPointerException, IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException("Поле не может быть null");
        }
        if (name.equals("")) {
            throw new IllegalArgumentException("Строка не может быть пустой");
        }
    }
    public static void checkParentLegion(String parentLegion) {
        return;
    }
    public static void checkMarinesCount(int marinesCount) throws IllegalArgumentException {
        if (marinesCount < 0) {
            throw new IllegalArgumentException("Значение поля должно быть больше 0");
        }
        if (marinesCount > 1000) {
            throw new IllegalArgumentException(("Максимальное значение поля: 1000"));
        }
    }
    public static void checkWorld(String world) {
        return;
    }
    public static void check(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException("Значение поля \"Chapter\" не может быть null");
        }
    }


    public String getName() {
        return name;
    }
    public String getParentLegion() {
        return parentLegion;
    }
    public int getMarinesCount() {
        return marinesCount;
    }
    public String getWorld() {
        return world;
    }

    public void setName(String name) throws NullPointerException, IllegalArgumentException {
        checkName(name);
        this.name = name;
    }
    public void setParentLegion(String parentLegion) {
        checkParentLegion(parentLegion);
        this.parentLegion = parentLegion;
    }
    public void setMarinesCount(int marinesCount) throws IllegalArgumentException {
        checkMarinesCount(marinesCount);
        this.marinesCount = marinesCount;
    }
    public void setWorld(String world) {
        checkWorld(world);
        this.world = world;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == null || !getClass().equals(ob.getClass()) || hashCode() != ob.hashCode()) {
            return false;
        }
        Chapter chapter = (Chapter) ob;
        if (!getName().equals(chapter.getName()) || getMarinesCount() != chapter.getMarinesCount()) {
            return false;
        } else if ((getParentLegion() == null) != (chapter.getParentLegion() == null)) {
            return false;
        } else if ((getWorld() == null) != (chapter.getWorld() == null)) {
            return false;
        }
        return getParentLegion().equals(chapter.getParentLegion()) && getWorld().equals(chapter.getWorld());
    }

    @Override
    public String toString() {
        return "Chapter: {name: " + getName() +
                ", parentLegion: " + getParentLegion() +
                ", marinesCount: " + getMarinesCount() +
                ", world: " + getWorld() + "}";
    }

    @Override
    public int compareTo(Chapter chapter) {
        if (!getName().equals(chapter.getName())) {
            return getName().compareTo(chapter.getName());
        } else if (getParentLegion() == null && chapter.getParentLegion() != null) {
            return -1;
        } else if (!getParentLegion().equals(chapter.getParentLegion())) {
            return getParentLegion().compareTo(chapter.getParentLegion());
        } else if (getMarinesCount() != chapter.getMarinesCount()) {
            return getMarinesCount() - chapter.getMarinesCount();
        } else if (getWorld() == null && chapter.getWorld() != null) {
            return -2;
        } else if (getWorld() != null && chapter.getWorld() == null) {
            return 2;
        }
        return getWorld().compareTo(chapter.getWorld());
    }
}
