CREATE TABLE if not exists clients
(
    id           BIGINT PRIMARY KEY,
    name         VARCHAR(255),
    phone_number VARCHAR(255) UNIQUE,
    action       VARCHAR(255),
    role         VARCHAR(255)
);

CREATE TABLE if not exists orders
(
    id             BIGINT PRIMARY KEY,
    time_of_record VARCHAR(255),
    week_day       VARCHAR(255),
    is_verified    BOOLEAN,
    is_completed   BOOLEAN,
    client_id      BIGINT,
    FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE TABLE if not exists services
(
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) UNIQUE,
    description VARCHAR(255),
    price       INTEGER
);

CREATE TABLE if not exists order_services
(
    order_id   BIGINT,
    service_id UUID,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (service_id) REFERENCES services (id)
);