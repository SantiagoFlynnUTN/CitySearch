package com.flynn.citysearch.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var cityDao: CityDao
    private lateinit var entries: List<CityEntity>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cityDao = db.cityDao()
       entries = listOf(
            CityEntity(519188, "Novinki", "RU", 37.666668, 55.683334, false),
            CityEntity(1283378, "Gorkhā", "NP", 84.633331, 28.0, false),
            CityEntity(1270260, "State of Haryāna", "IN", 76.0, 29.0, false),
            CityEntity(708546, "Holubynka", "UA", 33.900002, 44.599998, false),
            CityEntity(1283710, "Bāgmatī Zone", "NP", 85.416664, 28.0, false),
            CityEntity(529334, "Mar’ina Roshcha", "RU", 37.611111, 55.796391, false),
            CityEntity(1269750, "Republic of India", "IN", 77.0, 20.0, false),
            CityEntity(1283240, "Kathmandu", "NP", 85.316666, 27.716667, false),
            CityEntity(703363, "Laspi", "UA", 33.733334, 44.416668, false),
            CityEntity(3632308, "Merida", "VE", -71.144997, 8.598333, false),
            CityEntity(473537, "Vinogradovo", "RU", 38.545555, 55.423332, false),
            CityEntity(384848, "Qarah Gawl al ‘Ulyā", "IQ", 45.6325, 35.353889, false),
            CityEntity(569143, "Cherkizovo", "RU", 37.728889, 55.800835, false),
            CityEntity(713514, "Alupka", "UA", 34.049999, 44.416668, false),
            CityEntity(2878044, "Lichtenrade", "DE", 13.40637, 52.398441, false),
            CityEntity(464176, "Zavety Il’icha", "RU", 37.849998, 56.049999, false),
            CityEntity(295582, "‘Azriqam", "IL", 34.700001, 31.75, false),
            CityEntity(1271231, "Ghūra", "IN", 79.883331, 24.766666, false),
            CityEntity(690856, "Tyuzler", "UA", 34.083332, 44.466667, false),
            CityEntity(464737, "Zaponor’ye", "RU", 38.861942, 55.639999, false),
            CityEntity(707716, "Il’ichëvka", "UA", 34.383331, 44.666668, false),
            CityEntity(697959, "Partyzans’ke", "UA", 34.083332, 44.833332, false),
            CityEntity(803611, "Yurevichi", "RU", 39.934444, 43.600555, false),
            CityEntity(614371, "Gumist’a", "GE", 40.973888, 43.026943, false),
            CityEntity(874560, "Ptitsefabrika", "GE", 40.290558, 43.183613, false),
            CityEntity(874652, "Orekhovo", "GE", 40.146111, 43.351391, false),
            CityEntity(2347078, "Birim", "NG", 9.997027, 10.062094, false),
            CityEntity(2051302, "Priiskovyy", "RU", 132.822495, 42.819168, false),
            CityEntity(563692, "Dzhaga", "RU", 42.650002, 43.25, false)
        )

    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFilterCitiesSortedAlphabetically() = runTest {
        entries.shuffled().forEach { cityDao.insert(it) }

        val result = cityDao.getCitiesByPrefix("", limit = 100, offset = 0)
        val names = result.map { it.name }

        val expected = entries.map { it.name }.sorted()
        assertEquals(expected, names)
    }
}
