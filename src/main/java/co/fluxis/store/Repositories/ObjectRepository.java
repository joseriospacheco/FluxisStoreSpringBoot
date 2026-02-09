package co.fluxis.store.Repositories;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repositorio genérico que proporciona persistencia basada en archivos para colecciones de objetos.
 *
 * <p>Esta clase permite operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre una colección
 * de objetos que se persisten en formato binario utilizando serialización de Java.</p>
 *
 * <h2>Características principales:</h2>
 * <ul>
 *   <li>Garantiza que todos los objetos almacenados sean serializables (mediante restricción genérica)</li>
 *   <li>Persistencia automática en archivo binario</li>
 *   <li>Prevención de duplicados en la colección</li>
 *   <li>Creación automática de directorios padre</li>
 *   <li>Uso de streams buffered para optimizar I/O</li>
 * </ul>
 *
 * <h2>Ejemplo de uso:</h2>
 * <pre>{@code
 * // Usuario debe implementar Serializable
 * public class Usuario implements Serializable {
 *     private String nombre;
 *     private String email;
 *     // ...
 * }
 *
 * ObjectRepository<Usuario> repo = new ObjectRepository<>("data/usuarios.dat");
 *
 * // Agregar un nuevo usuario
 * Usuario usuario = new Usuario("Juan", "juan@email.com");
 * repo.add(usuario);
 *
 * // Obtener todos los usuarios
 * List<Usuario> usuarios = repo.getAll();
 * }</pre>
 *
 * <h2>Consideraciones de seguridad de hilos:</h2>
 * <p>Esta implementación <strong>NO es thread-safe</strong>. Si se accede desde múltiples hilos,
 * se debe sincronizar externamente.</p>
 *
 * @param <T> Tipo de objetos que serán almacenados. <strong>Debe implementar {@link Serializable}</strong>
 *
 * @author José Ríos
 * @version 3.1
 * @see Serializable
 * @since 1.0
 */
public class ObjectRepository<T extends Serializable> implements Serializable {

    /** Versión de serialización para compatibilidad */
    private static final long serialVersionUID = 1L;

    /**
     * Ruta del archivo donde se almacena la colección de objetos.
     * Este archivo se crea automáticamente si no existe.
     */
    private final Path filePath;

    /**
     * Colección en memoria de los objetos del repositorio.
     * Esta colección se sincroniza con el archivo en cada operación de modificación.
     */
    private List<T> collection;

    /**
     * Constructor que inicializa el repositorio con la ruta del archivo especificada.
     *
     * <p>El archivo no se crea hasta que se agregue el primer elemento.
     * Si la ruta incluye directorios que no existen, se crearán automáticamente
     * en la primera operación de persistencia.</p>
     *
     * @param pathName Ruta absoluta o relativa del archivo donde se almacenarán los objetos.
     *                 Ejemplo: "data/usuarios.dat" o "/var/app/data/usuarios.dat"
     * @throws IllegalArgumentException si pathName es null, vacío o contiene solo espacios
     */
    public ObjectRepository(String pathName) {



        if (Objects.isNull(pathName) || pathName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La ruta del archivo no puede ser null o vacía");
        }
        this.filePath = Paths.get(pathName);
        this.collection = new ArrayList<>();
    }

    /**
     * Agrega un objeto a la colección y lo persiste inmediatamente en el archivo.
     *
     * <p>Este método realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Valida que el objeto no sea null</li>
     *   <li>Carga la colección actual desde el archivo</li>
     *   <li>Verifica que el objeto no exista ya en la colección (usando equals)</li>
     *   <li>Agrega el nuevo objeto</li>
     *   <li>Persiste la colección actualizada</li>
     * </ol>
     *
     * @param object Objeto a agregar al repositorio. No puede ser null y debe ser serializable.
     * @throws IOException si ocurre un error al leer o escribir el archivo
     * @throws IllegalArgumentException si el objeto es null o ya existe en la colección
     * @see #getAll()
     * @see #persist()
     */
    public void add(T object) throws IOException {
        Objects.requireNonNull(object, "No se puede agregar un objeto null");

        try {
            collection = getAll();

            if (collection.contains(object)) {
                throw new IllegalArgumentException(
                        "El objeto que intenta agregar ya existe en la colección.");
            }

            collection.add(object);
            persist();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección existente", e);
        }
    }

    /**
     * Carga y retorna todos los objetos almacenados en el archivo de persistencia.
     *
     * <p>Si el archivo no existe, retorna una lista vacía sin generar error.
     * La lista retornada es una copia defensiva, por lo que modificarla no afecta
     * la colección interna ni el archivo.</p>
     *
     * @return Nueva instancia de {@link List} conteniendo todos los objetos almacenados.
     *         Nunca retorna null; retorna lista vacía si no hay objetos.
     * @throws IOException si ocurre un error al leer el archivo
     * @throws ClassNotFoundException si la clase de los objetos serializados no se encuentra
     *         en el classpath actual
     */
    @SuppressWarnings("unchecked")
    public List<T> getAll() throws IOException, ClassNotFoundException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(filePath)))) {
            collection = (List<T>) ois.readObject();
            return new ArrayList<>(collection); // Copia defensiva
        }
    }

    /**
     * Persiste la colección actual en el archivo binario.
     *
     * <p>Crea automáticamente los directorios padre si no existen.
     * Utiliza {@link BufferedOutputStream} para optimizar la escritura.</p>
     *
     * <p><strong>Nota:</strong> Este método sobrescribe completamente el archivo existente.</p>
     *
     * @throws IOException si ocurre un error al crear directorios o escribir el archivo
     */
    private void persist() throws IOException {
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(filePath)))) {
            oos.writeObject(collection);
            oos.flush();
        }
    }

    /**
     * Elimina el objeto en la posición especificada de la colección.
     *
     * @param index Índice del objeto a eliminar (0-based)
     * @return El objeto que fue eliminado
     * @throws IOException si ocurre un error al leer o escribir el archivo
     * @throws IndexOutOfBoundsException si el índice es negativo o mayor/igual al tamaño
     *         de la colección
     */
    public T remove(int index) throws IOException {
        try {
            collection = getAll();
            T removed = collection.remove(index);
            persist();
            return removed;
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     * Elimina la primera ocurrencia del objeto especificado de la colección.
     *
     * <p>La comparación se realiza utilizando el método {@link Object#equals(Object)}
     * del objeto proporcionado.</p>
     *
     * @param object Objeto a eliminar. No puede ser null.
     * @return {@code true} si el objeto fue encontrado y eliminado;
     *         {@code false} si no se encontró en la colección
     * @throws IOException si ocurre un error al leer o escribir el archivo
     * @throws IllegalArgumentException si el objeto es null
     */
    public boolean remove(T object) throws IOException {
        Objects.requireNonNull(object, "El objeto no puede ser null");

        try {
            collection = getAll();
            boolean removed = collection.remove(object);
            if (removed) {
                persist();
            }
            return removed;
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     * Actualiza el objeto en la posición especificada con un nuevo objeto.
     *
     * @param index Índice del objeto a actualizar (0-based)
     * @param object Nuevo objeto que reemplazará al anterior. No puede ser null.
     * @return El objeto anterior que fue reemplazado
     * @throws IOException si ocurre un error al leer o escribir el archivo
     * @throws IllegalArgumentException si el objeto es null o ya existe en otra posición
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T update(int index, T object) throws IOException {
        Objects.requireNonNull(object, "El objeto no puede ser null");

        try {
            collection = getAll();

            if (collection.contains(object)) {
                throw new IllegalArgumentException(
                        "El objeto que intenta agregar ya existe en la colección.");
            }
            T previous = collection.set(index, object);
            persist();
            return previous;
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     * Actualiza un objeto existente en la colección.
     *
     * <p>Busca el objeto en la colección usando {@link Object#equals(Object)}
     * y lo reemplaza con la versión actualizada. Este método es útil cuando
     * has modificado un objeto y necesitas actualizar su versión almacenada.</p>
     *
     * <p><strong>Nota:</strong> El objeto debe existir previamente en la colección
     * para poder ser actualizado.</p>
     *
     * @param object Objeto con los datos actualizados. No puede ser null.
     * @return {@code true} si el objeto fue encontrado y actualizado;
     *         {@code false} si no se encontró en la colección
     * @throws IOException si ocurre un error al leer o escribir el archivo
     * @throws IllegalArgumentException si el objeto es null
     */
    public boolean update(T object) throws IOException {
        Objects.requireNonNull(object, "El objeto no puede ser null");

        try {

            collection = getAll();

            if (collection.contains(object)) {
                throw new IllegalArgumentException(
                        "El objeto que intenta agregar ya existe en la colección.");
            }

            int index = collection.indexOf(object);

            if (index >= 0) {
                collection.set(index, object);
                persist();
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     * Obtiene la ruta del archivo de persistencia configurada para este repositorio.
     *
     * @return Instancia de {@link Path} representando la ruta del archivo.
     *         Nunca retorna null.
     */
    public Path getFilePath() {
        return filePath;
    }
}