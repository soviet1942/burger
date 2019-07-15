package pipeline;

import annotation.mysql.Column;
import annotation.mysql.Table;
import bean.Crawler;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static utils.SqlUtils.isInitialize;
import static utils.SqlUtils.sqlType2JavaType;


public class PipelineFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(PipelineFactory.class);
    private List<Entry<?>> ENTRIES = new ArrayList<>();
    private static final String PARAM_PREFIX = "#{";
    private static final String PARAM_SUFFIX = "}";
    private static final Set<String> STRING = new HashSet<>(Arrays.asList("String", "Timestamp", "Blob"));
    private static final Set<String> NUMBER = new HashSet<>(Arrays.asList("Integer", "Long", "Short", "Byte", "Boolean", "Float", "Double"));
    private static PipelineFactory INSTANCE;

    private PipelineFactory() {}

    public static PipelineFactory instance() {
        if (INSTANCE == null) {
            synchronized (PipelineFactory.class) {
                if (INSTANCE == null) {
                    PipelineFactory pipelineFactory = new PipelineFactory();
                    pipelineFactory.init();
                    INSTANCE = pipelineFactory;
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        if (isInitialize()) {
            loadAllEntry();
            prepareSql();
        }
    }

    private String generateSql(Entry<?> ent, Map<String, Object> fieldNameValue) {
        String sql = ent.getSqlTemplate();
        for (Map.Entry<String, Object> e : fieldNameValue.entrySet()) {
            sql = sql.replaceFirst(PARAM_PREFIX + e.getKey() + PARAM_SUFFIX, e.getValue().toString());
        }
        return sql;
    }

    private void loadAllEntry() {
        Reflections reflections = Crawler.getReflection();
        Set<Class<?>> tableClasses = reflections.getTypesAnnotatedWith(Table.class);
        tableClasses.forEach(clazz -> {
            Table table = clazz.getAnnotation(Table.class);
            if (StringUtils.isNotEmpty(table.value())) {
                Map<String, ColumnType> columnTypeMap = new HashMap<>();
                Arrays.stream(clazz.getFields()).forEach(field -> {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && column.insertable() == true) {
                        String columnName = column.name();
                        String columnType = column.columnDefinition() == null ? sqlType2JavaType(column.columnDefinition()) : field.getType().getSimpleName();
                        columnTypeMap.put(columnName, new ColumnType(field.getType(), columnType));
                    }
                });
                Entry entry = new Entry();
                entry.setTableName(table.value());
                entry.setColumnNameType(columnTypeMap);
                ENTRIES.add(entry);
            }
        });
    }

    private void prepareSql() {
        ENTRIES.forEach(entry -> {
            Set<Map.Entry<String, ColumnType>> entries = entry.getColumnNameType().entrySet();
            StringBuilder sql = new StringBuilder("INSERT IGNORE INTO `").append(entry.getTableName()).append("` (");
            for (Map.Entry<String, ColumnType> column : entries) {
                sql.append("`").append(column.getKey()).append("`,");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(") VLAUES (");
            for (Map.Entry<String, ColumnType> column : entries) {
                String sqlType = column.getValue().getSqlType();
                if (STRING.contains(sqlType)) {
                    sql.append("'" + PARAM_PREFIX + column.getKey() + PARAM_SUFFIX + "',");
                } else if (NUMBER.contains(sqlType)) {
                    sql.append(PARAM_PREFIX + column.getKey() + "},");
                } else if ("Date".equals(sqlType)) {
                    sql.append("DATE '" + PARAM_PREFIX + column.getKey() + PARAM_SUFFIX + "',");
                } else {
                    throw new IllegalArgumentException("unknow type: '" + sqlType + "' in sql, class=[" + entry.getClazz().getName() + "], field=[" + column.getKey() + "]");
                }
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
            entry.setSqlTemplate(sql.toString());
        });
    }

    static class Entry<T> {
        private String tableName;
        private Map<String, ColumnType> columnNameType;
        private String sqlTemplate;
        private Class<T> clazz;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Map<String, ColumnType> getColumnNameType() {
            return columnNameType;
        }

        public void setColumnNameType(Map<String, ColumnType> columnNameType) {
            this.columnNameType = columnNameType;
        }

        public String getSqlTemplate() {
            return sqlTemplate;
        }

        public void setSqlTemplate(String sqlTemplate) {
            this.sqlTemplate = sqlTemplate;
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public void setClazz(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

    static class ColumnType {
        private Class<?> javaType;
        private String sqlType;

        public ColumnType(Class<?> javaType, String sqlType) {
            this.javaType = javaType;
            this.sqlType = sqlType;
        }

        public Class<?> getJavaType() {
            return javaType;
        }

        public void setJavaType(Class<?> javaType) {
            this.javaType = javaType;
        }

        public String getSqlType() {
            return sqlType;
        }

        public void setSqlType(String sqlType) {
            this.sqlType = sqlType;
        }
    }
}
