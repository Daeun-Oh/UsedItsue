CREATE TABLE PRODUCT (
    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    gid VARCHAR(45) NOT NULL,       -- 그룹 id (상품 이미지 띄울 때 사용)
    name VARCHAR(100) NOT NULL,     -- 상품 이름
    category VARCHAR(40),           -- 상품 분류
    status ENUM('READY', 'SALE', "OUT_OF_STOCK", "STOP") DEFAULT 'READY', -- 판매 상태
    consumerPrice INT DEFAULT 0,    -- 소비자 가격
    salePrice INT DEFAULT 0,        -- 판매 가격
    description TEXT,               -- 상품 설명
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    modifiedAt DATETIME,
    deletedAt DATETIME
);