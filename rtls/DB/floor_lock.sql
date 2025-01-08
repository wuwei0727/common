CREATE TABLE time_period_admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mapId INT NOT NULL,
    map_name VARCHAR(255) NOT NULL,
    company_id INT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    day_of_week tinyint(1) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    period_type tinyint(1),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE place_unlock_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    mapId INT NOT NULL,
    place_id INT,
    license_plate VARCHAR(20) NOT NULL,
    phone VARCHAR(11) NOT NULL,
    unlock_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    parking_status tinyint(1) NOT NULL DEFAULT '1'
);

CREATE TABLE car_plate (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       plate_number VARCHAR(50) NOT NULL,
       company_id BIGINT NOT NULL,
       company_name VARCHAR(100) NOT NULL,
       map_id BIGINT NOT NULL,
       map_name VARCHAR(255) NOT NULL,
       phone_number VARCHAR(20)
);

CREATE TABLE user_company_map (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      user_id BIGINT NOT NULL,
      map_id BIGINT NOT NULL,
      map_name VARCHAR(255),
      company_id BIGINT NOT NULL,
      company_name VARCHAR(100) NOT NULL
);


Temporary failure in name resolution