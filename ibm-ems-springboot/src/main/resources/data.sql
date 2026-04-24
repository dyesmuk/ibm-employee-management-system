-- ═══════════════════════════════════════════════════════════════════════════
-- IBM EMS — Seed Data
-- Used by H2 in-memory DB for dev/test environments.
-- DevSecOps note: This data is for training only.
--                 Production data must NEVER appear in version control.
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. ROLES ────────────────────────────────────────────────────────────────
INSERT INTO roles (id, name, description, level) VALUES
(1,  'JUNIOR_ENGINEER',  'Entry-level software engineer',                1),
(2,  'ENGINEER',         'Mid-level software engineer',                  2),
(3,  'SENIOR_ENGINEER',  'Senior software engineer',                     3),
(4,  'TECH_LEAD',        'Technical lead for a team',                    4),
(5,  'PRINCIPAL_ENGINEER','Principal / Staff engineer',                  5),
(6,  'JUNIOR_ANALYST',   'Entry-level business analyst',                 1),
(7,  'ANALYST',          'Mid-level business analyst',                   2),
(8,  'MANAGER',          'People and delivery manager',                  5),
(9,  'HR_EXECUTIVE',     'HR executive — hiring and onboarding',         2),
(10, 'HR_MANAGER',       'HR manager — policy and compliance',           4);

-- ── 2. DEPARTMENTS ──────────────────────────────────────────────────────────
INSERT INTO departments (id, name, description) VALUES
(1,  'Engineering',      'Core software development'),
(2,  'HR',               'Human resources and talent acquisition'),
(3,  'Finance',          'Financial operations and treasury'),
(4,  'Marketing',        'Brand and digital marketing'),
(5,  'Sales',            'Retail and corporate banking sales'),
(6,  'Customer Support', 'Customer service and grievance resolution'),
(7,  'Operations',       'Branch and backend operations'),
(8,  'QA',               'Quality assurance and testing'),
(9,  'DevOps',           'Platform engineering and CI/CD'),
(10, 'Compliance',       'Regulatory compliance and audit');

-- ── 3. PROJECTS ─────────────────────────────────────────────────────────────
INSERT INTO projects (id, name, description, start_date, status, created_at, updated_at) VALUES
(1, 'YONO 2.0',            'Next-gen mobile banking platform',   '2024-01-15', 'ACTIVE',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Core Banking Upgrade','CBS migration to new platform',       '2024-02-01', 'ACTIVE',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Analytics Hub',       'Real-time transaction analytics',    '2024-03-10', 'PLANNED',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'HR Self-Service Portal','Employee self-service portal',     '2024-01-20', 'ACTIVE',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'UPI Integration',     'Enhanced UPI payment flows',         '2024-04-01', 'PLANNED',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── 4. EMPLOYEES (20 records — realistic IBM names and emails) ─────────────
INSERT INTO employees (id, first_name, last_name, email, phone, salary, hire_date, status, department_id, role_id, created_at, updated_at) VALUES
(1,  'Arjun',     'Sharma',    'arjun.sharma@ibm.co.in',    '+919811001001', 55000.00, '2021-06-01', 'ACTIVE',     1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2,  'Priya',     'Nair',      'priya.nair@ibm.co.in',      '+919811001002', 72000.00, '2020-03-15', 'ACTIVE',     1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3,  'Rajesh',    'Kumar',     'rajesh.kumar@ibm.co.in',    '+919811001003', 45000.00, '2022-08-01', 'ACTIVE',     8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4,  'Sunita',    'Patel',     'sunita.patel@ibm.co.in',    '+919811001004', 48000.00, '2022-01-10', 'ACTIVE',     2, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5,  'Vikram',    'Singh',     'vikram.singh@ibm.co.in',    '+919811001005', 90000.00, '2019-07-01', 'ACTIVE',     9, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6,  'Deepa',     'Menon',     'deepa.menon@ibm.co.in',     '+919811001006', 62000.00, '2021-02-01', 'ACTIVE',     3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7,  'Kiran',     'Reddy',     'kiran.reddy@ibm.co.in',     '+919811001007', 38000.00, '2023-03-01', 'ACTIVE',     5, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8,  'Amit',      'Verma',     'amit.verma@ibm.co.in',      '+919811001008', 41000.00, '2023-01-15', 'ACTIVE',     1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9,  'Meera',     'Joshi',     'meera.joshi@ibm.co.in',     '+919811001009', 53000.00, '2021-11-01', 'ACTIVE',     7, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Ravi',      'Pillai',    'ravi.pillai@ibm.co.in',     '+919811001010', 67000.00, '2020-09-01', 'ACTIVE',     1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Ananya',    'Gupta',     'ananya.gupta@ibm.co.in',    '+919811001011', 44000.00, '2022-05-01', 'ACTIVE',     4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Sanjay',    'Desai',     'sanjay.desai@ibm.co.in',    '+919811001012', 58000.00, '2021-04-01', 'ACTIVE',     6, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Kavitha',   'Rao',       'kavitha.rao@ibm.co.in',     '+919811001013', 47000.00, '2022-07-01', 'ACTIVE',     2, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Nikhil',    'Bose',      'nikhil.bose@ibm.co.in',     '+919811001014', 51000.00, '2021-09-01', 'ACTIVE',     9, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Pooja',     'Iyer',      'pooja.iyer@ibm.co.in',      '+919811001015', 39000.00, '2023-06-01', 'ACTIVE',     8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Suresh',    'Chandra',   'suresh.chandra@ibm.co.in',  '+919811001016', 82000.00, '2019-01-01', 'ACTIVE',     1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Lakshmi',   'Krishnan',  'lakshmi.krishnan@ibm.co.in','+919811001017', 46000.00, '2022-11-01', 'ON_LEAVE',   3, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Pranav',    'Shah',      'pranav.shah@ibm.co.in',     '+919811001018', 54000.00, '2021-03-01', 'ACTIVE',     10, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Divya',     'Mathur',    'divya.mathur@ibm.co.in',    '+919811001019', 36000.00, '2023-08-01', 'ACTIVE',     5, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Harish',    'Naidu',     'harish.naidu@ibm.co.in',    '+919811001020', 75000.00, '2020-06-01', 'ACTIVE',     9, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── 5. EMPLOYEE_PROJECT ──────────────────────────────────────────────────────
INSERT INTO employee_project (employee_id, project_id, assigned_date, project_role) VALUES
(1,  1, CURRENT_DATE, 'Backend Developer'),
(2,  1, CURRENT_DATE, 'Tech Lead'),
(3,  1, CURRENT_DATE, 'QA Engineer'),
(5,  1, CURRENT_DATE, 'DevOps Engineer'),
(8,  1, CURRENT_DATE, 'Frontend Developer'),
(10, 2, CURRENT_DATE, 'Senior Developer'),
(14, 2, CURRENT_DATE, 'DevOps Engineer'),
(16, 2, CURRENT_DATE, 'Tech Lead'),
(4,  4, CURRENT_DATE, 'Business Analyst'),
(13, 4, CURRENT_DATE, 'HR Liaison'),
(9,  4, CURRENT_DATE, 'Project Coordinator'),
(6,  3, CURRENT_DATE, 'Finance Analyst'),
(11, 3, CURRENT_DATE, 'Marketing Analyst'),
(18, 3, CURRENT_DATE, 'Compliance Analyst');
