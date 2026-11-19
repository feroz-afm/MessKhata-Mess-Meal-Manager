package com.messkhata.model;

/**
 * Common data for every person in the mess.
 * Abstract: we never create a plain Person, only a Member or a Manager.
 */
public abstract class Person implements Storable {

    private final String id;
    private String name;
    private String phone;

    protected Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public abstract String getRole();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return name;
    }
}
