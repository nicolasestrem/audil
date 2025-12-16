package com.audil.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun setUp() {
        // Clean up any existing test database
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // Cleanup is handled by MigrationTestHelper
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        // Create the database with version 1 schema
        val db = helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data in v1 schema
            execSQL(
                """
                INSERT INTO meetings (id, title, audioPath, timestamp, duration, type, participantCount)
                VALUES (1, 'Test Meeting', '/path/to/audio.wav', ${System.currentTimeMillis()}, 60000, 'BUSINESS', 5)
                """.trimIndent()
            )
            close()
        }

        // Run migration to version 2
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Verify migration by checking if indices were created
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, false, MIGRATION_1_2)

        // Query to check if indices exist
        val cursor = migratedDb.query(
            """
            SELECT name FROM sqlite_master
            WHERE type='index' AND tbl_name='meetings'
            """.trimIndent()
        )

        val indexNames = mutableListOf<String>()
        while (cursor.moveToNext()) {
            indexNames.add(cursor.getString(0))
        }
        cursor.close()

        // Verify all three indices were created
        assertTrue(
            "index_meetings_timestamp should exist",
            indexNames.any { it.contains("index_meetings_timestamp") }
        )
        assertTrue(
            "index_meetings_type should exist",
            indexNames.any { it.contains("index_meetings_type") }
        )
        assertTrue(
            "index_meetings_participantCount should exist",
            indexNames.any { it.contains("index_meetings_participantCount") }
        )

        migratedDb.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create oldest version of the database
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data
            execSQL(
                """
                INSERT INTO meetings (id, title, audioPath, timestamp, duration, type, participantCount)
                VALUES (1, 'Test Meeting 1', '/path/to/audio1.wav', ${System.currentTimeMillis()}, 60000, 'BUSINESS', 3)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO meetings (id, title, audioPath, timestamp, duration, type, participantCount)
                VALUES (2, 'Test Meeting 2', '/path/to/audio2.wav', ${System.currentTimeMillis() - 1000}, 120000, 'PERSONAL', 2)
                """.trimIndent()
            )
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB
        ).addMigrations(MIGRATION_1_2).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun testDataIntegrityAfterMigration() {
        val timestamp = System.currentTimeMillis()

        // Create v1 database with test data
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO meetings (id, title, audioPath, timestamp, duration, type, participantCount)
                VALUES (1, 'Important Meeting', '/path/to/audio.wav', $timestamp, 90000, 'BUSINESS', 10)
                """.trimIndent()
            )
            close()
        }

        // Migrate to v2
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Verify data integrity
        val cursor = db.query("SELECT * FROM meetings WHERE id = 1")
        assertTrue("Meeting should exist after migration", cursor.moveToFirst())

        val titleIndex = cursor.getColumnIndex("title")
        val timestampIndex = cursor.getColumnIndex("timestamp")
        val typeIndex = cursor.getColumnIndex("type")
        val participantCountIndex = cursor.getColumnIndex("participantCount")

        assertEquals("Important Meeting", cursor.getString(titleIndex))
        assertEquals(timestamp, cursor.getLong(timestampIndex))
        assertEquals("BUSINESS", cursor.getString(typeIndex))
        assertEquals(10, cursor.getInt(participantCountIndex))

        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testIndexPerformance() {
        val timestamp = System.currentTimeMillis()

        // Create v1 database
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert multiple test records
            for (i in 1..100) {
                execSQL(
                    """
                    INSERT INTO meetings (id, title, audioPath, timestamp, duration, type, participantCount)
                    VALUES ($i, 'Meeting $i', '/path/to/audio$i.wav', ${timestamp - i * 1000}, 60000, '${if (i % 2 == 0) "BUSINESS" else "PERSONAL"}', ${i % 10})
                    """.trimIndent()
                )
            }
            close()
        }

        // Migrate to v2
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Query using indexed columns - should use the indices
        val timestampCursor = db.query("SELECT * FROM meetings ORDER BY timestamp DESC LIMIT 10")
        assertTrue("Should find meetings ordered by timestamp", timestampCursor.count == 10)
        timestampCursor.close()

        val typeCursor = db.query("SELECT * FROM meetings WHERE type = 'BUSINESS'")
        assertTrue("Should find business meetings", typeCursor.count > 0)
        typeCursor.close()

        val participantCursor = db.query("SELECT * FROM meetings WHERE participantCount > 5")
        assertTrue("Should find meetings with >5 participants", participantCursor.count > 0)
        participantCursor.close()

        db.close()
    }
}
