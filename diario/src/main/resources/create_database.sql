DROP DATABASE IF EXISTS diario;
-------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE SCHEMA IF NOT EXISTS `diario` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `diario` ;
-- -----------------------------------------------------
-- Table `diario`.`usuarios`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `diario`.`usuarios` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(255) NULL DEFAULT NULL,
    `senha` VARCHAR(255) NULL DEFAULT NULL,
    `username` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    AUTO_INCREMENT = 3
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `diario`.`entrada_diario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `diario`.`entrada_diario` (
     `id` INT NOT NULL AUTO_INCREMENT,
     `id_usuario` BIGINT NULL DEFAULT NULL,
     `conteudo` TEXT NOT NULL,
     `data` DATE NOT NULL,
     `foto` VARCHAR(255) NULL DEFAULT NULL,
     `caminho_imagem` VARCHAR(255) NULL DEFAULT NULL,
     `usuario_id` BIGINT NOT NULL,
     PRIMARY KEY (`id`),
     INDEX `FK7khklp8d9bhrwot3std6walml` (`usuario_id` ASC) VISIBLE,
     INDEX `fk_usuario` (`id_usuario` ASC) VISIBLE,
     CONSTRAINT `FK7khklp8d9bhrwot3std6walml`
         FOREIGN KEY (`usuario_id`)
             REFERENCES `diario`.`usuarios` (`id`),
     CONSTRAINT `fk_usuario`
         FOREIGN KEY (`id_usuario`)
             REFERENCES `diario`.`usuarios` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

----------------------------------------------------
-- Adicionando mail e fk:
----------------------------------------------------
ALTER TABLE usuarios add column `email` VARCHAR(255) NULL DEFAULT NULL; commit;
ALTER TABLE usuarios ADD CONSTRAINT uk_usuario_email UNIQUE (email); commit;

-----------------------------------------------------------------------------
--    CRIANDO ABORDAGEM MAIS PROFUNDA pra receber outras medias
-----------------------------------------------------------------------------

-----------------------------------------------------
-- Dropo a coluna que eu não vou precisar mais:
-----------------------------------------------------
ALTER TABLE `diario`.`entrada_diario` DROP COLUMN `foto`, DROP COLUMN `caminho_imagem`;

-----------------------------------------------------
-- Crio table só pras medias:
-----------------------------------------------------

CREATE TABLE IF NOT EXISTS `diario`.`midia_entrada` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `id_entrada_diario` INT NOT NULL,
    `tipo_arquivo` VARCHAR(50) NOT NULL, -- Ex: 'video', 'audio', 'documento_word', 'documento_pdf', 'imagem'
    `caminho_arquivo` VARCHAR(255) NOT NULL, -- Caminho completo no servidor/nuvem
    `nome_original_arquivo` VARCHAR(255) NULL DEFAULT NULL,
    `data_upload` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Quando o arquivo foi enviado
    PRIMARY KEY (`id`),
    INDEX `fk_midia_entrada_diario` (`id_entrada_diario` ASC) VISIBLE,
    CONSTRAINT `fk_midia_entrada_diario`
        FOREIGN KEY (`id_entrada_diario`)
            REFERENCES `diario`.`entrada_diario` (`id`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-----------------------------------------------------
-- Implementando título nas entradas:
-----------------------------------------------------
ALTER TABLE entrada_diario add column `titulo` VARCHAR(255) NULL DEFAULT NULL; commit;