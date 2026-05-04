package com.bookshop.shared;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8.0");

    static {
        MY_SQL_CONTAINER.start();
    }

}