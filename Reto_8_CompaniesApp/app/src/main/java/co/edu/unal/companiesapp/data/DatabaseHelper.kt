import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "CompanyDirectory.db"
        const val DATABASE_VERSION = 1

        const val TABLE_COMPANY = "companies"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_URL = "url"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PRODUCTS = "products"
        const val COLUMN_CATEGORY = "category"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_COMPANY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_URL TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PRODUCTS TEXT,
                $COLUMN_CATEGORY TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COMPANY")
        onCreate(db)
    }

    fun insertCompany(name: String, url: String, phone: String, email: String, products: String, category: String): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_URL, url)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PRODUCTS, products)
            put(COLUMN_CATEGORY, category)
        }
        return db.insert(TABLE_COMPANY, null, contentValues)
    }

    fun getAllCompanies(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_COMPANY", null)
    }

    fun updateCompany(id: Int, name: String, url: String, phone: String, email: String, products: String, category: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_URL, url)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PRODUCTS, products)
            put(COLUMN_CATEGORY, category)
        }
        return db.update(TABLE_COMPANY, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteCompany(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_COMPANY, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun filterCompanies(name: String?, category: String?): Cursor {
        val db = readableDatabase
        val selection = StringBuilder()
        val selectionArgs = mutableListOf<String>()

        if (!name.isNullOrEmpty()) {
            selection.append("$COLUMN_NAME LIKE ?")
            selectionArgs.add("%$name%")
        }

        if (!category.isNullOrEmpty()) {
            if (selection.isNotEmpty()) selection.append(" OR ") // Combine with OR
            selection.append("$COLUMN_CATEGORY LIKE ?")
            selectionArgs.add("%$category%")
        }

        return db.query(
            TABLE_COMPANY, null, selection.toString(),
            selectionArgs.toTypedArray(), null, null, null
        )
    }


}
