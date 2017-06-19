package com.haier.uhome.usend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.anye.greendao.gen.DaoMaster;
import com.anye.greendao.gen.DaoSession;

/**
 * ${todo}(这里用一句话描述这个类的作用)
 *
 * @author majunling
 * @date 2017/6/11
 */

public class DBHelper {
    private static DBHelper sInstance;
    private DaoSession daoSession;

    public static DBHelper getInstance(){
        if(sInstance == null){
            synchronized (DBHelper.class){
                if(sInstance == null){
                    sInstance = new DBHelper();
                }
            }
        }
        return sInstance;
    }

    public DaoSession getDBSessioin(Context context) {
        if(daoSession != null){
            return daoSession;
        }
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "ua", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        return daoSession;
    }
}
