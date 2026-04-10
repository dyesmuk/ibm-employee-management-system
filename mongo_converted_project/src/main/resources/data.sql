-- Seed data for EMS — loaded automatically on startup by Spring Boot

-- Departments
INSERT INTO departments (id, name, location) VALUES (1, 'Engineering',      'Bangalore');
INSERT INTO departments (id, name, location) VALUES (2, 'Human Resources',  'Mumbai');
INSERT INTO departments (id, name, location) VALUES (3, 'Finance',          'Delhi');
INSERT INTO departments (id, name, location) VALUES (4, 'Marketing',        'Hyderabad');
INSERT INTO departments (id, name, location) VALUES (5, 'Operations',       'Pune');

-- Employees
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (1, 'Alice',   'Smith',   'alice.smith@ibm.com',   85000.00, '2022-01-15', 'ACTIVE',    1);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (2, 'Bob',     'Jones',   'bob.jones@ibm.com',     72000.00, '2021-06-01', 'ACTIVE',    1);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (3, 'Carol',   'White',   'carol.white@ibm.com',   65000.00, '2023-03-10', 'ACTIVE',    2);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (4, 'David',   'Brown',   'david.brown@ibm.com',   90000.00, '2020-09-20', 'ACTIVE',    3);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (5, 'Eve',     'Davis',   'eve.davis@ibm.com',     78000.00, '2022-11-05', 'ACTIVE',    4);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (6, 'Frank',   'Miller',  'frank.miller@ibm.com',  55000.00, '2023-07-18', 'ON_LEAVE',  1);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (7, 'Grace',   'Wilson',  'grace.wilson@ibm.com',  95000.00, '2019-04-25', 'ACTIVE',    3);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (8, 'Henry',   'Moore',   'henry.moore@ibm.com',   62000.00, '2024-01-08', 'ACTIVE',    5);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (9, 'Iris',    'Taylor',  'iris.taylor@ibm.com',   71000.00, '2021-12-15', 'ACTIVE',    2);
INSERT INTO employees (id, first_name, last_name, email, salary, join_date, status, department_id)
VALUES (10,'James',   'Anderson','james.anderson@ibm.com',88000.00, '2020-03-30', 'ACTIVE',    1);
