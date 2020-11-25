package sk.stuba.fei.mv.android.zaverecne.database

import androidx.room.*

@Dao
interface UserDatabaseDao {
    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * from users LIMIT 1")
    suspend fun getActive(): User?

    @Query("DELETE FROM users")
    suspend fun clear()
}