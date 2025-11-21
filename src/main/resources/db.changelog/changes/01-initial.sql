--liquibase formatted sql

--changeset ilp:001
create SCHEMA IF NOT EXISTS ilp;

CREATE TABLE IF NOT EXISTS ilp.drone_service_points (
    id int primary key,
    name varchar(100) not null,
    latitude numeric(18,14) not null,
    longitude numeric(18,14) not null,
    altitude integer not null
);

CREATE TABLE IF NOT EXISTS ilp.drones (
    id varchar(50) primary key,
    name varchar(100) not null,
    cooling boolean default false not null,
    heating boolean default false not null,
    capacity numeric(10,2) not null,
    max_moves integer not null,
    cost_per_move numeric(10,3) not null,
    cost_initial numeric(10,2) not null,
    cost_final numeric(10,2) not null
);

CREATE TABLE IF NOT EXISTS ilp.drone_service_point_availability (
    id uuid primary key,
    drone_id varchar(50) not null,
    service_point_id int not null,
    day_of_week integer not null,
    start_time time not null,
    end_time time not null,
    FOREIGN KEY (drone_id) REFERENCES ilp.drones(id),
    FOREIGN KEY (service_point_id) REFERENCES ilp.drone_service_points(id)
);