package com.universalbox.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.universalbox.app.data.model.FavoriteItem
import com.universalbox.app.data.model.TimeSchedule

@Database(
    entities = [FavoriteItem::class, TimeSchedule::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // 提供 Dao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun timeScheduleDao(): TimeScheduleDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // 数据库迁移：从版本1升级到版本2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加两个新字段，默认值为空字符串
                db.execSQL("ALTER TABLE favorites ADD COLUMN imageUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE favorites ADD COLUMN siteName TEXT NOT NULL DEFAULT ''")
            }
        }

        // 数据库迁移：从版本2升级到版本3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加tags字段
                db.execSQL("ALTER TABLE favorites ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        // 数据库迁移：从版本3升级到版本4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. 添加新字段到 favorites 表
                db.execSQL("ALTER TABLE favorites ADD COLUMN packageName TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN category TEXT NOT NULL DEFAULT '工具'")
                db.execSQL("ALTER TABLE favorites ADD COLUMN usageWeight INTEGER NOT NULL DEFAULT 0")
                
                // 2. 创建新的 time_schedules 表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS time_schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        recommendCategory TEXT NOT NULL
                    )
                """)
            }
        }

        // 数据库迁移：从版本4升级到版本5
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE favorites ADD COLUMN source TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE favorites ADD COLUMN featureTag TEXT NOT NULL DEFAULT ''")
            }
        }

        // 获取数据库实例 (单例模式，保证全局只有一个数据库连接)
        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "universal_box_db" // 数据库文件的名字
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // 添加所有迁移策略
                    .fallbackToDestructiveMigration() // 开发阶段：改表结构直接清空重建，省事
                    .build()
                    .also { Instance = it }
            }
        }
    }
}