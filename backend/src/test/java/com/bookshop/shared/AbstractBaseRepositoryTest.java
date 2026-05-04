package com.bookshop.shared;


import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractBaseRepositoryTest {
    @ServiceConnection
    static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8.0");

    // The JVM runs this block exactly once when the class is loaded into memory so our container is started once only.
    static {
        MY_SQL_CONTAINER.start();
    }
}