package com.sigiv.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico del patrón <b>DAO (Data Access Object)</b>.
 *
 * <p>Esta interfaz formaliza el patrón que estructura toda la capa de
 * persistencia de SIGIV-SM: define <i>qué</i> operaciones de acceso a datos
 * expone una entidad, sin atarse a <i>cómo</i> se implementan (JDBC + MySQL en
 * este prototipo). Al programar contra la interfaz, las capas de servicio
 * quedan desacopladas del motor de base de datos: si mañana se cambiara MySQL
 * por otra tecnología, bastaría con proveer otra implementación de
 * {@code DAO<T>} sin tocar la lógica de negocio.</p>
 *
 * <p>El uso de <b>genéricos</b> ({@code <T>}) permite reutilizar el mismo
 * contrato para cualquier entidad del dominio (Producto, Cliente, etc.),
 * evidenciando reutilización de métodos a través de una abstracción común.</p>
 *
 * @param <T> tipo de la entidad del dominio gestionada por el DAO
 */
public interface DAO<T> {

    /** Devuelve todas las entidades activas. */
    List<T> listar() throws SQLException;

    /** Busca una entidad por su identificador; vacío si no existe. */
    Optional<T> buscarPorId(int id) throws SQLException;

    /** Inserta una entidad nueva y devuelve el id autogenerado. */
    int insertar(T entidad) throws SQLException;

    /** Persiste los cambios de una entidad existente. */
    void actualizar(T entidad) throws SQLException;

    /** Baja lógica de la entidad (marca {@code activo = false}). */
    void darDeBaja(int id) throws SQLException;
}
