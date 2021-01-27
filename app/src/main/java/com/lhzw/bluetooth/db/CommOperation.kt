package com.lhzw.bluetooth.db

import android.content.ContentValues
import org.litepal.LitePal
import org.litepal.crud.LitePalSupport


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/15 16:53
 *
 */
object CommOperation {

    /**
     * 单个插入
     */

    @Synchronized
    inline fun <T : LitePalSupport> insert(t: T) {
        t.save()
    }

    /**
     * 批量插入
     */
    @Synchronized
    fun <T : LitePalSupport> insert(list: MutableList<T>) {
        LitePal.saveAll(list)
    }

    /**
     * 条件删除
     */
    @Synchronized
    inline fun <reified T : LitePalSupport> delete(clazz: Class<T>, key: String, value: String): Int {
        return LitePal.deleteAll(clazz, "$key = ?", value)
    }

    /**
     * 删除单个表
     */
    @Synchronized
    inline fun <reified T : LitePalSupport> deleteAll(clazz: Class<T>): Int {
        return LitePal.deleteAll(clazz)
    }


    /**
     * 多条件删除
     */
    @Synchronized
    inline fun <reified T : LitePalSupport> delete(clazz: Class<T>, map: MutableMap<String, String>): Int {
        if (map == null || map.size == 0) return 0
        var sql = ""
        var conditions = arrayOfNulls<String>(map.size)
        var counter = 0
        map.forEach { (key, value) ->
            conditions[counter] = value
            counter++;
            if (counter == map.size) {
                sql += "$key = ?"
            } else {
                sql += "$key = ? and "
            }
        }
        return LitePal.deleteAll(clazz, sql, *conditions)
    }

    /**
     * 单条件更新
     */
    @Synchronized
    inline fun <reified T : LitePalSupport> update(clazz: Class<T>, values: ContentValues, id: Long): Int {
        return LitePal.update(clazz, values, id)
    }

    /**
     * 多条件更新
     */
    @Synchronized
    inline fun <T : LitePalSupport> update(clazz: Class<T>, map: MutableMap<String, String>, values: ContentValues): Int {
        if (map == null || map.size == 0) return 0
        var conditions = arrayOfNulls<String>(map.size)
        var sql = ""
        var counter = 0
        map.forEach { (key, value) ->
            conditions[counter] = value
            counter++;
            if (counter == map.size) {
                sql += "$key = ?"
            } else {
                sql += "$key = ? and "
            }
        }
        return LitePal.updateAll(clazz, values, sql, *conditions)
    }

    /**
     * 查询全部
     */
    inline fun <reified T : LitePalSupport> query(clazz: Class<T>): List<T> {
        return LitePal.findAll(clazz)
    }

    /**
     * 查询全部
     */
    inline fun <reified T : LitePalSupport> query(clazz: Class<T>, order: String): List<T> {
        return LitePal.order("$order desc").find(clazz)
    }

    /**
     * 单条件查询查询
     */
    inline fun <reified T : LitePalSupport> query(clazz: Class<T>, key: String, value: String): List<T> {
        return LitePal.where("$key = ?", value).find(clazz)
    }

    /**
     * 单条件查询查询
     */
    inline fun <reified T : LitePalSupport> query(clazz: Class<T>, key: String, value: String, order: String): List<T> {
        return LitePal.where("$key = ?", value).order("$order desc").find(clazz)
    }

    /**
     * 多条件查询
     */
    inline fun <reified T : LitePalSupport> query(clazz: Class<T>, map: MutableMap<String, String>): List<T>? {
        if (map == null || map.isEmpty()) return null
        var conditions = arrayOfNulls<String>(map.size)
        var sql = ""
        var counter = 0
        map.forEach { (key, value) ->
            conditions[counter] = value
            counter++;
            if (counter == map.size) {
                sql += "$key = ?"
            } else {
                sql += "$key = ? and "
            }
        }
        return LitePal.where(sql, *conditions).order("").find(clazz)
    }

    /**
     * 多条件查询 有排序
     */
    inline fun <reified T : LitePalSupport> query(clazz: Class<T>, map: MutableMap<String, String>, order: String): List<T>? {
        if (map == null || map.isEmpty()) return null
        var conditions = arrayOfNulls<String>(map.size)
        var sql = ""
        var counter = 0
        map.forEach { (key, value) ->
            conditions[counter] = value
            counter++;
            if (counter == map.size) {
                sql += "$key = ?"
            } else {
                sql += "$key = ? and "
            }
        }
        return LitePal.where(sql, *conditions).order(order).find(clazz)
    }

    /**
     * 模糊查询
     */

    inline fun <reified T : LitePalSupport> queryFuzzy(clazz: Class<T>, key: String, value: String): List<T>? {
        val conditions = arrayOf("%$value%")
        val sql = "$key like ?"
        return LitePal.where(sql, *conditions).find(clazz)
    }
}