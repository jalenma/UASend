package com.anye.greendao.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.haier.uhome.usend.data.UserData;
import com.haier.uhome.usend.staticcid.SendCidData;

import com.anye.greendao.gen.UserDataDao;
import com.anye.greendao.gen.SendCidDataDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig userDataDaoConfig;
    private final DaoConfig sendCidDataDaoConfig;

    private final UserDataDao userDataDao;
    private final SendCidDataDao sendCidDataDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        userDataDaoConfig = daoConfigMap.get(UserDataDao.class).clone();
        userDataDaoConfig.initIdentityScope(type);

        sendCidDataDaoConfig = daoConfigMap.get(SendCidDataDao.class).clone();
        sendCidDataDaoConfig.initIdentityScope(type);

        userDataDao = new UserDataDao(userDataDaoConfig, this);
        sendCidDataDao = new SendCidDataDao(sendCidDataDaoConfig, this);

        registerDao(UserData.class, userDataDao);
        registerDao(SendCidData.class, sendCidDataDao);
    }
    
    public void clear() {
        userDataDaoConfig.getIdentityScope().clear();
        sendCidDataDaoConfig.getIdentityScope().clear();
    }

    public UserDataDao getUserDataDao() {
        return userDataDao;
    }

    public SendCidDataDao getSendCidDataDao() {
        return sendCidDataDao;
    }

}
