package co.fluxis.store.mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetMapper<T> {

    private final Class<T> clazz;
    private final Map<Field, String> fieldColumnMap;

    public ResultSetMapper(Class<T> clazz) {
        this.clazz = clazz;
        this.fieldColumnMap = buildFieldColumnMap();
    }

    /**
     * Mapea una fila del ResultSet a un objeto del tipo T
     */
    public T mapRow(ResultSet rs) throws SQLException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Map.Entry<Field, String> entry : fieldColumnMap.entrySet()) {
                Field field = entry.getKey();
                String columnName = entry.getValue();

                field.setAccessible(true);
                Object value = getValueFromResultSet(rs, columnName, field.getType());
                field.set(instance, value);
            }

            return instance;
        } catch (Exception e) {
            throw new SQLException("Error mapeando ResultSet a " + clazz.getName(), e);
        }
    }

    /**
     * Mapea un ResultSet completo a una lista de objetos
     */
    public List<T> mapResultSet(ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }

    /**
     * Construye el mapa de campos y nombres de columnas usando anotaciones JPA
     */
    private Map<Field, String> buildFieldColumnMap() {
        Map<Field, String> map = new HashMap<>();

        // Obtener todos los campos, incluyendo los heredados
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();

            for (Field field : fields) {
                // Ignorar campos static, transient o marcados con @Transient de JPA
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                        java.lang.reflect.Modifier.isTransient(field.getModifiers()) ) {
                    continue;
                }

                String columnName = getColumnName(field);
                map.put(field, columnName);
            }

            currentClass = currentClass.getSuperclass();
        }

        return map;
    }

    /**
     * Obtiene el nombre de la columna desde la anotación @Column de JPA
     */
    private String getColumnName(Field field) {
        // Primero intenta con @Column

        /*
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            String name = column.name();
            if (!name.isEmpty()) {
                return name;
            }
        }

        */

        // Si no tiene @Column o el name está vacío, usa el nombre del campo
        // convertido de camelCase a snake_case
        return camelToSnake(field.getName());
    }

    /**
     * Obtiene el valor del ResultSet según el tipo del campo
     */
    private Object getValueFromResultSet(ResultSet rs, String columnName, Class<?> fieldType)
            throws SQLException {

        // Verificar si la columna existe en el ResultSet
        try {
            rs.findColumn(columnName);
        } catch (SQLException e) {
            // Si la columna no existe, retornar null
            return null;
        }

        Object value = rs.getObject(columnName);

        if (value == null) {
            return null;
        }

        // Tipos primitivos y wrappers
        if (fieldType == Integer.class || fieldType == int.class) {
            return rs.getInt(columnName);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return rs.getLong(columnName);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return rs.getDouble(columnName);
        } else if (fieldType == Float.class || fieldType == float.class) {
            return rs.getFloat(columnName);
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return rs.getBoolean(columnName);
        } else if (fieldType == Byte.class || fieldType == byte.class) {
            return rs.getByte(columnName);
        } else if (fieldType == Short.class || fieldType == short.class) {
            return rs.getShort(columnName);
        } else if (fieldType == String.class) {
            return rs.getString(columnName);
        } else if (fieldType == BigDecimal.class) {
            return rs.getBigDecimal(columnName);
        } else if (fieldType == LocalDate.class) {
            java.sql.Date date = rs.getDate(columnName);
            return date != null ? date.toLocalDate() : null;
        } else if (fieldType == LocalDateTime.class) {
            java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } else if (fieldType == java.sql.Date.class) {
            return rs.getDate(columnName);
        } else if (fieldType == java.sql.Time.class) {
            return rs.getTime(columnName);
        } else if (fieldType == java.sql.Timestamp.class) {
            return rs.getTimestamp(columnName);
        } else if (fieldType.isEnum()) {
            String enumValue = rs.getString(columnName);
            return enumValue != null ? Enum.valueOf((Class<Enum>) fieldType, enumValue) : null;
        } else if (fieldType == byte[].class) {
            return rs.getBytes(columnName);
        }

        // Si no es ninguno de los tipos conocidos, retornar el objeto tal cual
        return value;
    }

    /**
     * Convierte camelCase a snake_case
     */
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}