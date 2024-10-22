CREATE TABLE `card`
(
    `id`   BIGINT AUTO_INCREMENT NOT NULL,
    `deck` VARCHAR(255)          NOT NULL,
    `type` VARCHAR(255)          NOT NULL,
    `text` LONGTEXT              NOT NULL,
    CONSTRAINT `pk_card` PRIMARY KEY (`id`)
);
