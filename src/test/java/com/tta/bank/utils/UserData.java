package com.tta.bank.utils;

import java.util.concurrent.ThreadLocalRandom;

/** A unique throw-away customer. Every test registers its own user, so tests never depend on each other. */
public class UserData {

    public final String name;
    public final String email;
    public final String phone;
    public final String password;

    public UserData(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public static UserData random() {
        long stamp = System.currentTimeMillis();
        int rnd = ThreadLocalRandom.current().nextInt(1000, 9999);
        String email = "auto." + stamp + "." + rnd + "@ttabank.in";
        String phone = "9" + String.format("%09d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
        return new UserData("Auto Tester", email, phone, ConfigReader.get("default.password", "Test@1234"));
    }

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
}
