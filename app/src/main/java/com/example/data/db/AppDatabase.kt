package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AiSkillEntity
import com.example.data.model.SongProject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SongProject::class, AiSkillEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun skillDao(): SkillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_melody_lyricist.db"
                ).addCallback(DatabaseCallback()).build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.songDao(), database.skillDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(songDao: SongDao, skillDao: SkillDao) {
            // Seed initial 5 skills
            val skills = listOf(
                AiSkillEntity(
                    name = "Phân tích nhạc",
                    description = "Phân tích cấu trúc, nốt nhạc và trường độ theo audio thực tế",
                    iconType = "music",
                    isActive = true
                ),
                AiSkillEntity(
                    name = "Viết lời bài hát",
                    description = "Sáng tác lời mới theo chủ đề và cảm xúc mong muốn",
                    iconType = "pen",
                    isActive = true
                ),
                AiSkillEntity(
                    name = "Tối ưu giai điệu",
                    description = "Chỉnh sửa thanh điệu tiếng Việt phù hợp với cao độ nốt nhạc",
                    iconType = "tune",
                    isActive = true
                ),
                AiSkillEntity(
                    name = "Tạo tiêu đề hay",
                    description = "Gợi ý tiêu đề bài hát ấn tượng, dễ nhớ và hợp phong cách",
                    iconType = "text",
                    isActive = true
                ),
                AiSkillEntity(
                    name = "Khác",
                    description = "Tùy biến các kỹ năng sáng tạo âm nhạc nâng cao",
                    iconType = "other",
                    isActive = true
                )
            )
            skillDao.insertAll(skills)

            // Seed initial projects matching HTML mockups
            val projects = listOf(
                SongProject(
                    title = "Bếp Ấm Yêu Thương",
                    genre = "Pop Ballad",
                    subGenre = "Acoustic Ballad",
                    vocalStyle = "Nữ nhẹ nhàng",
                    tempo = "Trung bình",
                    emotion = "Lãng mạn",
                    theme = "Tình yêu",
                    structure = "Verse - Chorus",
                    instruments = "Guitar, Piano, Violin",
                    arrangement = "Hiện đại, acoustic",
                    energy = "Dịu dàng, nhẹ nhàng",
                    duration = "03:45",
                    originalLyrics = "Từng ngày trôi qua, anh vẫn nhớ...\nDù cho mưa gió, anh vẫn chờ...\nVì trong tim anh, em là tất cả...\nDù xa cách, anh vẫn luôn...\nTình yêu này, sẽ không phai...",
                    newLyrics = "Verse 1:\nMỗi sáng thức dậy, nắng len vào bếp\nHương thơm lan tỏa, ấm áp lòng mình\nDù cuộc sống có bao đổi thay\nBếp nhà vẫn là nơi bình yên nhất.\n\nChorus:\nBếp ấm yêu thương, là nơi ta trở về\nCùng nhau nấu nướng, cùng nhau sẻ chia\nNhững bữa cơm giản dị, đầy ắp tiếng cười\nLà hạnh phúc, là yêu thương, là nhà.\n\nVerse 2:\nDù ngoài kia có bao nhiêu bộn bề\nChỉ cần quay về, là thấy ấm lòng...",
                    productionGuide = "Pop Ballad, female voice, acoustic style, medium tempo (90-110 BPM), romantic emotion. Theme: love, home, family. Structure: Verse – Chorus, with soft bridge. Instruments: acoustic guitar, piano, violin. Arrangement: modern, warm, clear vocals, light percussion. Focus on natural and heartfelt singing, keep the melody smooth and expressive.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCYDkIjTpqH2PJcCKAFznhl2i-IojN-6TYjrrhCMB13ySDY-dE3rTotRHV515H4kQr1rEjK9yJvtNhp0TlxtJlsdlYeKaes-_91GLYPO88S1fc0VI-tYREAnahYjOj0L4S-5iDjzK6JawPhO2BoVEDb3mTChZdeQ49sMUQAchmbfLkJ4OhQFfZ4aOKfx7eMOYI09wsXhMQJG-UtnHimYssjfsxF1v-H7AWH5CQ4kqlfp0oo9rvbcPBvEw",
                    isSaved = true,
                    isCreated = true
                ),
                SongProject(
                    title = "Giấc Mơ Biển Xanh",
                    genre = "Acoustic",
                    subGenre = "Indie Folk",
                    vocalStyle = "Nữ trong trẻo",
                    tempo = "Chậm",
                    emotion = "Hoài niệm",
                    theme = "Du lịch, Thiên nhiên",
                    structure = "Verse - Chorus",
                    instruments = "Acoustic Guitar, Cello",
                    arrangement = "Mộc mạc, sâu lắng",
                    energy = "Nhẹ nhàng, thư thái",
                    duration = "04:12",
                    originalLyrics = "Sóng vỗ miên man, cát trắng trải dài...",
                    newLyrics = "Biển xanh ôm lấy giấc mơ tuổi thơ\nGió ru từng chiều hoàng hôn buông xuống...",
                    productionGuide = "Acoustic Indie Folk, soft female vocals, 75 BPM, contemplative and peaceful mood. Lead acoustic guitar with subtle cello backing.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAmm3T1smDhM78-mNKJS9S6dSET_uvSyrRZewWEK4sMQH3uiOxTLLLJ11_ESOGwb_d5xItet8cheYou0wY-UxsRDtMBAw3BE6WkHedd46feX2KNAP9NhJp_TfmoGtvmEDlPidOybho9mpVUXqrk-Dk9cLw6W_nlGedUjO7_D6XmVUSmHqSBeObK-o5SV5gdO9NCOYQjAuoXbNnSeMIgm24dLV8V5m598XHhWuR5zk_1Kmx3hH-ntEScLg",
                    isSaved = false,
                    isCreated = true
                ),
                SongProject(
                    title = "Nắng Trong Tim",
                    genre = "Pop",
                    subGenre = "Upbeat Pop",
                    vocalStyle = "Nữ tươi sáng",
                    tempo = "Nhanh",
                    emotion = "Vui tươi, Lạc quan",
                    theme = "Tuổi trẻ, Hy vọng",
                    structure = "Verse - PreChorus - Chorus",
                    instruments = "Synth, Bass, Drums, Guitar",
                    arrangement = "Hiện đại, rộn ràng",
                    energy = "Tràn đầy năng lượng",
                    duration = "03:28",
                    originalLyrics = "Bước qua bóng tối, đón chào bình minh...",
                    newLyrics = "Nắng vàng rực rỡ chiếu qua ngàn mây\nTrong tim rộn rã khúc ca ngày mới...",
                    productionGuide = "Upbeat Pop, energetic female voice, 120 BPM, optimistic, modern radio-ready production with punchy drums and bright synth layers.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBP5tWuCgufyvfCg1GFvUKGwEPIWcQl8M30Wn_saF458PlaOvLUYvkHzULBdCDowa6YEAIZj9sMA8xY4jMbkXxX6V3jbrc7_Kl7i2ubYdVy59Jyiw3u3JMvfJM3d4vSo3EW3F0jRRfBc42gs4VfDUj7Z-ePPiZSkVOP6MlLCI3Nr6C4G_zZiKgrQ9hIv1qMRmvsy-moygtrDJDc6ySAjITdKhgXWjLFyf2hCYQpHY-BPCSnFjJsWyldmg",
                    isSaved = true,
                    isCreated = true
                ),
                SongProject(
                    title = "Tình Bạn Vĩnh Cửu",
                    genre = "Rock",
                    subGenre = "Pop Rock",
                    vocalStyle = "Nam mạnh mẽ",
                    tempo = "Nhanh",
                    emotion = "Hào hùng, Gắn kết",
                    theme = "Tình bạn",
                    structure = "Verse - Chorus - Solo - Chorus",
                    instruments = "Electric Guitar, Bass, Drums",
                    arrangement = "Rock band, dynamic",
                    energy = "Mạnh mẽ, bùng nổ",
                    duration = "04:05",
                    originalLyrics = "Kề vai sát cánh qua ngàn chông gai...",
                    newLyrics = "Dù đường dài gió bụi ta vẫn bên nhau\nTình anh em bền vững chẳng hề phai...",
                    productionGuide = "Pop Rock anthem, strong male vocals with grit, 125 BPM, driving rhythm section with soaring electric guitar leads.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAEt-DmogdEkEZmondCQZcCCvyJw5dP9nVAb1Fd3VGKpMUV5iQLPI_5k_6IjmtKIvNFnNnTl0ytXTmgnTF2LDyJhhev71dhIIBbJiMl-NrlPguzeQM6bBrNvkTbKaAW6r-5sUYhi1b1eXrJ3h5_PUG4haaKLTQ6yX7ecsYrKuUSIOpMBurR_oYPHPTD04-ac9cQWh29fTM-nfOrmcVliqLkCn2FUfmjvf8-vo60QJh8Wx8X5VJnjMx54Q",
                    isSaved = false,
                    isCreated = true
                ),
                SongProject(
                    title = "Mùa Thu Đã Qua",
                    genre = "Ballad",
                    subGenre = "Classic Ballad",
                    vocalStyle = "Nam trầm ấm",
                    tempo = "Chậm",
                    emotion = "U sầu, Lắng đọng",
                    theme = "Kỷ niệm, Chia tay",
                    structure = "Verse - Chorus",
                    instruments = "Grand Piano, String Quartet",
                    arrangement = "Cổ điển, da diết",
                    energy = "Trầm buồn, sâu lắng",
                    duration = "03:50",
                    originalLyrics = "Lá vàng rơi rụng khắp con đường xưa...",
                    newLyrics = "Mùa thu đi qua để lại lạnh giá\nKỷ niệm ngày nào giờ trôi về đâu...",
                    productionGuide = "Emotional Ballad, warm male baritone, 72 BPM, cinematic piano and strings arrangement.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBPL7rj4MuCRUud1mtGohtv_Qv4zZQSRA2ie2x7fY7KfluHBHMIsnWNbZO39MRH7pgz7-9-TmYOkDmhJ5ZWUs5gu6mDR-Uci9MxatSQTkTrusVS11CPy7zawr1QA8U4SmzSo39ITPwCtvqYQyW0_Ri9rVQV9bbwIc8Skc_TfVuSOtq_Hsr03WENpos_0tNbzgaYy9EMcwuA5pmQkvdrg9JVGDjjGNbULwln-3l3dsVgRdA1cqQ4YteXDg",
                    isSaved = true,
                    isCreated = true
                )
            )
            songDao.insertAll(projects)
        }
    }
}
