# AI Viết Lời Theo Giai Điệu (AI Melody Lyricist)

Xây dựng ứng dụng Android chuyên nghiệp cho phép sáng tác và viết lại lời bài hát tiếng Việt khớp nhịp điệu, giai điệu và âm điệu gốc theo 5 bước chuẩn hóa, hỗ trợ AI Gemini trực tiếp kết hợp bộ dự phòng thông minh, trình phát audio waveform và quản lý dự án với Room Database.

## User Review & Critical Decisions

> [!IMPORTANT]
> Các quyết định cốt lõi đã được xác nhận dựa trên trao đổi ở Phase 1:
> - **Xử lý Audio**: Tích hợp sẵn các bài hát mẫu (Demo Songs: *Bếp Ấm Yêu Thương*, *Giấc Mơ Biển Xanh*, *Nắng Trong Tim*) cùng với trình chọn file audio thật trên thiết bị (`audio/*`), hỗ trợ phát nhạc, tạm dừng và căn chỉnh câu hát theo mốc thời gian.
> - **Cơ chế AI**: Sử dụng Gemini REST API (`gemini-3.5-flash`) để phân tích âm điệu câu chữ tiếng Việt, tự động gợi ý 5 phương án thay thế khớp số âm tiết và vần điệu, đồng thời tích hợp sẵn engine dự phòng (offline rules & rich templates) để ứng dụng hoạt động mượt mà ngay cả khi chưa có API key.
> - **Lưu trữ dữ liệu**: Sử dụng **Room Database** để lưu trữ toàn bộ dự án bài hát đã tạo/lưu, danh sách các câu hát phân tích và các kỹ năng AI (Custom Skills) do người dùng tự định nghĩa.

---

## 1. Overview & Core Concept

- **Mục tiêu**: Hỗ trợ nhạc sĩ, ca sĩ, nhà sáng tạo nội dung viết lời mới (đặt lời mới, parody, chuyển thể phong cách) cho một bài hát bất kỳ sao cho lời mới **khớp từng nhịp, từng nốt nhạc, số lượng từ và thanh điệu tiếng Việt**.
- **Đối tượng sử dụng**: Người yêu âm nhạc, content creator làm video/TikTok parody, ca sĩ cover, nhà soạn nhạc và nhà sản xuất âm nhạc cần tạo bản hướng dẫn sản xuất (Production Guide) cho Suno AI, Udio hoặc DAW.
- **Giá trị khác biệt**:
  1. Phân tích bài hát thành từng **câu hát thực tế theo dòng audio** (kèm timestamp bắt đầu và kết thúc) chứ không chỉ xuống dòng text đơn thuần.
  2. Trình sáng tác lời mới song song đối chiếu: Câu gốc vs Lời mới, nghe thử từng câu, gợi ý 5 phương án thay thế ngay tức thì.
  3. Tùy chỉnh phong cách toàn diện qua 10 chiều âm nhạc (Thể loại, Giọng hát, Thể loại phụ, Tốc độ, Cảm xúc, Chủ đề, Bố cục, Nhạc cụ, Phối khí, Năng lượng).
  4. Trình xuất **Production Guide** chuẩn tiếng Anh với đếm ký tự để copy ngay vào các công cụ AI âm nhạc hoặc gửi cho producer.

---

## 2. User Experience & Visual Design

### 2.1 Hệ thống màn hình và Luồng trải nghiệm (User Flows)

Ứng dụng gồm 4 tab điều hướng chính ở Bottom Navigation:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        BOTTOM NAVIGATION                               │
├───────────────────┬──────────────────┬────────────────┬────────────────┤
│ 🎵 Sáng tác       │ 📁 Dự án         │ ⚡ Skills      │ ⚙️ Cài đặt     │
│ (Compose Flow)    │ (Project List)   │ (AI Skills)    │ (Settings)     │
└───────────────────┴──────────────────┴────────────────┴────────────────┘
```

#### Tab 1: Sáng tác (5 bước hoàn chỉnh)
1. **Bước 01 - Nhập bài hát gốc**:
   - Khung nhập/dán lời bài hát gốc (`textarea` bo tròn, nút dán nhanh).
   - Thanh chọn file audio (hỗ trợ chọn file mp3/wav thật từ thiết bị hoặc chọn bài mẫu có sẵn).
   - Thẻ hiển thị file audio đã chọn (tên file, thời lượng `03:45`, số câu ước tính, nút *Thay file*, *Xóa*).
   - Nút hành động chính: **PHÂN TÍCH AUDIO & LỜI HÁT** (gradient xanh - chàm nổi bật).
   - Lối tắt nhanh tới *Cài đặt* và *Quản lý Skills*.

2. **Bước 02 - Phân tích theo audio**:
   - Tiêu đề và giải thích: *AI xác định câu hát theo cách ca sĩ thực sự hát, không phụ thuộc vào các xuống dòng của văn bản*.
   - Khung phát Audio Waveform: Nút Play tròn, dải sóng âm sống động đa sắc màu (xanh, tím, hồng), tổng số câu hát (`30 câu hát`) và thanh thời gian chạy (`00:00 / 03:45`).
   - Danh sách các câu hát được bóc tách: Mỗi câu hát có badge màu tròn đánh số (Câu 01: Đỏ, Câu 02: Cam, Câu 03: Xanh lá, Câu 04: Tím, Câu 05: Xanh petrol...), thời gian bắt đầu - kết thúc, text câu hát, nút nghe riêng câu đó và nút sửa.
   - Nút hành động: **Tiếp tục ➔**.

3. **Bước 03 - Sáng tác lời mới**:
   - Danh sách thẻ câu hát với giao diện đối chiếu:
     - Header thẻ: Icon bút chì xanh tím, số thứ tự câu, timestamp.
     - Dòng **Lời gốc** (màu xám nhạt trung tính).
     - Dòng **Lời mới** (chữ đậm, màu xanh lam nổi bật).
     - Bộ nút thao tác: Nút Play câu hát, nút **Sửa** (mở popup chỉnh sửa văn bản trực tiếp), nút **Gợi ý 5 câu** (mở Bottom Sheet gợi ý AI).
   - Nút điều hướng sang bước tiếp theo.

4. **Modal / Bottom Sheet - Gợi ý 5 câu thay thế**:
   - Hiển thị câu gốc đang chọn.
   - Danh sách 5 phương án do AI đề xuất khớp chuẩn nhịp và số từ, mỗi phương án có số thứ tự tròn và nút **Chọn**.
   - Hai nút cuối: **Hủy** và **Áp dụng** (nút gradient tím).

5. **Bước 04 - Phong cách bài hát mới**:
   - 10 tùy chọn phong cách âm nhạc với icon màu sắc riêng biệt và dropdown pill:
     1. *Thể loại nhạc* (Pop Ballad, Rock, Acoustic, R&B, EDM...)
     2. *Phong cách giọng hát* (Nữ nhẹ nhàng, Nam mạnh mẽ, Nữ nội lực, Nam trầm ấm...)
     3. *Thể loại phụ* (Acoustic Ballad, Indie Pop, Lo-fi...)
     4. *Tốc độ nhạc* (Chậm, Trung bình, Nhanh)
     5. *Cảm xúc* (Lãng mạn, U sầu, Vui tươi, Hào hùng...)
     6. *Chủ đề lời bài hát* (Tình yêu, Gia đình, Cuộc sống, Du lịch, Tình bạn...)
     7. *Bố cục bài hát* (Verse - Chorus, Verse - Chorus - Bridge...)
     8. *Nhạc cụ* (Guitar, Piano, Violin, Drums...)
     9. *Phối khí & sản xuất* (Hiện đại, acoustic, Điện tử, Mộc...)
     10. *Năng lượng & thể hiện* (Dịu dàng, nhẹ nhàng, Sôi động, Cao trào...)
   - Nút hành động chính: **TẠO BÀI HÁT MỚI** (Gradient tím xanh lấp lánh).

6. **Bước 05 - Bài hát của bạn & Production Guide**:
   - Thẻ bài hát hoàn thiện: Tiêu đề (e.g. *Bếp Ấm Yêu Thương*), nhãn thể loại, nút trái tim Yêu thích.
   - Ảnh bìa minh họa phong cách ấm áp nghệ thuật.
   - Trình hiển thị toàn bộ lời bài hát chia rõ từng phân đoạn (**VERSE 1**, **CHORUS**, **VERSE 2**).
   - Thanh điều khiển Audio: Seekbar kéo thả tiến trình, thời gian hiện tại / tổng thời gian, nút **Nghe thử** và nút **COPY TOÀN BỘ LỜI**.
   - Thẻ **PRODUCTION GUIDE**: Đoạn prompt tiếng Anh chuyên sâu tối ưu cho Suno/Udio/Producer, bộ đếm ký tự (`542/990 CHARACTERS`), nút **COPY PRODUCTION GUIDE**.
   - Khung mở rộng **Tùy chỉnh nâng cao** cho phép xem và tinh chỉnh lại các thông số phong cách.

#### Tab 2: Quản lý Dự án
- Thanh lọc: **Tất cả**, **Đã tạo**, **Đã lưu**.
- Danh sách dự án âm nhạc trực quan (ảnh bìa, tên bài hát, thể loại, thời lượng, ngày tạo/cập nhật, menu thao tác Xem/Sửa/Xóa).
- Nút **+** tạo dự án mới từ đầu.

#### Tab 3: Quản lý Skills (AI Skills)
- Danh sách kỹ năng AI đang có: *Phân tích nhạc*, *Viết lời bài hát*, *Tối ưu giai điệu*, *Tạo tiêu đề hay*, v.v., kèm trạng thái nhãn xanh *Đang hoạt động*.
- Nút **+ Thêm Skill**.
- Màn hình thêm Skill mới: Tab chuyển đổi *Bằng mô tả* / *Bằng công cụ/Code*, khung nhập mô tả kỹ năng, khung gợi ý prompt mẫu có thể nhấn vào để điền tự động, nút **★ Thêm Skill**.

#### Tab 4: Cài đặt (Settings)
- Quản lý Gemini API Key: Ô nhập bảo mật kèm icon ẩn/hiện mật khẩu, nút **Kiểm tra kết nối**.
- Banner thông báo kết nối trạng thái: *Kết nối thành công - Gemini 3.5 Flash*.
- Nút **Lưu cấu hình**.
- Khu vực quản lý Skills và thống kê tổng số Skills.

---

### 2.2 Định hướng Phong cách & Thiết kế (Design Guidelines Alignment)

- **Màu sắc chủ đạo**:
  - Nền ứng dụng: Xanh băng dịu mắt `0xFFEEF3FB` / `0xFFF0F4F9`
  - Thẻ Card: Trắng tinh khiết `0xFFFFFFFF` với viền nhẹ `0xFFE2E8F0` và đổ bóng mềm mại `0x0D0047AB`.
  - Màu thương hiệu (Primary): Xanh dương hoàng gia `0xFF2563EB` kết hợp chuyển sắc tím `0xFF7C3AED` và chàm `0xFF4F46E5`.
  - Màu điểm nhấn trạng thái:
    - Xanh ngọc thành công: `0xFF10B981`
    - Đỏ/Hồng nổi bật: `0xFFF43F5E`
    - Cam năng động: `0xFFF97316`
    - Tím mộng mơ: `0xFF9333EA`
- **Typography**: M3 Typography rõ ràng, các tiêu đề số thứ tự (`01.`, `02.`, `03.`, `04.`, `05.`) in hoa đậm nét, dễ quét bằng mắt.
- **Edge-to-Edge & Insets**: Đảm bảo an toàn không bị che bởi thanh trạng thái (status bar) hoặc thanh điều hướng hệ thống (navigation bar) thông qua `WindowInsets.safeDrawing`.
- **Touch Target**: Tối thiểu `48.dp` cho mọi nút bấm, icon thao tác và thẻ chọn.

---

## 3. Key Product Decisions & Trade-Offs

| Quyết định | Hướng triển khai | Lý do | Phương án đã cân nhắc |
|---|---|---|---|
| **Audio Engine** | Android `MediaPlayer` tích hợp bài mẫu + chọn file thực tế | Đơn giản, độ tương thích cao từ Android 7.0 đến Android 15+, hỗ trợ play/pause/seek chính xác từng câu hát. | ExoPlayer/Media3 (quá nặng đối với nhu cầu phát audio playback nhẹ nhàng). |
| **AI Integration** | Retrofit REST API gọi `gemini-3.5-flash` kèm bộ fallback phong phú | Bảo đảm tính tương thích, kiểm tra kết nối API key trực quan trong Cài đặt, hoạt động offline mượt mà không bị văng app nếu người dùng chưa nhập key. | Firebase AI (yêu cầu cấu hình console thủ công). |
| **Local Persistence** | Room Database (SQLite) + KSP | Lưu trữ cục bộ an toàn, quản lý các bản ghi dự án, câu hát và kỹ năng AI không phụ thuộc mạng, hỗ trợ Flow reactive updates. | SharedPreferences / DataStore (không phù hợp với cấu trúc danh sách dự án đa trường). |
| **Navigation** | State-driven Navigation với BackHandler | Chuyển đổi giữa các bước (Step 1 đến 5), màn hình chi tiết, modal sheet mượt mà, phản hồi phím Back vật lý chuẩn xác. | Navigation Compose (State-driven trực tiếp dễ quản lý dữ liệu luân chuyển giữa 5 bước sáng tác). |

---

## 4. Technical Architecture & Data Strategy

### 4.1 Sơ đồ kiến trúc tổng thể (Architecture Diagram)

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    MainActivity / Compose UI                     │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐  │  │
│  │  │ ComposeView │ │ ProjectView │ │  SkillsView │ │ SettingsUI │  │  │
│  │  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └─────┬──────┘  │  │
│  └─────────┼───────────────┼───────────────┼──────────────┼─────────┘  │
│            ▼               ▼               ▼              ▼            │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     VIEWMODEL LAYER                              │  │
│  │  • SongViewModel (5 Steps, Audio Player, AI Suggestion State)   │  │
│  │  • ProjectViewModel (Dự án, Lọc Tất cả/Đã tạo/Đã lưu)            │  │
│  │  • SkillViewModel (Quản lý 5 skills + Thêm mới)                 │  │
│  │  • SettingsViewModel (Gemini API Key, Test kết nối)              │  │
│  └─────────────────────────┬────────────────────────────────────────┘  │
└────────────────────────────┼───────────────────────────────────────────┘
                             ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        DATA & DOMAIN LAYER                             │
│  ┌─────────────────────────┴───────────────┐                           │
│  │                                         │                           │
│  ▼                                         ▼                           │
│ ┌─────────────────────────────┐           ┌──────────────────────────┐ │
│ │    GeminiAiRepository       │           │    SongRepository        │ │
│ │ • REST API (gemini-3.5-flash│           │ • ProjectDao (Room DB)   │ │
│ │ • Vietnamese Rhyme Engine   │           │ • SkillDao (Room DB)     │ │
│ │ • Production Guide Builder  │           │ • Preloaded Mock Samples │ │
│ └─────────────────────────────┘           └──────────────────────────┘ │
│                │                                       │               │
│                ▼                                       ▼               │
│ ┌─────────────────────────────┐           ┌──────────────────────────┐ │
│ │    Gemini REST Service      │           │     Room SQLite DB       │ │
│ └─────────────────────────────┘           └──────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Data Models & Entities (Room Database)

1. **`SongProject`**:
   - `id`: Long (Auto-generate)
   - `title`: String (*Bếp Ấm Yêu Thương*, *Giấc Mơ Biển Xanh*...)
   - `genre`: String (*Pop Ballad*, *Acoustic*...)
   - `subGenre`: String
   - `duration`: String (*03:45*)
   - `originalLyrics`: String
   - `newLyrics`: String
   - `productionGuide`: String
   - `thumbnailUrl`: String
   - `isSaved`: Boolean
   - `createdAt`: Long

2. **`LyricSegment`**:
   - `id`: Long
   - `projectId`: Long
   - `index`: Int (1, 2, 3...)
   - `startTime`: String (*00:12*)
   - `endTime`: String (*00:18*)
   - `originalText`: String (*Từng ngày trôi qua, anh vẫn nhớ...*)
   - `newText`: String (*Mỗi sớm mai thức dậy, anh vẫn mơ...*)
   - `suggestions`: List<String> (5 câu thay thế)

3. **`AiSkill`**:
   - `id`: Long
   - `name`: String (*Phân tích nhạc*, *Viết lời bài hát*...)
   - `description`: String
   - `iconType`: String
   - `isActive`: Boolean

---

## 5. Verification & Testing Strategy

1. **Compilation Check**: Chạy `compile_applet` để đảm bảo code Kotlin không phát sinh lỗi cú pháp, tương thích đầy đủ với Compose BOM và KSP.
2. **Luồng Sáng tác (CUJ 1)**:
   - Dán lời bài hát gốc ➔ Bấm "Phân tích Audio & Lời hát" ➔ Chuyển sang Bước 2 xem danh sách câu hát và sóng âm.
   - Bấm "Tiếp tục" ➔ Sang Bước 3 so sánh lời gốc & lời mới.
   - Bấm "Gợi ý 5 câu" ➔ Mở modal sheet hiển thị 5 gợi ý ➔ Chọn 1 gợi ý và bấm "Áp dụng".
   - Sang Bước 4 chọn 10 tiêu chí phong cách ➔ Bấm "TẠO BÀI HÁT MỚI" ➔ Sang Bước 5 xem toàn bộ bài hát hoàn thiện.
   - Xem và copy "PRODUCTION GUIDE".
3. **Luồng Quản lý Dự án (CUJ 2)**:
   - Chuyển sang tab "Dự án" ➔ Thử các tab lọc "Tất cả", "Đã tạo", "Đã lưu".
   - Mở xem lại dự án hoặc tạo dự án mới.
4. **Luồng Cài đặt & Quản lý Skills (CUJ 3)**:
   - Nhập API Key trong Cài đặt ➔ Bấm "Kiểm tra kết nối" ➔ Hiển thị huy hiệu xanh thành công.
   - Vào danh sách Skills ➔ Bấm "+ Thêm Skill" ➔ Nhập mô tả hoặc chọn prompt mẫu ➔ Thêm thành công vào danh sách.
