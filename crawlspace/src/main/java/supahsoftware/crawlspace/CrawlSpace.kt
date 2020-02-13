package supahsoftware.crawlspace

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable

class CrawlSpace(context: Context) {

    val disposable = CompositeDisposable()

    fun add(storableData: StorableData) {
        database.add(storableData)
    }

    fun delete(storableData: StorableData) {
        database.deleteById(storableData.storageKey)
    }

    inline fun <reified T : StorableData> observeAllFor(
            kClass: Class<T>,
            crossinline onSuccess: (List<T>) -> Unit,
            crossinline onError: (Throwable) -> Unit = {}
    ) {
        disposable.add(database
                .observeSpecificType<T>(kClass::class.java.simpleName)
                .map { it.map { data -> convertFromGson<T>(data) } }
                .subscribe({ onSuccess(it) }, { onError(it) }))
    }

    inline fun <reified T : StorableData> getFor(
            id: String,
            crossinline onSuccess: (T) -> Unit,
            crossinline onError: (Throwable) -> Unit = {}
    ) {
        disposable.add(database
                .getById(id)
                .map { convertFromGson<T>(it) }
                .subscribe({ onSuccess(it) }, { onError(it) }))
    }

    inline fun <reified T> convertFromGson(data: StorableData): T {
        return gson.fromJson(data.dataJson, T::class.java)
    }

    fun clearSubscriptions() = disposable.clear()

    companion object {
        val gson = Gson()
        lateinit var database: CrawlSpaceDao
        fun initializeDatabase(context: Context) {
            database = Room.databaseBuilder(context, CrawlSpaceDatabase::class.java, "crawlspace_data.database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .crawlSpaceDao()
        }
    }
}