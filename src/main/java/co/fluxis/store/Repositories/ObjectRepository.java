package co.fluxis.store.Repositories;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

/**
 * Repositorio genérico que permite persistir, recuperar, buscar y ordenar
 * colecciones de objetos en archivos binarios.
 *
 * @param <T> Tipo de objetos que serán almacenados en el repositorio
 *
 * @author José Ríos
 * @version 3.0
 */
public class ObjectRepository<T extends Serializable> implements Serializable {

    /**
     * Ruta del archivo donde se almacenará la colección de objetos
     */
    private final Path filePath;

    /**
     * Colección en memoria de los objetos del repositorio
     */
    private List<T> collection;

    /**
     * Constructor que inicializa el repositorio con la ruta del archivo
     * especificada.
     *
     * @param pathName Ruta del archivo donde se almacenarán los objetos
     * @throws IllegalArgumentException si pathName es null o vacío
     */
    public ObjectRepository(String pathName) {
        if (pathName == null || pathName.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser null o vacía");
        }
        this.filePath = Paths.get(pathName);
        this.collection = new ArrayList<>();
    }

    /**
     * Agrega un objeto a la colección y lo persiste en el archivo.
     *
     * @param object Objeto a agregar al repositorio
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si el objeto es null
     */
    public void add(T object) throws IOException {

        Objects.requireNonNull(object, "No se puede agregar un objeto null");

        if (collection.contains(object)) {
            throw new IllegalArgumentException("El objeto que intenta agregar ya existe en la colección.");
        }

        try {
            collection = getAll();
            collection.add(object);
            persist();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección existente", e);
        }
    }

    /**
     * Carga todos los objetos desde el archivo de persistencia.
     *
     * @return Lista con todos los objetos almacenados, o lista vacía si el
     * archivo no existe
     * @throws IOException si ocurre un error de entrada/salida
     * @throws ClassNotFoundException si no se puede encontrar la clase del
     * objeto almacenado
     */
    public List<T> getAll() throws IOException, ClassNotFoundException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(filePath)))) {
            collection = (List<T>) ois.readObject();
            return new ArrayList<>(collection);
        }
    }

    /**
     * Persiste la colección actual en el archivo binario. Crea los directorios
     * padre si no existen.
     *
     * @throws IOException si ocurre un error de entrada/salida
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
     * Elimina el objeto en la posición especificada.
     *
     * @param index Índice del objeto a eliminar
     * @return El objeto eliminado
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
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
     * @param object Objeto a eliminar
     * @return true si el objeto fue encontrado y eliminado, false en caso
     * contrario
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si el objeto es null
     */
    public boolean remove(T object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("No se puede eliminar un objeto null");
        }

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
     * @param index Índice del objeto a actualizar
     * @param object Nuevo objeto que reemplazará al anterior
     * @return El objeto anterior que fue reemplazado
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si el objeto es null
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T update(int index, T object) throws IOException {

        Objects.requireNonNull(object, "No se puede actualizar con un objeto null");

        try {
            collection = getAll();

            if (collection.contains(object)) {
                throw new IllegalArgumentException("El objeto que intenta agregar ya existe en la colección.");
            }

            T previous = collection.set(index, object);
            persist();
            return previous;
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }


    /**
     * Actualiza un objeto existente en la colección. Busca el objeto en la
     * colección usando equals() y lo reemplaza con la versión actualizada.
     *
     * @param object Objeto con los datos actualizados
     * @return true si el objeto fue encontrado y actualizado, false en caso
     * contrario
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si el objeto es null
     */
    public boolean update(T object) throws IOException {

        Objects.requireNonNull(object, "El objeto no puede ser null");

        if (collection.contains(object)) {
            throw new IllegalArgumentException("El objeto que intenta agregar ya existe en la colección.");
        }

        int index = collection.indexOf(object);
        if (index >= 0) {
            collection.set(index, object);
            persist();
            return true;
        }
        return false;
    }



    /**
     * Busca un objeto en la colección usando búsqueda binaria con un comparador
     * específico. La colección debe estar previamente ordenada con el mismo
     * comparador.
     *
     * @param key Objeto clave a buscar
     * @param comparator Comparador que define el criterio de ordenación y
     * búsqueda
     * @return Optional conteniendo el objeto si se encuentra, o Optional vacío
     * si no existe
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si key o comparator son null
     */
    public Optional<T> find(T key, Comparator<? super T> comparator) throws IOException {

        Objects.requireNonNull(key, "La clave de búsqueda no puede ser null");
        Objects.requireNonNull(comparator, "El comparador no puede ser null");

        try {
            collection = getAll();
            int index = Collections.binarySearch(collection, key, comparator);
            return index >= 0 ? Optional.of(collection.get(index)) : Optional.empty();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }



    /**
     * Filtra los elementos de la coleccion segun una condicion.
     *
     * @param predicate condicion que debe cumplir el elemento
     * @return lista con los elementos que coinciden
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si el predicate es null
     */
    public List<T> filter(Predicate<? super T> predicate) throws IOException {
        if (predicate == null) {
            throw new IllegalArgumentException("El criterio de filtrado no puede ser null");
        }

        try {
            collection = getAll();
            List<T> result = new ArrayList<>();

            for (T element : collection) {
                if (predicate.test(element)) {
                    result.add(element);
                }
            }

            return result;

        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la coleccion", e);
        }
    }


    /**
     * Ordena la colección usando el comparador especificado y persiste los
     * cambios.
     *
     * @param comparator Comparador que define el criterio de ordenación
     * @throws IOException si ocurre un error de entrada/salida
     * @throws IllegalArgumentException si el comparator es null
     */
    public void sort(Comparator<? super T> comparator) throws IOException {
        if (comparator == null) {
            throw new IllegalArgumentException("El comparador no puede ser null");
        }

        try {
            collection = getAll();
            Collections.sort(collection, comparator);
            persist();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     * Busca el último objeto que cumpla con el predicado y retorna su índice.
     *
     * <p>Similar a {@link #indexWhere(Predicate)}, pero busca desde el final de la
     * colección hacia el inicio, retornando el índice de la última ocurrencia.</p>
     *
     * <h2>Ejemplo de uso:</h2>
     * <pre>{@code
     * // Encontrar el último usuario registrado con edad mayor a 30
     * int ultimoIndice = repo.lastIndexWhere(u -> u.getEdad() > 30);
     * }</pre>
     *
     * @param predicate Condición que debe cumplir el objeto buscado. No puede ser null.
     * @return Índice del último objeto que cumple la condición (0-based), o -1 si ninguno cumple
     * @throws IOException si ocurre un error al leer el archivo
     * @throws IllegalArgumentException si el predicate es null
     */
    public int indexWhere(Predicate<? super T> predicate) throws IOException {
        if (predicate == null) {
            throw new IllegalArgumentException("El predicado no puede ser null");
        }

        try {
            collection = getAll();
            // Buscar desde el final hacia el inicio
            for (int i = collection.size() - 1; i >= 0; i--) {
                if (predicate.test(collection.get(i))) {
                    return i;
                }
            }
            return -1;

        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     * Obtiene la ruta del archivo de persistencia.
     *
     * @return Path representando la ruta del archivo
     */
    public Path getFilePath() {

        return filePath;
    }
}
