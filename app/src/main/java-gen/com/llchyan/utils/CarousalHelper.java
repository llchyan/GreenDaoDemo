package com.llchyan.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.llchyan.bean.Carousal;
import com.llchyan.dao.CarousalDao;
import com.llchyan.dao.DaoMaster;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by LinLin on 2015/12/23.NoteHelper
 */
public class CarousalHelper extends BaseBeanHelper<Carousal>
{
    public static final String DB_NAME = "carousal.db";
    private static CarousalHelper helper;
    private CarousalDao carousalDao;

    private CarousalHelper()
    {
    }

    public CarousalHelper(Context mContext)
    {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        carousalDao = new DaoMaster(database).newSession().getCarousalDao();
    }

    public static void init(Context context)
    {
        if (null == helper)
        {
            synchronized (CarousalHelper.class)
            {
                if (null == helper)
                    helper = new CarousalHelper(context);
            }
        }
    }

    public static CarousalHelper getInstance()
    {
        if (null == helper)
            throw new NullPointerException("you must be Initialize  UserDaoHelper before used");
        return helper;
    }

    public CarousalDao getCarousalDao()
    {
        return carousalDao;
    }

    @Override
    public void insert(Carousal carousal)
    {
        Carousal mNote = findByID(carousal.getCarousal_id());
        if (null == mNote)
        {
            carousalDao.insert(mNote);
        } else
        {
            carousalDao.update(mNote);
        }
    }

    @Override
    public void delete(Carousal carousal)
    {
        Carousal mNote = findByID(carousal.getCarousal_id());
        if (null != mNote)
            carousalDao.delete(mNote);
    }

    @Override
    public void clear()
    {
        carousalDao.deleteAll();
    }

    @Override
    public void update(Carousal carousal)
    {
        Carousal mNote = findByID(carousal.getCarousal_id());
        if (null != mNote)
            carousalDao.update(mNote);
    }

    @Override
    public Carousal findByID(long id)
    {
        QueryBuilder<Carousal> builder = carousalDao.queryBuilder().where(CarousalDao.Properties.Carousal_id.eq(id));
        return builder.unique();
    }

    @Override
    public Carousal find(WhereCondition condition)
    {
        QueryBuilder<Carousal> builder = carousalDao.queryBuilder().where(condition);
        return builder.unique();
    }

    @Override
    public List<Carousal> findList(WhereCondition condition)
    {
        QueryBuilder<Carousal> builder = carousalDao.queryBuilder().where(condition);
        return builder.list();
    }


}
