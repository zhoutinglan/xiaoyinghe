package com.xiaoyinghe.tv.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.xiaoyinghe.tv.cache.Cache;
import com.xiaoyinghe.tv.cache.CacheDao;
import com.xiaoyinghe.tv.cache.VodCollect;
import com.xiaoyinghe.tv.cache.VodCollectDao;
import com.xiaoyinghe.tv.cache.VodRecord;
import com.xiaoyinghe.tv.cache.VodRecordDao;


/**
 * 类描述:
 *
 * @author pj567
 * @since 2020/5/15
 */
@Database(entities = {Cache.class, VodRecord.class, VodCollect.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract CacheDao getCacheDao();

    public abstract VodRecordDao getVodRecordDao();

    public abstract VodCollectDao getVodCollectDao();
}
