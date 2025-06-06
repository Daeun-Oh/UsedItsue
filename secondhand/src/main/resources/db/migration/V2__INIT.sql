CREATE TABLE TREND (
    seq BIGINT AUTO_INCREMENT,
    category VARCHAR(40),
    wordCloud VARCHAR(60) NOT NULL,
    keywords TEXT NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    modifiedAt DATETIME,
    deletedAt DATETIME,
    PRIMARY KEY(seq)
);