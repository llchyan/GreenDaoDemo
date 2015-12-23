package com.llchyan.utils;

import java.util.List;

import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by LinLin on 2015/12/23.
 */
public abstract class BaseBeanHelper<T>
{
    public abstract void insert(T t);

    public abstract void delete(T t);

    public abstract void clear();

    public abstract void update(T t);

    public abstract T findByID(long id);

    public abstract T find(WhereCondition condition);

    public abstract List<T> findList(WhereCondition condition);
}
