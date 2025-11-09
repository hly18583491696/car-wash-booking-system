-- Payment system database tables

-- Payment records table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL,
    payment_no VARCHAR(64) NOT NULL UNIQUE,
    transaction_id VARCHAR(64) NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    paid_at TIMESTAMP NULL,
    expire_at TIMESTAMP NOT NULL,
    notify_count INT NOT NULL DEFAULT 0,
    description VARCHAR(200) NULL,
    raw_data TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    
    INDEX idx_order_no (order_no),
    INDEX idx_payment_no (payment_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Refund records table
CREATE TABLE IF NOT EXISTS refunds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    refund_no VARCHAR(64) NOT NULL UNIQUE,
    third_party_refund_id VARCHAR(64) NULL,
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(200) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    refunded_at TIMESTAMP NULL,
    operator_id BIGINT NULL,
    raw_data TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    
    INDEX idx_payment_id (payment_id),
    INDEX idx_refund_no (refund_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Add payment related columns to bookings table (ignore errors if columns already exist)
ALTER TABLE bookings ADD COLUMN payment_status VARCHAR(20) DEFAULT 'unpaid';
ALTER TABLE bookings ADD COLUMN payment_method VARCHAR(20) NULL;
ALTER TABLE bookings ADD COLUMN paid_at TIMESTAMP NULL;

-- Add indexes for bookings payment columns
CREATE INDEX idx_bookings_payment_status ON bookings(payment_status);
CREATE INDEX idx_bookings_payment_method ON bookings(payment_method);
CREATE INDEX idx_bookings_paid_at ON bookings(paid_at);