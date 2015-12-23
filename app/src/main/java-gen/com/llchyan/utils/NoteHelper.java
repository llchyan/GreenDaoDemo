package com.llchyan.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.llchyan.bean.Note;
import com.llchyan.dao.DaoMaster;
import com.llchyan.dao.NoteDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by LinLin on 2015/12/23.NoteHelper
 */
public class NoteHelper extends BaseBeanHelper<Note>
{
    public static final String DB_NAME = "note.db";
    private static NoteHelper helper;
    private NoteDao mNoteDao;

    private NoteHelper()
    {
    }

    public NoteHelper(Context mContext)
    {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        mNoteDao = new DaoMaster(database).newSession().getNoteDao();
    }

    public static void init(Context context)
    {
        if (null == helper)
        {
            synchronized (NoteHelper.class)
            {
                if (null == helper)
                    helper = new NoteHelper(context);
            }
        }
    }

    public static NoteHelper getInstance()
    {
        if (null == helper)
            throw new NullPointerException("you must be Initialize  UserDaoHelper before used");
        return helper;
    }

    public NoteDao getNoteDao()
    {
        return mNoteDao;
    }

    @Override
    public void insert(Note note)
    {
        Note mNote = findByID(note.getId());
        if (null == mNote)
        {
            mNoteDao.insert(mNote);
        } else
        {
            mNoteDao.update(mNote);
        }
    }

    @Override
    public void delete(Note note)
    {
        Note mNote = findByID(note.getId());
        if (null != mNote)
            mNoteDao.delete(mNote);
    }

    @Override
    public void clear()
    {
        mNoteDao.deleteAll();
    }

    @Override
    public void update(Note note)
    {
        Note mNote = findByID(note.getId());
        if (null != mNote)
            mNoteDao.update(mNote);
    }

    @Override
    public Note findByID(long id)
    {
        QueryBuilder<Note> builder = mNoteDao.queryBuilder().where(NoteDao.Properties.Id.eq(id));
        return builder.unique();
    }

    @Override
    public Note find(WhereCondition condition)
    {
        QueryBuilder<Note> builder = mNoteDao.queryBuilder().where(condition);
        return builder.unique();
    }

    @Override
    public List<Note> findList(WhereCondition condition)
    {
        QueryBuilder<Note> builder = mNoteDao.queryBuilder().where(condition);

        return builder.list();
    }


}
