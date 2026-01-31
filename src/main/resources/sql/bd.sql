CREATE TABLE `productos`
(
    `codigo` INT            NOT NULL,
    `nombre` VARCHAR(255)   NOT NULL,
    `precio` DECIMAL(10, 2) NOT NULL,
    `stock`  INT            NOT NULL,
    `estado` VARCHAR(255)   NOT NULL,
    PRIMARY KEY (`codigo`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;



INSERT INTO productos (codigo, nombre, precio, stock, estado)
VALUES (2001, 'Laptop Lenovo IdeaPad 3', 2450000.00, 5, 'DISPONIBLE'),
       (2002, 'Mouse Logitech Inalambrico', 85000.00, 30, 'DISPONIBLE'),
       (2003, 'Teclado Mecanico Redragon', 195000.00, 12, 'DISPONIBLE'),
       (2004, 'Monitor Samsung 24 Pulgadas', 920000.00, 7, 'DISPONIBLE'),
       (2005, 'Disco Solido SSD Kingston 480GB', 210000.00, 18, 'DISPONIBLE'),
       (2006, 'Memoria RAM DDR4 16GB Corsair', 265000.00, 10, 'DISPONIBLE'),
       (2007, 'Audifonos Gamer HyperX', 175000.00, 20, 'DISPONIBLE'),
       (2008, 'Impresora Epson EcoTank L3250', 1350000.00, 4, 'DISPONIBLE'),
       (2009, 'Router TP Link Archer C6', 185000.00, 9, 'DISPONIBLE'),
       (2010, 'Webcam Logitech HD 1080p', 320000.00, 6, 'DISPONIBLE');


