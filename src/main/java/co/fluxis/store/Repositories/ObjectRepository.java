package co.fluxis.store.Repositories;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 *
 * @author José Ríos
 */

    /**
     */
    private final Path filePath;

    /**
     */
    private List<T> collection;

    /**
     *
     */
    public ObjectRepository(String pathName) {
        }
        this.filePath = Paths.get(pathName);
        this.collection = new ArrayList<>();
    }

    /**
     *
     */
    public void add(T object) throws IOException {
        Objects.requireNonNull(object, "No se puede agregar un objeto null");

        try {
            collection = getAll();
            collection.add(object);
            persist();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección existente", e);
        }
    }

    /**
     *
     */
    public List<T> getAll() throws IOException, ClassNotFoundException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(filePath)))) {
            collection = (List<T>) ois.readObject();
        }
    }

    /**
     *
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
     *
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
     * @throws IllegalArgumentException si el objeto es null
     */
    public boolean remove(T object) throws IOException {

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
     * @return El objeto anterior que fue reemplazado
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T update(int index, T object) throws IOException {

        try {
            collection = getAll();
            T previous = collection.set(index, object);
            persist();
            return previous;
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la colección", e);
        }
    }

    /**
     *
     * @throws IllegalArgumentException si el objeto es null
     */
    public boolean update(T object) throws IOException {
        Objects.requireNonNull(object, "El objeto no puede ser null");

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
     *
     */
    public Path getFilePath() {
        return filePath;
    }
}