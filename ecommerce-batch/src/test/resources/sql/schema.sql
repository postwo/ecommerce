
CREATE TABLE products
(
    product_id       varchar(255) PRIMARY KEY,
    seller_id        BIGINT       NOT NULL,
    category         VARCHAR(255) NOT NULL,
    product_name     VARCHAR(255) NOT NULL,
    sales_start_date DATE,
    sales_end_date   DATE,
    product_status   VARCHAR(50),
    brand            VARCHAR(255),
    manufacturer     VARCHAR(255),
    sales_price      INTEGER      NOT NULL,
    stock_quantity   INTEGER   DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_status ON products (product_status);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_brand ON products (brand);
CREATE INDEX idx_products_manufacturer ON products (manufacturer);
CREATE INDEX idx_products_seller_id ON products (seller_id);

UPDATE batch_job_execution
SET
    status = 'FAILED',
    end_time = NOW(),
    exit_code = 'FAILED',
    exit_message = 'Manually marked as FAILED'
WHERE job_execution_id = 40;


select * from batch_job_execution;


DELETE FROM batch_step_execution_context;
DELETE FROM batch_step_execution;
DELETE FROM batch_job_execution_context;
DELETE FROM batch_job_execution_params;
DELETE FROM batch_job_execution;
DELETE FROM batch_job_instance;

