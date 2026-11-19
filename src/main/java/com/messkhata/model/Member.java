package com.messkhata.model;

import java.time.LocalDate;

/**
 * A person who lives in the mess and eats meals.
 */
public class Member extends Person {

    private String room;
    private final LocalDate joinDate;
    private boolean active;

    public Member(String id, String name, String phone, String room, LocalDate joinDate, boolean active) {
        super(id, name, phone);
        this.room = room;
        this.joinDate = joinDate;
        this.active = active;
    }

    @Override
    public String getRole() {
        return "Member";
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toLine() {
        return String.join(SEPARATOR, getId(), getName(), getPhone(), room,
                joinDate.toString(), String.valueOf(active));
    }

    public static Member fromParts(String[] p) {
        return new Member(p[0], p[1], p[2], p[3], LocalDate.parse(p[4]), Boolean.parseBoolean(p[5]));
    }
}
