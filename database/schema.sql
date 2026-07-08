-- ============================================================
-- Insurance Management System - MySQL 8 Database Schema
-- Phase 1 deliverable: normalized (3NF) schema with FKs
-- Note: In Phase 2, Hibernate (spring.jpa.hibernate.ddl-auto)
-- can generate this schema automatically; this script is the
-- reference design and can also be run manually.
-- ============================================================

CREATE DATABASE IF NOT EXISTS insurance_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE insurance_db;

-- ------------------------------------------------------------
-- 1. users: all actors of the system (ADMIN / AGENT / CUSTOMER)
-- ------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','AGENT','CUSTOMER') NOT NULL,
    phone         VARCHAR(15),
    address       VARCHAR(255),
    dob           DATE,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 2. agent_assignments: one agent serves many customers,
--    a customer has at most one agent (unique customer_id)
-- ------------------------------------------------------------
CREATE TABLE agent_assignments (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    agent_id    BIGINT   NOT NULL,
    customer_id BIGINT   NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignment_customer (customer_id),
    CONSTRAINT fk_assignment_agent
        FOREIGN KEY (agent_id) REFERENCES users (id),
    CONSTRAINT fk_assignment_customer
        FOREIGN KEY (customer_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 3. plans: insurance product catalogue (all 4 types)
-- ------------------------------------------------------------
CREATE TABLE plans (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    name              VARCHAR(100)  NOT NULL,
    insurance_type    ENUM('LIFE','HEALTH','MOTOR','HOME') NOT NULL,
    description       TEXT,
    coverage_amount   DECIMAL(15,2) NOT NULL,
    premium_amount    DECIMAL(10,2) NOT NULL,
    premium_frequency ENUM('MONTHLY','QUARTERLY','YEARLY') NOT NULL,
    duration_years    INT           NOT NULL,
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_plans_type (insurance_type)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 4. policies: a plan purchased by a customer (life cycle)
-- ------------------------------------------------------------
CREATE TABLE policies (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    policy_number VARCHAR(30) NOT NULL,
    customer_id   BIGINT      NOT NULL,
    plan_id       BIGINT      NOT NULL,
    status        ENUM('PENDING','ACTIVE','REJECTED','EXPIRED','CANCELLED')
                              NOT NULL DEFAULT 'PENDING',
    start_date    DATE,
    end_date      DATE,
    details_json  JSON,
    remarks       VARCHAR(255),
    applied_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_policies_number (policy_number),
    KEY idx_policies_status (status),
    CONSTRAINT fk_policies_customer
        FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_policies_plan
        FOREIGN KEY (plan_id) REFERENCES plans (id)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 5. claims: raised by a customer against an ACTIVE policy
-- ------------------------------------------------------------
CREATE TABLE claims (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    claim_number     VARCHAR(30)   NOT NULL,
    policy_id        BIGINT        NOT NULL,
    claim_amount     DECIMAL(15,2) NOT NULL,
    incident_date    DATE          NOT NULL,
    description      TEXT          NOT NULL,
    status           ENUM('FILED','UNDER_REVIEW','APPROVED','REJECTED','SETTLED')
                                   NOT NULL DEFAULT 'FILED',
    reviewer_remarks VARCHAR(255),
    settled_amount   DECIMAL(15,2),
    settled_date     DATE,
    filed_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_claims_number (claim_number),
    KEY idx_claims_status (status),
    CONSTRAINT fk_claims_policy
        FOREIGN KEY (policy_id) REFERENCES policies (id)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 6. payments: premium dues and simulated payments per policy
-- ------------------------------------------------------------
CREATE TABLE payments (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    policy_id       BIGINT        NOT NULL,
    due_date        DATE          NOT NULL,
    paid_date       DATE,
    amount          DECIMAL(10,2) NOT NULL,
    transaction_ref VARCHAR(40),
    status          ENUM('DUE','PAID','OVERDUE') NOT NULL DEFAULT 'DUE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_txn (transaction_ref),
    KEY idx_payments_status (status),
    CONSTRAINT fk_payments_policy
        FOREIGN KEY (policy_id) REFERENCES policies (id)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 7. notifications: in-app notifications per user
-- ------------------------------------------------------------
CREATE TABLE notifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(100) NOT NULL,
    message    VARCHAR(500) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notifications_user (user_id, is_read),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Seed data: default admin account
-- Password below is the BCrypt hash of 'Admin@123'
-- (the Phase 2 application will also seed this on first run)
-- ------------------------------------------------------------
INSERT INTO users (name, email, password_hash, role)
VALUES ('System Administrator', 'admin@ims.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN');
