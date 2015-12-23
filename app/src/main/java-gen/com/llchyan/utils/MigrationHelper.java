package com.llchyan.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.llchyan.dao.DaoMaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by LinLin on 2015/9/1. 数据库迁移助手
 */
public class MigrationHelper
{
    /**
     * 迁移助手——类与当前参数不匹配
     */
    private static final String CONVERSION_CLASS_NOT_FOUND_EXCEPTION = "MIGRATION HELPER - CLASS DOESN'T MATCH WITH THE CURRENT PARAMETERS";
    private static MigrationHelper instance;

    public static MigrationHelper getInstance()
    {
        if (instance == null)
        {
            instance = new MigrationHelper();
        }
        return instance;
    }

    /**
     * 迁移操作
     *
     * @param db
     * @param daoClasses
     */
    public void migrate(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses)
    {
        generateTempTables(db, daoClasses);
        DaoMaster.dropAllTables(db, true);
        DaoMaster.createAllTables(db, false);
        restoreData(db, daoClasses);
    }

    /**
     * 创建临时表
     *
     * @param db
     * @param daoClasses
     */
    private void generateTempTables(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses)
    {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses)
        {
            DaoConfig daoConfig = new DaoConfig(db, daoClass);

            String divider = "";
            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");//重命名
            ArrayList<String> properties = new ArrayList<>();

            StringBuilder createTableStringBuilder = new StringBuilder();

            createTableStringBuilder.append("CREATE TABLE ").append(tempTableName).append(" (");

            for (int j = 0; j < daoConfig.properties.length; j++)
            {
                String columnName = daoConfig.properties[j].columnName;//获取new class的每个字段的name

                if (getColumns(db, tableName).contains(columnName))
                {
                    properties.add(columnName);//如果new class中还包含该字段就添加进去,没有的就过滤掉

                    String type = null;

                    try
                    {
                        type = getTypeByClass(daoConfig.properties[j].type);//获取每个字段的类型，如Integer,Boolean
                    } catch (Exception exception)
                    {
                        //                        Crashlytics.logException(exception);
                    }

                    createTableStringBuilder.append(divider).append(columnName).append(" ").append(type);

                    if (daoConfig.properties[j].primaryKey)
                    {//判断是否为表 key，即表排列的依据
                        createTableStringBuilder.append(" PRIMARY KEY");
                    }

                    divider = ",";
                }
            }
            createTableStringBuilder.append(");");

            db.execSQL(createTableStringBuilder.toString());//开始创建表

            StringBuilder insertTableStringBuilder = new StringBuilder();

            //从旧表中迁移数据到新表中
            insertTableStringBuilder.append("INSERT INTO ").append(tempTableName).append(" (");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(") SELECT ");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(" FROM ").append(tableName).append(";");

            db.execSQL(insertTableStringBuilder.toString());//开始迁移
        }
    }

    /**
     * 保存数据,从临时表迁移到新的数据表中
     *
     * @param db
     * @param daoClasses
     */
    private void restoreData(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses)
    {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses)
        {
            DaoConfig daoConfig = new DaoConfig(db, daoClass);

            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            ArrayList<String> properties = new ArrayList();

            for (int j = 0; j < daoConfig.properties.length; j++)
            {
                String columnName = daoConfig.properties[j].columnName;

                if (getColumns(db, tempTableName).contains(columnName))
                {
                    properties.add(columnName);
                }
            }

            StringBuilder insertTableStringBuilder = new StringBuilder();

            //从临时表中迁移数据
            insertTableStringBuilder.append("INSERT INTO ").append(tableName).append(" (");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(") SELECT ");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(" FROM ").append(tempTableName).append(";");

            StringBuilder dropTableStringBuilder = new StringBuilder();

            dropTableStringBuilder.append("DROP TABLE ").append(tempTableName);

            db.execSQL(insertTableStringBuilder.toString());//开始迁移
            db.execSQL(dropTableStringBuilder.toString());//删除表
        }
    }

    /**
     * 判断字段类型
     *
     * @param type
     * @return
     * @throws Exception
     */
    private String getTypeByClass(Class<?> type) throws Exception
    {
        if (type.equals(String.class))
        {
            return "TEXT";
        }
        if (type.equals(Long.class) || type.equals(Integer.class) || type.equals(long.class))
        {
            return "INTEGER";
        }
        if (type.equals(Boolean.class))
        {
            return "BOOLEAN";
        }

        Exception exception = new Exception(CONVERSION_CLASS_NOT_FOUND_EXCEPTION.concat(" - Class: ").concat(type.toString()));
        //        Crashlytics.logException(exception);
        throw exception;
    }

    /**
     * 获取所有字段集合
     *
     * @param db
     * @param tableName
     * @return
     */
    private static List<String> getColumns(SQLiteDatabase db, String tableName)
    {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null)
            {
                columns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e)
        {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally
        {
            if (cursor != null)
                cursor.close();
        }
        return columns;
    }
}
