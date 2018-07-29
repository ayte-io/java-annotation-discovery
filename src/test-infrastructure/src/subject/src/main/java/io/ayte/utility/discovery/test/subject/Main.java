package io.ayte.utility.discovery.test.subject;

import io.ayte.utility.discovery.api.v1.Repository;

public class Main {
    @SuppressWarnings("squid:S106")
    public static void main(String[] args) {
        System.out.println(Repository.getInstance());
    }
}
