package supahsoftware.crawlspace

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Single

@Suppress("LeakingThis")
@Entity(tableName = "cs_data_table")
abstract class StorableData(
    @PrimaryKey
    @ColumnInfo(name = "cs_data_key")
    val storageKey: String
) {
    @ColumnInfo(name = "cs_data_json")
    var dataJson: String = Gson().toJson(this)

    @ColumnInfo(name = "cs_data_type")
    val dataType: String = this::class.java.simpleName
}

@Dao
interface CrawlSpaceDao {
    @Insert(onConflict = REPLACE)
    fun add(storableData: StorableData)

    @Query("DELETE FROM cs_data_table WHERE cs_data_key = :id")
    fun deleteById(id: String)

    @Query("SELECT * FROM cs_data_table WHERE cs_data_key=:id")
    fun getById(id: String): Single<StorableData>

    @Query("SELECT * FROM cs_data_table")
    fun observeAllTypes(): Flowable<List<StorableData>>

    @Query("SELECT * FROM cs_data_table WHERE cs_data_type = :dataType")
    fun <T> observeSpecificType(dataType: String): Flowable<List<T>>
}

@Database(version = 1, entities = [(StorableData::class)], exportSchema = false)
abstract class CrawlSpaceDatabase : RoomDatabase() {
    abstract fun crawlSpaceDao(): CrawlSpaceDao
}