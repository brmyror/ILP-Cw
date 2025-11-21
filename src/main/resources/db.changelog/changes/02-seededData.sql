--liquibase formatted sql

--changeset ilp:002
-- Seed data for ilp.drones
INSERT INTO ilp.drones (id, name, cooling, heating, capacity, max_moves, cost_per_move, cost_initial, cost_final) VALUES
('1',  'Drone 1',  true,  true,  4,  2000, 0.01, 4.30, 6.50),
('2',  'Drone 2',  false, true,  8,  1000, 0.03, 2.60, 5.40),
('3',  'Drone 3',  false, false, 20, 4000, 0.05, 9.50, 11.50),
('4',  'Drone 4',  false, true,  8,  1000, 0.02, 1.40, 2.50),
('5',  'Drone 5',  true,  true,  12, 1500, 0.04, 1.80, 3.50),
('6',  'Drone 6',  false, true,  4,  2000, 0.03, 3.00, 4.00),
('7',  'Drone 7',  false, true,  8,  1000, 0.015, 1.40, 2.20),
('8',  'Drone 8',  true,  false, 20, 4000, 0.04, 5.40, 12.50),
('9',  'Drone 9',  true,  true,  8,  1000, 0.06, 2.40, 1.50),
('10', 'Drone 10', false, false, 12, 1500, 0.07, 1.40, 3.50)
ON CONFLICT (id) DO NOTHING;

--rollback DELETE FROM ilp.drones WHERE id IN ('1','2','3','4','5','6','7','8','9','10');

--changeset ilp:003
-- Seed data for ilp.drone_service_points
INSERT INTO ilp.drone_service_points (id, name, latitude, longitude, altitude) VALUES
(1, 'Appleton Tower', 55.9446806670849, -3.18635807889864, 50),
(2, 'Ocean Terminal', 55.9811862793337, -3.17732611501824, 50)
ON CONFLICT (id) DO NOTHING;

--rollback DELETE FROM ilp.drone_service_points WHERE id IN (1,2);