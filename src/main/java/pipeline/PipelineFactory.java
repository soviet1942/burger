package pipeline;

import annotation.mysql.Column;
import annotation.mysql.Table;
import bean.Crawler;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DateUtils;
import utils.SqlUtils;

import java.lang.reflect.Field;
import java.util.*;

import static utils.SqlUtils.sqlType2JavaType;


public class PipelineFactory {

    private static final Set<String> NUMBER = new HashSet<>(Arrays.asList("Integer", "Long", "Short", "Byte", "Boolean", "Float", "Double"));
    private static final String PARAM_PREFIX = "#{";
    private static final String PARAM_SUFFIX = "}";
    private static final Set<String> STRING = new HashSet<>(Arrays.asList("String", "Timestamp", "Blob", "Date"));
    private static PipelineFactory INSTANCE;
    private static Logger LOGGER = LoggerFactory.getLogger(PipelineFactory.class);
    private Map<Class<?>, Entry<?>> ENTRY_MAP = new HashMap<>();

    private PipelineFactory() {
    }

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
        SqlUtils.verifyAllTables(); //校验
        genSqlTemplate();
    }


    /**
     * 根据实体类生成sql模板
     */
    private void genSqlTemplate() {
        loadEntry().forEach(entry -> {
            Set<Map.Entry<String, ColumnType>> entries = entry.getColumnNameType().entrySet();
            Map<String, String> columnFieldNameMap = entry.getColumnFieldName();
            StringBuilder sql = new StringBuilder("INSERT IGNORE INTO `").append(entry.getTableName()).append("` (");
            for (Map.Entry<String, ColumnType> column : entries) {
                sql.append("`").append(column.getKey()).append("`,");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(") VALUES (");
            for (Map.Entry<String, ColumnType> column : entries) {
                String sqlType = column.getValue().getSqlType();
                String token = columnFieldNameMap.get(column.getKey());
                if (StringUtils.isNotEmpty(token)) {
                    if (STRING.contains(sqlType)) {
                        sql.append("'" + PARAM_PREFIX + token + PARAM_SUFFIX + "',");
                    } else if (NUMBER.contains(sqlType)) {
                        sql.append(PARAM_PREFIX + token + PARAM_SUFFIX + ",");
                    } else {
                        throw new IllegalArgumentException("unknow type: '" + sqlType + "' in sql, class=[" + entry.getClazz().getName() + "], field=[" + column.getKey() + "]");
                    }
                }
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
            entry.setSqlTemplate(sql.toString());
            ENTRY_MAP.put(entry.getClazz(), entry);
        });
    }

    /**
     * 加载所有sql实体类
     */
    private List<Entry<?>> loadEntry() {
        List<Entry<?>> entries = new ArrayList<>();
        Reflections reflections = Crawler.getReflection();
        Set<Class<?>> tableClasses = reflections.getTypesAnnotatedWith(Table.class);
        tableClasses.forEach(clazz -> {
            Table table = clazz.getAnnotation(Table.class);
            if (StringUtils.isNotEmpty(table.value())) {
                Map<String, ColumnType> columnTypeMap = new HashMap<>();
                Map<String, String> columnFieldNameMap = new HashMap<>();
                Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && column.insertable() == true) {
                        String columnName = column.name();
                        String columnType = column.columnDefinition() == null ? sqlType2JavaType(column.columnDefinition()) : field.getType().getSimpleName();
                        columnTypeMap.put(columnName, new ColumnType(field.getType(), columnType));
                        columnFieldNameMap.put(columnName, field.getName());
                    }
                });
                Entry entry = new Entry();
                entry.setTableName(table.value());
                entry.setColumnNameType(columnTypeMap);
                entry.setColumnFieldName(columnFieldNameMap);
                entry.setClazz(clazz);
                entries.add(entry);
            }
        });
        return entries;
    }

    /**
     * 生成sql语句
     * @param clazz 实体类所属的Class
     * @param objects 实体类列表 -> 取值注入sql模板
     * @return
     */
    public List<String> generateSql(Class<?> clazz, List<Object> objects) {
        List<String> sqlList = new ArrayList<>();
        Entry<?> entry = ENTRY_MAP.get(clazz);
        Collection<String> fieldsFilter = entry.getColumnFieldName().values();
        for (Object obj : objects) {
            Map<String, Object> fieldNameValueMap = new HashMap<>();
            for (Field field : obj.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if (!fieldsFilter.contains(fieldName)) {
                        continue;
                    }
                    Object fieldValue = field.get(obj);
                    if (fieldValue != null && field.getType() == Date.class) {
                        fieldValue = DateUtils.defaultDateFormat((Date) fieldValue);
                    }
                    fieldNameValueMap.put(fieldName, fieldValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            String sql = generateSql(entry.getSqlTemplate(), fieldNameValueMap);
            sqlList.add(sql);
        }
        return sqlList;
    }

    /**
     * 生成sql语句
     *
     * @param tempSql sql模板
     * @param fieldNameValue 实体类的变量名和变量值
     * @return
     */
    private String generateSql(String tempSql, Map<String, Object> fieldNameValue) {
        String sql = tempSql;
        for (Map.Entry<String, Object> e : fieldNameValue.entrySet()) {
            String fieldName = e.getKey();
            String fieldValue = e.getValue() == null ? "NULL" : e.getValue().toString();
            String token = PARAM_PREFIX + fieldName + PARAM_SUFFIX;
            int begin = sql.indexOf(token);
            int end = begin + token.length();
            if (fieldValue.equals("NULL") && sql.charAt(begin - 1) == '\'' && sql.charAt(end) == '\'') {
                begin--;
                end++;
            }
            fieldValue = fieldValue.replaceAll("'", "''");
            sql = sql.substring(0, begin) + fieldValue + sql.substring(end);
        }
        return sql;
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

    static class Entry<T> {
        private Class<T> clazz;
        private Map<String, String> columnFieldName; //sql表字段名称和实体类对应的变量名
        private Map<String, ColumnType> columnNameType; //sql表字段名称和字段类型
        private String sqlTemplate;
        private String tableName;

        public Class<T> getClazz() {
            return clazz;
        }

        public void setClazz(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Map<String, String> getColumnFieldName() {
            return columnFieldName;
        }

        public void setColumnFieldName(Map<String, String> columnFieldName) {
            this.columnFieldName = columnFieldName;
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

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }
}
