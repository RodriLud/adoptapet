DROP DATABASE IF EXISTS bd_adopcion;
CREATE DATABASE bd_adopcion;
USE bd_adopcion;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'ROLE_ADOPTANTE',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_rol_valido CHECK (rol IN ('ROLE_ADMIN', 'ROLE_TRABAJADOR', 'ROLE_ADOPTANTE'))
);

CREATE TABLE trabajador (
    id_usuario INT PRIMARY KEY,
    nom_trabajador VARCHAR(100) NOT NULL,
    ape_trabajador VARCHAR(100) NOT NULL,
    dni CHAR(8) UNIQUE NOT NULL,
    fec_nacimiento DATE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono CHAR(9) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE adoptante (
    id_usuario INT PRIMARY KEY,
    nom_adoptante VARCHAR(100) NOT NULL,
    ape_adoptante VARCHAR(100) NOT NULL,
    fec_nacimiento DATE NOT NULL,
    dni CHAR(8) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono CHAR(9) NOT NULL,
    direccion VARCHAR(150),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE mascota (
    id_mascota INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    especie VARCHAR(20) NOT NULL,
    raza VARCHAR(50) NOT NULL,
    sexo VARCHAR(20) NOT NULL DEFAULT 'MACHO',
    edad_anios INT DEFAULT 0,
    edad_meses INT DEFAULT 0,
    estado_salud VARCHAR(50) NOT NULL,
    estado_adopcion VARCHAR(20) DEFAULT 'DISPONIBLE',
    estado_esterilizacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    observaciones VARCHAR(500),
    ruta_imagen VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_edad_anios CHECK (edad_anios >= 0),
    CONSTRAINT chk_edad_meses CHECK (edad_meses >= 0 AND edad_meses < 12),
    CONSTRAINT chk_mascota_sexo CHECK (sexo IN ('MACHO', 'HEMBRA')),
    CONSTRAINT chk_mascota_salud CHECK (estado_salud IN ('SIN NOVEDADES', 'EN TRATAMIENTO', 'CRITICO')),
    CONSTRAINT chk_mascota_adopcion CHECK (estado_adopcion IN ('DISPONIBLE', 'ADOPTADO', 'RESERVADO', 'INACTIVO')),
    CONSTRAINT chk_mascota_esterilizacion CHECK (estado_esterilizacion IN ('ESTERILIZADO', 'NO ESTERILIZADO', 'PENDIENTE', 'NO APLICA'))
);

CREATE TABLE solicitud (
    id_solicitud INT AUTO_INCREMENT PRIMARY KEY,
    fecha_registro DATE DEFAULT (CURRENT_DATE),
    id_adoptante INT NOT NULL,
    id_mascota INT NOT NULL,
    id_trabajador INT,
    comentario TEXT,
    ruta_pdf_acta VARCHAR(255),
    motivo_adopcion TEXT,
    tipo_vivienda VARCHAR(100),
    experiencia_mascotas TEXT,
    otras_mascotas TEXT,
    cantidad_personas_hogar INT,
    comentarios_adicionales TEXT,
    ruta_dni VARCHAR(255),
    ruta_domicilio VARCHAR(255),
    ruta_acta_firmada VARCHAR(255),
    cantidad_reprogramaciones INT NOT NULL DEFAULT 0,
    motivo_contingencia TEXT,
    fecha_cierre_contingencia DATETIME,
    estado_solicitud VARCHAR(20) DEFAULT 'PENDIENTE',
    motivo_rechazo TEXT,
    CONSTRAINT chk_solicitud_estado CHECK (estado_solicitud IN ('PENDIENTE', 'APROBADA', 'NO_ASISTIO', 'RECHAZADA', 'FINALIZADA', 'CANCELADA')),
    CONSTRAINT chk_personas_hogar CHECK (cantidad_personas_hogar IS NULL OR cantidad_personas_hogar >= 0),
    FOREIGN KEY (id_adoptante) REFERENCES adoptante(id_usuario),
    FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota),
    FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_usuario) ON DELETE SET NULL
);

CREATE TABLE cupo_entrega (
    id_cupo INT AUTO_INCREMENT PRIMARY KEY,
    fecha_entrega DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    capacidad INT NOT NULL DEFAULT 2,
    reservadas INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_cupo_capacidad CHECK (capacidad > 0),
    CONSTRAINT chk_cupo_reservadas CHECK (reservadas >= 0 AND reservadas <= capacidad),
    CONSTRAINT uq_cupo_entrega_horario UNIQUE (fecha_entrega, hora_inicio, hora_fin)
);

CREATE TABLE programacion_entrega (
    id_programacion INT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud INT NOT NULL UNIQUE,
    id_cupo INT NOT NULL,
    fecha_entrega DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    fecha_limite_recojo DATETIME NOT NULL,
    estado_programacion VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
    motivo_cancelacion TEXT,
    observacion TEXT,
    CONSTRAINT chk_programacion_estado CHECK (estado_programacion IN ('PROGRAMADA', 'NO_ASISTIO', 'COMPLETADA', 'CANCELADA')),
    FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
    FOREIGN KEY (id_cupo) REFERENCES cupo_entrega(id_cupo)
);

CREATE INDEX idx_usuario_username ON usuario(username);
CREATE INDEX idx_usuario_rol ON usuario(rol);
CREATE INDEX idx_mascota_estado ON mascota(estado_adopcion);
CREATE UNIQUE INDEX uq_mascota_identidad_estado ON mascota(nombre, especie, raza, estado_adopcion);
CREATE INDEX idx_solicitud_estado ON solicitud(estado_solicitud);
CREATE INDEX idx_solicitud_adoptante ON solicitud(id_adoptante);
CREATE INDEX idx_solicitud_mascota ON solicitud(id_mascota);
CREATE INDEX idx_solicitud_trabajador ON solicitud(id_trabajador);
CREATE INDEX idx_programacion_estado ON programacion_entrega(estado_programacion);
CREATE INDEX idx_programacion_fecha ON programacion_entrega(fecha_entrega, hora_inicio, hora_fin);

-- LIMPIAR TABLAS PARA EVITAR CONFLICTOS DE LLAVES PRIMARIAS
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE programacion_entrega;
TRUNCATE TABLE cupo_entrega;
TRUNCATE TABLE solicitud;
TRUNCATE TABLE mascota;
TRUNCATE TABLE adoptante;
TRUNCATE TABLE trabajador;
TRUNCATE TABLE usuario;

-- =========================================================================
-- DATOS DE PRUEBA
-- Usuarios (password - 123)
-- =========================================================================
INSERT INTO usuario (id_usuario, username, password, rol, activo) VALUES
(1, 'admin', '$2a$12$sF84YBQQDkPrtIYv8rjr5.eBr41yq2hPSKvbn490SOBMCtagHegNe', 'ROLE_ADMIN', TRUE),
(2, 'pepe', '$2a$12$sF84YBQQDkPrtIYv8rjr5.eBr41yq2hPSKvbn490SOBMCtagHegNe', 'ROLE_TRABAJADOR', TRUE),
(3, 'rodrigo', '$2a$12$sF84YBQQDkPrtIYv8rjr5.eBr41yq2hPSKvbn490SOBMCtagHegNe', 'ROLE_ADOPTANTE', TRUE);

-- =========================================================================
-- TRABAJADOR BASE
-- =========================================================================
INSERT INTO trabajador (id_usuario, nom_trabajador, ape_trabajador, dni, fec_nacimiento, email, telefono) VALUES
(1, 'Admin', 'General', '11111111', '1990-01-01', 'admin@pet.com', '999888777'),
(2, 'Pepe', 'Lucho', '22222222', '1995-05-15', 'pepe@pet.com', '999111222');

-- =========================================================================
-- ADOPTANTE BASE
-- =========================================================================
INSERT INTO adoptante (id_usuario, nom_adoptante, ape_adoptante, fec_nacimiento, dni, email, telefono, direccion) VALUES
(3, 'Rodrigo', 'Leon', '2000-10-10', '33333333', 'rodrigo@mail.com', '987654321', 'Av. Lima 123, Miraflores');

-- =========================================================================
-- MASCOTAS BASE PARA LA APP
-- 4 disponibles para Android y 1 reservada para probar contingencia.
-- =========================================================================
INSERT INTO mascota (id_mascota, nombre, especie, raza, sexo, edad_anios, edad_meses, estado_salud, estado_adopcion, estado_esterilizacion, observaciones) VALUES
(1, 'Lucas', 'Perro', 'Beagle', 'MACHO', 2, 4, 'SIN NOVEDADES', 'DISPONIBLE', 'ESTERILIZADO', 'Buen temperamento y activo. Ideal para familia con ninos.'),
(2, 'Nala', 'Gato', 'Mestizo', 'HEMBRA', 1, 7, 'SIN NOVEDADES', 'DISPONIBLE', 'ESTERILIZADO', 'Tranquila y apta para departamento.'),
(3, 'Bruno', 'Perro', 'Pug', 'MACHO', 4, 9, 'SIN NOVEDADES', 'DISPONIBLE', 'ESTERILIZADO', 'Requiere paseos cortos y constantes.'),
(4, 'Kira', 'Perro', 'Siberiano', 'HEMBRA', 1, 1, 'SIN NOVEDADES', 'DISPONIBLE', 'PENDIENTE', 'Energetica, sociable y en evaluacion para esterilizacion.'),
(5, 'Mia', 'Gato', 'Bengala', 'HEMBRA', 1, 2, 'SIN NOVEDADES', 'RESERVADO', 'ESTERILIZADO', 'Reservada para el caso de prueba de cierre por contingencia.');

-- =========================================================================
-- SOLICITUD MINIMA PARA PROBAR CIERRE POR CONTINGENCIA
-- Ingresar como admin o trabajador y abrir el detalle de la solicitud 1.
-- =========================================================================
INSERT INTO solicitud (id_solicitud, fecha_registro, id_adoptante, id_mascota, id_trabajador, comentario, motivo_adopcion, tipo_vivienda, experiencia_mascotas, otras_mascotas, cantidad_personas_hogar, estado_solicitud, motivo_rechazo, ruta_pdf_acta, ruta_acta_firmada) VALUES
(1, '2026-06-01', 3, 5, 2, 'Caso precargado para cerrar por contingencia', 'Compania familiar', 'Casa', 'Basica', 'No', 3, 'APROBADA', NULL, 'documentos/acta_adopcion_1.pdf', NULL);

-- =========================================================================
-- CUPO VENCIDO PARA CONTINGENCIA
-- =========================================================================
INSERT INTO cupo_entrega (id_cupo, fecha_entrega, hora_inicio, hora_fin, capacidad, reservadas) VALUES
(1, '2026-06-02', '10:00:00', '12:00:00', 2, 1);

-- =========================================================================
-- PROGRAMACION VENCIDA PARA CONTINGENCIA
-- =========================================================================
INSERT INTO programacion_entrega (id_programacion, id_solicitud, id_cupo, fecha_entrega, hora_inicio, hora_fin, fecha_limite_recojo, estado_programacion, motivo_cancelacion, observacion) VALUES
(1, 1, 1, '2026-06-02', '10:00:00', '12:00:00', '2026-06-02 12:30:00', 'PROGRAMADA', NULL, 'Caso de prueba para cierre por contingencia.');
