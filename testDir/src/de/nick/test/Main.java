package de.nick.test;
import de.nick.test.constants.Greetings;

import org.apache.maven.model.Profile;

public class Main {
    public static void main(String[] args) {
        System.out.println(Greetings.HELLO_WORLD);

        Profile profile = new Profile();
        profile.setId("Profile Id works!");
        System.out.println(profile.getId());
    }
}