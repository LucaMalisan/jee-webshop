CREATE TABLE article
(
    uuid            VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255),
    description     TEXT,
    priceCHF        FLOAT,
    discountPercent INT,
    stock           INT
);

CREATE TABLE article_image
(
    uuid        VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
    articleUuid VARCHAR(36) REFERENCES article (uuid),
    imageURL    TEXT,
    "order"       INT
)