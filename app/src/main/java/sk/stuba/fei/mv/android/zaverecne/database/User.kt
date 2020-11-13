package sk.stuba.fei.mv.android.zaverecne.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var userName: String,
    @ColumnInfo(name = "token")
    var token: String,
    @ColumnInfo(name = "refreshToken")
    var refreshToken: String
)